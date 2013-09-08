package nuctrl.core.test;

import nuctrl.core.impl.Gateway;

public class TGateway {

	public static void main(String[] args) {
		final String local = "127.0.0.1";
		
		Thread m1 = new Thread(new Runnable(){
			@Override
			public void run() {
				Thread.currentThread().setName("machine1");
				Gateway g1 = new Gateway("g1", local, 12003, local, 12001);
				g1.init();
			}
		});
		
		Thread m2 = new Thread(new Runnable(){
			@Override
			public void run() {
				Thread.currentThread().setName("machine2");
				Gateway g2 = new Gateway("g2", local, 12001, local, 12002);
				g2.init();
			}
		});
		
		Thread m3 = new Thread(new Runnable(){
			@Override
			public void run(){
				Thread.currentThread().setName("machine3");
				Gateway g3 = new Gateway("g3", local, 12002, local, 12003);
				g3.init();
			}
		});
		
		m1.start();
		m2.start();
		m3.start();
		
		
	}

}
