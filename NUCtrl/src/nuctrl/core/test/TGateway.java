package nuctrl.core.test;

import nuctrl.center.impl.CenterIOPort;
import nuctrl.core.impl.Gateway;

import java.io.IOException;
import java.net.UnknownHostException;

public class TGateway {

	public static void main(String[] args) {
		final String local = "127.0.0.1";
		
		Thread m1 = new Thread(new Runnable(){
			@Override
			public void run() {
				Thread.currentThread().setName("machine1");
				Gateway g1 = new Gateway("g1");
				try {
					g1.setCenter(local, 20000);
					g1.setLeft(local, 12003);
					g1.setRight(local, 12001);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				g1.init();

			}
		});
		
		Thread m2 = new Thread(new Runnable(){
			@Override
			public void run() {
				Thread.currentThread().setName("machine2");
				Gateway g2 = new Gateway("g2");
				try {
					g2.setLeft(local, 12001);
					g2.setRight(local, 12002);
					g2.setCenter(local, 20000);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				g2.init();
			}
		});
		
		Thread m3 = new Thread(new Runnable(){
			@Override
			public void run(){
				Thread.currentThread().setName("machine3");
				Gateway g3 = new Gateway("g3");
				try {
					g3.setLeft(local, 12002);
					g3.setRight(local, 12003);
					g3.setCenter(local, 20000);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				g3.init();
			}
		});
		
		Thread c = new Thread(new Runnable(){

			@Override
			public void run() {
				Thread.currentThread().setName("Center");
				CenterIOPort cen = new CenterIOPort(local, 20000, null);
				try {
					cen.listen();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		
		m1.start();
		m2.start();
		m3.start();
		c.start();
		
	}

}
