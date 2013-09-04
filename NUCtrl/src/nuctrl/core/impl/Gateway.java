package nuctrl.core.impl;

// STEP build Gateway module first to get familiar with socket as well as Java

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import nuctrl.core.IF.IDispatcher;
import nuctrl.core.IF.IGatewayListener;
import nuctrl.core.IF.IMasterDup;
import nuctrl.core.IF.IPacketListener;
import nuctrl.protocol.CoreStatus;
import nuctrl.protocol.EDispatchTarget;
import nuctrl.protocol.GatewayMsg;

public class Gateway implements IMasterDup, IPacketListener, IDispatcher{
	
	
	private Monitor mn;
	private IGatewayListener coreCallback;
	
	// for neighborhood communication
	private SocketChannel sockChanToLeft;
	private SocketChannel sockChanToRight;
	private ServerSocketChannel sockChanAsServerForRight;
	private Selector selectorForLeft;
	private Selector selectorForRight;
	private NeighborListener listenerForRight;
	private NeighborListener listenerForLeft;
	private SelectionKey keyForRight;
	private SelectionKey keyForLeft;
	
	// for center communication
	private SocketChannel sockChanToCenter;
	
	private int PORT_LEFT;
	private int PORT_RIGHT;
	private String GATEWAY_ID;
	private InetSocketAddress sockAdd4Left;
	private InetSocketAddress sockAdd4Right;

	
	// no use
	private static Logger log;
	
	public Gateway(String IP4Left, String IP4Right, int port4Left, int port4Right) {
		super();
		this.GATEWAY_ID = "Haha";
		InetAddress L,R;
		try {
			L = InetAddress.getByName(IP4Left);
			R = InetAddress.getByName(IP4Right);
			sockAdd4Left = new InetSocketAddress(L, port4Left);
			sockAdd4Right = new InetSocketAddress(R, port4Right);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		log = Logger.getLogger("Gateway");
		log.setLevel(Level.INFO);
		
		log.info(String.format(
				"L:%s(%d) -- R:%s(%d)", IP4Left, port4Left , IP4Right, port4Right));
		
		
		Thread startR = new Thread(new Runnable(){
			@Override
			public void run(){
				log.info("Setting up Right connection...");
				linkToRight();
				log.info("Right Connection established");
			}
		});
		
		Thread startL = new Thread(new Runnable(){
			@Override
			public void run(){
				log.info("Setting up Left connection...");
				try {
					InetAddress L = sockAdd4Left.getAddress();
					int i = 0;
					log.info(String.format(
							"%s: Waiting for server on left at %s:%d", 
							GATEWAY_ID,
							sockAdd4Left.getHostName(),
							sockAdd4Left.getPort()));
					while(!L.isReachable(1)){
						i ++ ;
						log.info("Round 1...");
						Thread.sleep(10);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				linkToLeft();
				log.info("Left connection established");
			}
		});
		
		startR.start();
		startL.start();
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

	@Override
	public void linkToRight() {
		// toRight acts as a server
		try {
			sockChanAsServerForRight = ServerSocketChannel.open();
			selectorForRight = Selector.open();
			log.info(String.format("%s: Socket To Right open", GATEWAY_ID));
			ServerSocket ss = sockChanAsServerForRight.socket();
			
			ss.bind(this.sockAdd4Right); //blocking
			log.info(String.format(
					"%s: Socket To Right bind at %s:%d", 
					GATEWAY_ID,
					sockAdd4Right.getHostName(),
					sockAdd4Right.getPort()));
			
			sockChanAsServerForRight.configureBlocking(false);
			sockChanAsServerForRight.register(
					selectorForRight, SelectionKey.OP_ACCEPT);
	
			while (true){
				selectorForRight.select();
				
				Set readyKeys = selectorForRight.selectedKeys();
				Iterator iter = readyKeys.iterator();
				
				while (iter.hasNext()){
					SelectionKey key = (SelectionKey) iter.next();
					iter.remove();
					if (key.isAcceptable()){
						// the server socket receives clients
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						sockChanToRight = server.accept();
						sockChanToRight.configureBlocking(false);
						keyForRight = sockChanToRight.register(
								selectorForRight, SelectionKey.OP_WRITE | SelectionKey.OP_READ
								);
						ByteBuffer buf = ByteBuffer.allocate(256);
						keyForRight.attach(buf);
						
					}
				}
				
				break;//break the while(1)
			} // end of while(true)
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// setup listener
		listenerForRight = new NeighborListener(this, keyForRight);
		Thread lL = new Thread(new Runnable(){
			
			@Override
			public void run() {
				listenerForRight.listen();
			}
			
		});
		lL.start();
	}

	@Override
	public void linkToLeft() {
		try {
			this.sockChanToLeft = SocketChannel.open(this.sockAdd4Left);
			log.info(String.format(
					"%s: Socket To Left open at %s", GATEWAY_ID, sockAdd4Left.getHostName()));
			
			this.sockChanToLeft.configureBlocking(false);
			this.selectorForLeft = Selector.open();
			this.keyForLeft = sockChanToLeft.register(
					selectorForLeft, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
			
			// XXX Will buffer got disappeared after this function's end?
			ByteBuffer buf = ByteBuffer.allocate(256);
			keyForLeft.attach(buf);
			
			listenerForLeft = new NeighborListener(this, keyForLeft);
			Thread lL = new Thread(new Runnable(){

				@Override
				public void run() {
					listenerForLeft.listen();
				}
				
			});
			lL.start();
		
		} catch (ConnectException ce){
			ce.printStackTrace();
		}
		 catch (IOException e) {
			e.printStackTrace();
		}
					
	}

	
	void readFromSide(String whichSide, ByteBuffer buf){
		// parse the GatewayMsg
	}
	
	@Override
	public void dispatchTo(EDispatchTarget target, GatewayMsg msg){
		// FIXME dispatch is quite important
		SelectionKey key = null;
		SocketChannel sockChan = null;
		switch(target){
		case TO_CENTER:
			break;
			
		case TO_LEFT:
			key = this.keyForLeft;
			sockChan = this.sockChanToLeft;
			
		case TO_RIGHT:
			key = this.keyForRight;
			sockChan = this.sockChanToRight;
			break;
			
		case TO_CORE:
			break;
		
		case TO_OUT:
			break;
		}
		
		
		while(true){
			if (key.isWritable()){
				SocketChannel client = (SocketChannel) key.channel();
				ByteBuffer output = (ByteBuffer) key.attachment();
				synchronized (output){
				output.flip();
				try {
					client.write(output);
				} catch (IOException e) {
						e.printStackTrace();
				}} // end of synchronize
			}
			break;//break the while(1)
		}
	}
}

