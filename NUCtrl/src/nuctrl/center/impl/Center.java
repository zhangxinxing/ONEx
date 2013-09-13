package nuctrl.center.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nuctrl.core.IF.IDispatcher;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.GatewayMsgFactory;

public class Center implements IDispatcher{
	private CenterIOPort centerIO;
	private List<ByteBuffer> dispatchMsgQueue = new LinkedList<ByteBuffer>();

	private boolean portReady;
	
	public void initPort(){
	}

	public void init(){
		// init connection
		Thread startPort = new Thread(new Runnable(){
			@Override
			public void run(){
				Thread.currentThread().setName("Center Port");
				centerIO = new CenterIOPort(null, 0, null);
				portReady = true;
				try {
					centerIO.listen();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		startPort.start();

		Thread dispatcher = new Thread(new Runnable(){
			@Override
			public void run(){
				Thread.currentThread().setName("Center dispatcher");
				dispatch();
			}
		});
		dispatcher.start();


	}




	@Override //OPT dispatch directly
	public void dispatchDaemon(ByteBuffer msg){
		// msg has been Safely copied
		synchronized (this.dispatchMsgQueue){
			this.dispatchMsgQueue.add(msg);
			this.dispatchMsgQueue.notify();
		}
	}

	@Override
	public void dispatch(){
		ByteBuffer buf;
		while(true){
			synchronized(this.dispatchMsgQueue){
				while(this.dispatchMsgQueue.isEmpty()){
					// very important skill to avoid thread-safe issue
					try {
						this.dispatchMsgQueue.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				buf = (ByteBuffer)this.dispatchMsgQueue.remove(0);
			} // end of sync

			List<GatewayMsg> msgs = GatewayMsgFactory.parseGatewayMsg(buf);
			Iterator<GatewayMsg> iter = msgs.iterator();
			while(iter.hasNext()){
				GatewayMsg msg = iter.next();
				// logic here
			}
		}
	}


}
