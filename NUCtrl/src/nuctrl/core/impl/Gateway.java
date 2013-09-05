package nuctrl.core.impl;

// STEP build Gateway module first to get familiar with socket as well as Java

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import nuctrl.core.IF.IDispatcher;
import nuctrl.core.IF.IGatewayListener;
import nuctrl.core.IF.IMasterDup;
import nuctrl.core.IF.IPacketListener;
import nuctrl.protocol.CoreStatus;
import nuctrl.protocol.EDispatchTarget;
import nuctrl.protocol.GatewayMsg;

import org.apache.log4j.Logger;


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
	
	private ByteBuffer buf4L;
	private ByteBuffer buf4R;
	
	// for center communication
	private SocketChannel sockChanToCenter;
	
	private String GATEWAY_ID;
	private InetSocketAddress sockAdd4Left;
	private InetSocketAddress sockAdd4Right;
	private ReentrantLock lockOnLeftSel;
	private ReentrantLock lockOnRightSel;

	// no use
	private static Logger log;
	
	public Gateway(String gid,
			String IP4Left, int portL,
			String IP4Right, int portR) {
		super();
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
		buf4L = ByteBuffer.allocate(256);
		buf4R = ByteBuffer.allocate(256);
}
	
	
	public void init(){
		// init connection
		this.lockOnLeftSel = new ReentrantLock();
		this.lockOnRightSel = new ReentrantLock();
		
		Thread startR = new Thread(new Runnable(){
			@Override
			public void run(){
				log.info("Setting up Right connection...");
				linkToRight();
				log.info(GATEWAY_ID + ": Right Connection established" + 
						sockChanToRight.socket().toString());
			}
		});
		
		Thread startL = new Thread(new Runnable(){
			@Override
			public void run(){
				log.info("Setting up Left connection...");
				linkToLeft();
				log.info(GATEWAY_ID + ": Left connection established" + 
						sockChanToLeft.socket().toString());
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
			log.debug(this.GATEWAY_ID + ": Right Socket Open");
			
			ServerSocket ss = sockChanAsServerForRight.socket();
			
			ss.bind(this.sockAdd4Right); //blocking
			log.debug(this.GATEWAY_ID+ ": Right Socket bind to" + sockAdd4Right.toString());
			
			sockChanAsServerForRight.configureBlocking(false);
			sockChanAsServerForRight.register(
					selectorForRight, SelectionKey.OP_ACCEPT);
	

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
					
					log.debug(this.GATEWAY_ID + ": Right Socket Accepted: "
							+ sockChanToRight.socket().toString());
					
					if(sockChanToRight.finishConnect()){
						sockChanToRight.configureBlocking(false);
						keyForRight = sockChanToRight.register(selectorForRight, SelectionKey.OP_READ);
						ByteBuffer buf = ByteBuffer.allocate(256);
						keyForRight.attach(buf);						
					}
					
				}
			}
				
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// setup listener
		listenerForRight = new NeighborListener(this, selectorForRight, this.lockOnRightSel);
		Thread lL = new Thread(new Runnable(){
			@Override
			public void run() {
				listenerForRight.listen();
			}
			
		});
		lL.start();
		log.trace("Leaving LinkToRight");
	}

	@Override
	public void linkToLeft() {
		while (true){
			try {
				sockChanToLeft = SocketChannel.open(this.sockAdd4Left);
				break;
			} catch (ConnectException ce){
				log.info("Left Socket: No Server is on, retry in 5s...");
				try {
					Thread.sleep(5000);//miliseconds
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			} catch (IOException e) {
				e.printStackTrace();
			}
		} // end of while(1)
		
		log.debug(this.GATEWAY_ID + ": Left Socket connected: " + sockChanToLeft.toString());

		try {
			sockChanToLeft.configureBlocking(false);
			selectorForLeft = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			keyForLeft = sockChanToLeft.register(
					selectorForLeft, SelectionKey.OP_READ);
		} catch (ClosedChannelException e) {
			e.printStackTrace();
		}
		
		// XXX Will buffer got disappeared after this function's end? Y, it will
		
		keyForLeft.attach(buf4L);
		debug_key(keyForLeft, log);
		
		listenerForLeft = new NeighborListener(this, selectorForLeft, lockOnRightSel);
		
		Thread lL = new Thread(new Runnable(){
			@Override
			public void run() {
				listenerForLeft.listen();
			}
			
		});
		lL.start();
	}

	
	void readFromSide(String whichSide, ByteBuffer buf){
		// parse the GatewayMsg
	}
	
	@Override
	public void dispatchTo(EDispatchTarget target, GatewayMsg msg){
		// FIXME dispatch is quite important
		SocketChannel sockChan = null;
		Selector sl = null;
		
		switch(target){
		case TO_CENTER:
			break;
			
		case TO_LEFT:
			sl = this.selectorForLeft;
			sockChan = this.sockChanToLeft;
			break;
			
		case TO_RIGHT:
			sockChan = this.sockChanToRight;
			break;
			
		case TO_CORE:
			break;
		
		case TO_OUT:
			break;
		}
		
		
		log.debug(this.GATEWAY_ID + ": Send Message to "+ sockChan.toString());
		
		this.lockOnLeftSel.lock();
		try {
			sl.wakeup();
			log.debug("--------------");
			debug_key(this.keyForLeft, log);
			this.keyForLeft = sockChan.register(sl, SelectionKey.OP_WRITE);
			this.keyForLeft.attach(this.buf4L);
			debug_key(this.keyForLeft, log);
			
		} catch (ClosedChannelException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
//		log.debug("before change interest");
//		sl.wakeup();
//		this.keyForLeft.interestOps(SelectionKey.OP_WRITE);
//		log.debug("after change interest");
		
		try {
			sl.select();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		log.debug("Pass sl.select");
		
		Set keys = sl.selectedKeys();
		Iterator iter = keys.iterator();
		debug_sel(sl, log);
		debug_key(this.keyForLeft, log);
		log.trace(keys.toArray().length);
		
		while (iter.hasNext()){
			SelectionKey key = (SelectionKey) iter.next();
			iter.remove();
			
			if (key.isWritable()){
				log.trace("key is writable");
				
				SocketChannel client = (SocketChannel) key.channel();
				
				debug_key(key, log);
				ByteBuffer output = (ByteBuffer) key.attachment();
				// FIXEME attachment can be null
				synchronized (output){
					log.trace("lock on output got");
					msg.writeTo(output);
					output.flip();
					try {
						client.write(output);
						log.trace("write over");
					} catch (IOException e) {
						e.printStackTrace();
					}} // end of synchronize
		}
		}
		
		this.keyForLeft.interestOps(SelectionKey.OP_READ);
		this.lockOnLeftSel.unlock();
		log.debug("Unlock on sel");
	}
	
	private void debug_sel(Selector sl, Logger log){
		Iterator keys = sl.keys().iterator();
		while(keys.hasNext()){
			SelectionKey k = (SelectionKey) keys.next();
			log.debug("Sel: " + k.channel().toString());
			debug_key(k, log);
		}
	}
	
	private void debug_key(SelectionKey key, Logger log){
		log.debug("key: " + System.identityHashCode(key));
		log.debug("key: " + key.channel().toString() + "-" + key.interestOps() + "/" + key.readyOps());
		String s = "is";
		if (!(key.attachment() == null))
			s = "is not";
			
		log.debug("key: attachment " + s + " null");
	}
	 
	
}//end of class



