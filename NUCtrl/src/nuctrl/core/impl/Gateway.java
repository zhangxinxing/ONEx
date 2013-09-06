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

	private List dispatchMsgQueue = new LinkedList();
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
				Thread.currentThread().setName(GATEWAY_ID + "'s right listener");
				listenerForRight = new DispatchListener(sockAdd4Right, (ServerSocketChannel) null, cb);
				listenerForRight.listen();
			}
		});
		
		Thread startL = new Thread(new Runnable(){
			@Override
			public void run(){
				Thread.currentThread().setName(GATEWAY_ID + "'s left listener");
				listenerForLeft = new DispatchListener(sockAdd4Left, (SocketChannel)null, cb);
				listenerForLeft.listen();
			}
		});
		
		startR.start();
		startL.start();
		
		
		Thread dispatcher = new Thread(new Runnable(){
			@Override
			public void run(){
				Thread.currentThread().setName(GATEWAY_ID + " dispatcher");
				dispatch();
			}
		});
		dispatcher.start();
		log.info(this.toString() + " ready");
	}

	
	@Override
	public void dispatchDaemon(ByteBuffer msg){
		// FIXME copy requires
		ByteBuffer m = msg;
		synchronized (this.dispatchMsgQueue){
			this.dispatchMsgQueue.add(m);
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
		}
	}
	
	@Override
	public String toString(){
		return this.GATEWAY_ID + "'s dispatcher";
	}

	
}//end of class



