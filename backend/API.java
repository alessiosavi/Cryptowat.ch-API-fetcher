package backend;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import coin.Coin;
import market.Market;

public class API {

	public Dumper dumper;

	// This is the list of Main coin (ETH,BTC)
	public List<Coin> myCoin;

	public String assets = "https://api.cryptowat.ch/assets"; // this API will be used for retrieve every coin info

	/**
	 * This method fetch every coin from cryptowat.ch and load them into a Data
	 * structure useful for further analysis For every coin, it populate the data
	 * relative to the: Name, link for get price
	 * 
	 * @throws MalformedURLException
	 */
	public void getCoin() throws MalformedURLException {
		this.getAllCoin();
		for (Coin coin : myCoin) {
			if (!coin.isFiat()) {
				String url = assets + "/" + coin.getSymbol();
				JsonElement assetsList = new JsonObject();
				// Filter only the result
				assetsList = getJSON(url).get("result");
				JsonElement assets = new JsonObject();
				assets = assetsList.getAsJsonObject().get("markets").getAsJsonObject().get("base");
				if (assets != null && assets.getAsJsonArray() != null && assets.getAsJsonArray().size() >= 1) {
					for (JsonElement element : assets.getAsJsonArray()) {
						String marketName = element.getAsJsonObject().get("exchange").getAsString();
						String coinName = element.getAsJsonObject().get("pair").getAsString();
						String urlTrade = element.getAsJsonObject().get("route").getAsString();
						Market market = new Market(marketName, coinName, urlTrade, getPrice(urlTrade));
						coin.addMarket(market);
					}
				}
				System.out.println(coin.toString());
				try {
					Dumper.dumpToFile(coin.toString(), "cryptowat-coin");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		for (Coin coin : myCoin) {
			System.out.println(coin.toString());
		}
	}

	/**
	 * This method load every coin in the myCoin List Is used for load every coin
	 * name for further analysis
	 * 
	 * @param URL
	 *            - String of the json url API
	 * @return JsonObject - Response of the API
	 * @throws MalformedURLException
	 */

	public void getAllCoin() throws MalformedURLException {
		myCoin = new ArrayList<Coin>();
		JsonElement assetsList = new JsonObject();
		assetsList = getJSON(assets).get("result").getAsJsonArray();
		for (JsonElement element : assetsList.getAsJsonArray()) {
			String symbol = element.getAsJsonObject().get("symbol").getAsString();
			String name = element.getAsJsonObject().get("name").getAsString();
			boolean fiat = Boolean.valueOf(element.getAsJsonObject().get("fiat").getAsBoolean());
			myCoin.add(new Coin(name, symbol, fiat));
		}

		for (Coin coin : myCoin) {
			System.out.println(coin.toString());
		}

	}

	public float getPrice(Market market) throws MalformedURLException {
		String url = market.getUrlTrade() + "/price";
		JsonElement element = new JsonObject();
		element = getJSON(url).get("result").getAsJsonArray();
		String price = element.getAsJsonObject().get("price").getAsString();
		return Float.valueOf(price);
	}

	public float getPrice(String urlMarket) throws MalformedURLException {
		String url = urlMarket + "/price";
		JsonElement element = new JsonObject();
		element = getJSON(url).get("result");
		String price = element.getAsJsonObject().get("price").getAsString();
		return Float.valueOf(price);
	}

	public JsonObject getJSON(String URL) throws MalformedURLException {
		URL url = new URL(URL);
		HttpURLConnection request = null;

		try {
			request = (HttpURLConnection) url.openConnection();
			request.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Convert to a JSON object to print data
		JsonParser jp = new JsonParser(); // from gson
		JsonElement root = null;
		try {
			root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
		} catch (IOException e) {
			e.printStackTrace();
		} // Convert the input stream to a json element
		JsonObject oggetto = root.getAsJsonObject();
		return oggetto;
	}
}
