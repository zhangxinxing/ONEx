package nuctrl.core.test;

import nuctrl.core.impl.Gateway;

public class TGateway {

	public static void main(String[] args) {
		String local = "127.0.0.1";
		Gateway g = new Gateway(
				local, local ,12345,12388);
	}

}
