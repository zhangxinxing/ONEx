package nuctrl.gateway;

// STEP build Gateway module first to get familiar with socket as well as Java

import nuctrl.core.impl.Monitor;
import nuctrl.interfaces.IDispatcher;
import nuctrl.interfaces.IGatewayListener;
import nuctrl.interfaces.IMasterDup;
import nuctrl.interfaces.IPacketListener;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.GatewayMsgFactory;
import nuctrl.protocol.GatewayMsgType;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Gateway implements IMasterDup, IPacketListener, IDispatcher{
	/* Configuration */
	private static final boolean isRingTest = true;
	
	
	private Monitor mn;
	private Gateway cb;
	private IGatewayListener coreCallback;
	
	// for neighborhood communication
	private IOPortServer rightPort;
	private IOPortClient leftPort;
	private IOPortClient portToCenter;
	private boolean leftReady;
	private boolean rightReady;
	private boolean centerReady;
	
	private String GATEWAY_ID;
	private InetSocketAddress sockAdd4Left;
	private InetSocketAddress sockAdd4Right;
	private InetSocketAddress sockAdd4Center;
	

	private List<ByteBuffer> dispatchMsgQueue = new LinkedList<ByteBuffer>();


	private boolean center_alive;


	
	// logger
	private static Logger log;
	
	public Gateway(String gid){
		super();
		
		this.cb = this;
		this.GATEWAY_ID = gid;
		this.leftReady = false;
		this.rightReady = false;
		this.centerReady = false;
		
		log = Logger.getLogger(Gateway.class.getName());
}
	
	public Gateway(IGatewayListener gl){
		this.coreCallback = gl;
	}

	public void setLeft(String ip, int port) 
			throws UnknownHostException{
		this.sockAdd4Left = new InetSocketAddress(InetAddress.getByName(ip), port);
	}
	
	public void setRight(String ip, int port) 
			throws UnknownHostException{
		this.sockAdd4Right = new InetSocketAddress(InetAddress.getByName(ip), port);
	}
	
	public void setCenter(String ip, int port) 
			throws UnknownHostException{
		this.sockAdd4Center = new InetSocketAddress(InetAddress.getByName(ip), port);
	}
	
	public void init(){
		// init connection
		Thread startR = new Thread(new Runnable(){
			@Override
			public void run(){
				Thread.currentThread().setName(GATEWAY_ID + "'s right listener");
				rightPort = new IOPortServer(sockAdd4Right, cb);
				rightReady = true;
				try {
					rightPort.listen();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		Thread startL = new Thread(new Runnable(){
			@Override
			public void run(){
				Thread.currentThread().setName(GATEWAY_ID + "'s left listener");
				leftPort = new IOPortClient(sockAdd4Left, cb);
				leftReady = true;
				try {
					leftPort.listen();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		Thread startC = new Thread(new Runnable(){
			@Override
			public void run() {
				Thread.currentThread().setName(GATEWAY_ID + " 's central listener");
				portToCenter = new IOPortClient(sockAdd4Center, cb);
				centerReady = true;
				try {
					portToCenter.listen();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		});
		
		startR.start();
		startL.start();
		startC.start();
		
		
		Thread dispatcher = new Thread(new Runnable(){
			@Override
			public void run(){
				Thread.currentThread().setName(GATEWAY_ID + " dispatcher");
				dispatch();
			}
		});
		dispatcher.start();
		log.info(this.toString() + " ready");
		
		
		// FIXME think of the bad status
		while(!this.leftReady || !this.centerReady || !this.rightReady){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		GatewayMsg hello = GatewayMsgFactory.getGatewatMsg(GatewayMsgType.HELLO, (short)8, (short)8);
		this.leftPort.sendToOnePeer((ByteBuffer) hello.toBuffer().flip());
		this.portToCenter.sendToOnePeer((ByteBuffer) hello.toBuffer().flip());
		
	
		if (Gateway.isRingTest){
			if(this.GATEWAY_ID == "g1"){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				log.info("DEBUG::TOKEN GEN");
				GatewayMsg msg = GatewayMsgFactory.getGatewatMsg(GatewayMsgType.HELLO_ACK, (short)0, (short)0);
				this.leftPort.sendToOnePeer((ByteBuffer) msg.toBuffer().flip());
				log.info("DEBUG::TOKEN GOT");
				}
			}
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
				
				if(isRingTest){
					this.leftPort.sendToOnePeer((ByteBuffer) msg.toBuffer().flip());
				} else if (!mn.isCpuBusy()){
					// do it
					// this.coreCallback(msg);
				} else if (this.center_alive){
					// sendToCenter
					this.portToCenter.sendToOnePeer((ByteBuffer) msg.toBuffer().flip());
				} else {
					// send To Neighbor
				}
			}
		}
	}
	
	@Override
	public String getControllerInfo() {
		return mn.getControllerInfo();
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

	@Override
	public String toString(){
		return this.GATEWAY_ID + "'s dispatcher";
	}

	
}//end of class



