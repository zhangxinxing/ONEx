package nuctrl.core.impl;

// STEP build Gateway module first to get familiar with socket as well as Java

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import nuctrl.core.IF.IDispatcher;
import nuctrl.core.IF.IGatewayListener;
import nuctrl.core.IF.IMasterDup;
import nuctrl.core.IF.IPacketListener;
import nuctrl.protocol.CoreStatus;

import org.apache.log4j.Logger;


public class Gateway implements IMasterDup, IPacketListener, IDispatcher{
	
	
	private Monitor mn;
	private Gateway cb;
	private IGatewayListener coreCallback;
	
	// for neighborhood communication
	private DispatchListener listenerForRight;
	private DispatchListener listenerForLeft;
	
	
	private String GATEWAY_ID;
	private InetSocketAddress sockAdd4Left;
	private InetSocketAddress sockAdd4Right;

	private List dispatchQueue = new LinkedList();
	// logger
	private static Logger log;
	
	public Gateway(String gid,
			String IP4Left, int portL,
			String IP4Right, int portR) {
		super();
		
		this.cb = this;
		this.GATEWAY_ID = gid;
		InetAddress L,R;
		try {
			L = InetAddress.getByName(IP4Left);
			R = InetAddress.getByName(IP4Right);
			sockAdd4Left = new InetSocketAddress(L, portL);
			sockAdd4Right = new InetSocketAddress(R, portR);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		log = Logger.getLogger(Gateway.class.getName());
}
	
	
	public Gateway(IGatewayListener gl){
		this.coreCallback = gl;
	}

	@Override
	public String getControllerInfo() {
		return mn.getControllerInfo();
	}

	
	@Override
	public CoreStatus getStatus() {
		return mn.getStatus();
		
	}

	@Override
	public void onMsgFromMaster() {
		// TODO Auto-generated method stub
		// behave something
		coreCallback.handleMessageg();
		
	}
	

	@Override
	public void sendToMaster() {
		// TODO Auto-generated method stub
		// do sth using socket
		
	}

	@Override
	public void onPktFromSw() {
		// TODO Auto-generated method stub
		/*
		 * if not busy:
		 * 		callback.handle()
		 * 
		 * else if star:
		 * 		sendToMaster()
		 * 
		 * else if ring:
		 * 		sendToFriends();
		 */
		
	}

	public void init(){
		
		// init connection
		
		Thread startR = new Thread(new Runnable(){
			@Override
			public void run(){
				Thread.currentThread().setName(GATEWAY_ID + " connects Right");
				log.info("Setting up Right connection...");
				
				listenerForRight = new DispatchListener(sockAdd4Right, (ServerSocketChannel) null, cb);

				log.info(listenerForRight.toString() + " ready");
				log.debug(Thread.currentThread().getName() + " terminate");
			}
		});
		
		
		
		Thread startL = new Thread(new Runnable(){
			@Override
			public void run(){
				Thread.currentThread().setName(GATEWAY_ID + " connects Left");
				log.info("Setting up Left connection...");
				listenerForLeft = new DispatchListener(sockAdd4Left, (SocketChannel)null, cb);
				
				log.debug(Thread.currentThread().getName() + "terminate");
			}
		});
		
		startR.start();
		startL.start();
		
		try {
			startR.join();
			startL.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// setup listener
		Thread lR = new Thread(new Runnable(){
			@Override
			public void run() {
				Thread.currentThread().setName(GATEWAY_ID + " Right Listener");
				listenerForRight.listen();
				log.debug(Thread.currentThread().getName() + "terminate");
			}
		});
		
		lR.start();
		
		Thread lL = new Thread(new Runnable(){
			@Override
			public void run() {
				Thread.currentThread().setName(GATEWAY_ID + " Left listener");
				listenerForLeft.listen();
				log.debug(Thread.currentThread().getName() + "terminate");
			}
			
		});
		log.info(this.listenerForLeft.toString() + " ready");
		lL.start();
		
		
		
		Thread dispatcher = new Thread(new Runnable(){
			@Override
			public void run(){
				Thread.currentThread().setName(GATEWAY_ID + " dispatcher");
				dispatch();
				log.debug(Thread.currentThread().getName() + " terminates");
			}
		});
		dispatcher.start();
		
	}

	
	@Override
	public void dispatchDaemon(ByteBuffer msg){
		// FIXME copy requires
		ByteBuffer m = msg;
		synchronized (this.dispatchQueue){
			this.dispatchQueue.add(m);
			this.dispatchQueue.notify();
		}
	}
	
	
	@Override
	public void dispatch(){
		log.info("dispatcher ready");
		ByteBuffer buf;
		while(true){
			synchronized(this.dispatchQueue){
				while(this.dispatchQueue.isEmpty()){
					// very important skill to avoid thread-safe issue
					try {
						this.dispatchQueue.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				buf = (ByteBuffer)this.dispatchQueue.remove(0);
			} // end of sync
			
			byte[] arr = buf.array();
			for (int i = 0; i < 4; i++){
				System.out.println(arr[i]);
			}
		

		}
	}

	
}//end of class



