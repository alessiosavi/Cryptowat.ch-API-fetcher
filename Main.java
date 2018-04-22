import java.net.MalformedURLException;

import backend.API;

public class Main {

	static API api;

	public static void main(String[] args) throws MalformedURLException {
		// TODO Auto-generated method stub
		api = new API();
		api.getCoin();

	}

}
