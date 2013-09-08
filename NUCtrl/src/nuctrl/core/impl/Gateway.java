package nuctrl.core.impl;

// STEP build Gateway module first to get familiar with socket as well as Java

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nuctrl.core.IF.IDispatcher;
import nuctrl.core.IF.IGatewayListener;
import nuctrl.core.IF.IMasterDup;
import nuctrl.core.IF.IPacketListener;
import nuctrl.core.datastruct.Buffer;
import nuctrl.protocol.CoreStatus;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.GatewayMsgFactory;
import nuctrl.protocol.GatewayMsgType;

import org.apache.log4j.Logger;

public class Gateway implements IMasterDup, IPacketListener, IDispatcher{
	/* Configuration */
	private static final boolean isRingTest = true;
	
	
	private Monitor mn;
	private Gateway cb;
	private IGatewayListener coreCallback;
	
	// for neighborhood communication
	private DispatchListener listenerForRight;
	private DispatchListener listenerForLeft;
	private boolean leftReady;
	private boolean rightReady;
	
	private String GATEWAY_ID;
	private InetSocketAddress sockAdd4Left;
	private InetSocketAddress sockAdd4Right;
	
	
	
	
	private ByteBuffer buf4Left;
	private ByteBuffer buf4Right;

	private List<ByteBuffer> dispatchMsgQueue = new LinkedList<ByteBuffer>();
	
	// logger
	private static Logger log;
	
	public Gateway(String gid,
			String IP4Left, int portL,
			String IP4Right, int portR) {
		super();
		
		this.cb = this;
		this.GATEWAY_ID = gid;
		this.buf4Left = ByteBuffer.allocate(1024);
		this.buf4Right = ByteBuffer.allocate(1024);
		this.leftReady = false;
		this.rightReady = false;
		
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
				rightReady = true;
				listenerForRight.listen();
			}
		});
		
		Thread startL = new Thread(new Runnable(){
			@Override
			public void run(){
				Thread.currentThread().setName(GATEWAY_ID + "'s left listener");
				listenerForLeft = new DispatchListener(sockAdd4Left, (SocketChannel)null, cb);
				leftReady = true;
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
		
		while(!this.leftReady){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		GatewayMsg hello = GatewayMsgFactory.getGatewatMsg(GatewayMsgType.HELLO, (short)1, (short)2);
		synchronized(this.buf4Left){
			hello.writeTo(this.buf4Left);
			this.buf4Left.flip();
			this.listenerForLeft.sendToOnePeer(this.buf4Left);
		}
		
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(this.GATEWAY_ID == "g1"){
			log.info("Before gen");
			this.buf4Left.clear();
			GatewayMsg msg = GatewayMsgFactory.getGatewatMsg(GatewayMsgType.HELLO_ACK, (short)0, (short)0);
			synchronized(this.buf4Left){
				msg.writeTo(this.buf4Left);
				this.buf4Left.flip();
				this.listenerForLeft.sendToOnePeer(this.buf4Left);
				log.info("gen");
			}
		}
	}

	
	@Override
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
			
			// XXX ring test
			if(isRingTest){
				this.listenerForLeft.sendToOnePeer(buf);
			} else{
				List<GatewayMsg> msgs = GatewayMsgFactory.parseGatewayMsg(buf);
				Iterator<GatewayMsg> iter = msgs.iterator();
				while(iter.hasNext()){
					GatewayMsg msg = iter.next();
					// process every single msg
				}
			}
		}
	}
	
	
	public void genLeftPkg(){
		GatewayMsg msg = GatewayMsgFactory.getGatewatMsg(GatewayMsgType.HELLO, (short)0, (short)0);
		this.listenerForLeft.sendToOnePeer(msg.toBuffer());
	}
	
	
	
	@Override
	public String toString(){
		return this.GATEWAY_ID + "'s dispatcher";
	}

	
}//end of class



