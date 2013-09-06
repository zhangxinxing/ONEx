package nuctrl.core.test;

import nuctrl.core.impl.Gateway;
import nuctrl.protocol.EDispatchTarget;
import nuctrl.protocol.GatewayMsg;

public class TGateway {

	public static void main(String[] args) {
		final String local = "127.0.0.1";
		
		Thread m1 = new Thread(new Runnable(){
			@Override
			public void run() {
				Thread.currentThread().setName("machine1");
				Gateway g1 = new Gateway("g1", local, 12345, local, 12346);
				g1.init();
			}
		});
		
		Thread m2 = new Thread(new Runnable(){

			@Override
			public void run() {
				Thread.currentThread().setName("machine2");
				Gateway g2 = new Gateway("g2", local, 12346, local, 12345);
				g2.init();
			}
		});
		
		m1.start();
		m2.start();
	}

}
