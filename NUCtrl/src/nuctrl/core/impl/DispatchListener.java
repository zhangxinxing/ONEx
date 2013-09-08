package nuctrl.core.impl;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nuctrl.core.IF.IDispatcher;
import nuctrl.core.datastruct.Buffer;
import nuctrl.core.debug.Dump;
import nuctrl.protocol.DispatchRequest;
import nuctrl.protocol.EDispatchTarget;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.GatewayMsgFactory;
import nuctrl.protocol.GatewayMsgType;

import org.apache.log4j.Logger;

public class DispatchListener {
	
	
	// only for LEFT
	private static final int RETRY_INTERVAL = 1000;
	
	private ServerSocketChannel serverSockChan;
	private SocketChannel sockChan_as_client; // used when isRight is false
	private List<SocketChannel> sockList_as_server = new LinkedList<SocketChannel>(); 
	/*used when isRight is true, designed for multiple connections  */
	
	private IDispatcher cb;
	private Selector sl;
	
	private static Logger log;
	private boolean isRight;
	private boolean helloSent;
	
	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
	private ByteBuffer writeBuffer = ByteBuffer.allocate(8192);
	
	private List<DispatchRequest> dispatchRequests = new LinkedList<DispatchRequest>();
	private Map<SocketChannel, List<ByteBuffer>> peningMsg = 
			new HashMap<SocketChannel, List<ByteBuffer>>();

	public DispatchListener(InetSocketAddress sockAddr, SocketChannel sockChan, IDispatcher cb){
		// init as client end
		this.isRight = false;
		log = Logger.getLogger(Gateway.class.getName());
		this.cb = cb;
		
		// handling connection initialization
		try {
			this.sl = Selector.open();
			
			this.sockChan_as_client = sockChan;
			if (this.sockChan_as_client == null){
				while (true){
					try {
						this.sockChan_as_client = SocketChannel.open(sockAddr);
						break;
					} catch (ConnectException ce){
						log.info(String.format("Left Socket: No Server is on, retry in %ds...", this.RETRY_INTERVAL/1000));
						try {
							Thread.sleep(this.RETRY_INTERVAL);
						} catch (InterruptedException e) {
							e.printStackTrace();
						} 
					} catch (IOException e) {
						e.printStackTrace();
					}
				} // end of while(1)
			}
			Socket sock = sockChan_as_client.socket();

			log.info("Left Connected: " + sockToString(sock));
			
			this.sockChan_as_client.configureBlocking(false);
			this.sockChan_as_client.register(this.sl, SelectionKey.OP_READ);
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


	public DispatchListener(InetSocketAddress sockAddr, ServerSocketChannel ssc, IDispatcher cb){
		// init as server end
		this.isRight = true;
		log = Logger.getLogger(Gateway.class.getName());
		this.cb = cb;
		
		// handling connection initialization
		try {
			this.sl = Selector.open();
			this.serverSockChan = ssc;
			if (this.serverSockChan == null){
				this.serverSockChan = ServerSocketChannel.open();
				this.serverSockChan.configureBlocking(false);
			}
			this.serverSockChan.socket().bind(sockAddr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			this.serverSockChan.register(this.sl, SelectionKey.OP_ACCEPT);
		} catch (ClosedChannelException e) {
			e.printStackTrace();
		}
	}
	


	public void listen(){
		while(true){
//			//XXX DEBUG ONLY
//			SocketChannel debug_sock = null;
//			if (this.isRight)
//				if (!this.sockList_as_server.isEmpty())
//					debug_sock = this.sockList_as_server.get(0);
//				else
//					if (this.sockChan_as_client != null)
//						debug_sock = this.sockChan_as_client;
//
//			if (debug_sock != null){
//				SelectionKey debug_key= debug_sock.keyFor(this.sl);
//				log.info(Dump.key(debug_key));
//			}
//			//XXX
			
			
			
			try {
				//TODO simplify ?
				synchronized(dispatchRequests){
					Iterator changes = this.dispatchRequests.iterator();
					while (changes.hasNext()){
						DispatchRequest change = (DispatchRequest) changes.next();
						switch(change.type){
						case DispatchRequest.CHANGEOPS:
							change.sockChan.keyFor(this.sl).interestOps(change.ops);
						}
					}
					dispatchRequests.clear(); //No any problem
				}
				
				sl.select();
				
				Iterator keys = this.sl.selectedKeys().iterator();
				while(keys.hasNext()){
					SelectionKey key = (SelectionKey) keys.next();
					keys.remove();
					
					if (!key.isValid()){
						log.warn(key.channel().toString() + " issues an invalid key");
						continue;
					}
					
					else if (key.isAcceptable()){
						// accept connection
						ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
						
						SocketChannel sockChan = ssc.accept();
						this.sockList_as_server.add(sockChan);
						
						sockChan.configureBlocking(false);
						sockChan.register(this.sl, SelectionKey.OP_READ);
						Socket sock = sockChan.socket();
					
						log.info("Right Connected: " + this.sockToString(sock));
						
					}
					else if (key.isReadable()){						
						//read the key
						SocketChannel sc = (SocketChannel) key.channel();
						this.readBuffer.clear();
						int numRead;
						numRead = sc.read(this.readBuffer);
						if (numRead == -1){
							key.channel().close();
							key.cancel();
						}
						log.info(this.sockToString(sc.socket()) + " got " + numRead);
						
						this.readBuffer.flip();
						
						if(!helloSent){
							List<GatewayMsg> msgs = GatewayMsgFactory.parseGatewayMsg(readBuffer);
							Iterator iter = msgs.iterator();
							while (iter.hasNext()){
								GatewayMsg msg = (GatewayMsg) iter.next();
								log.info(msg.toString());
								if (msg.getType() == GatewayMsgType.HELLO.getType()){
									//send HELLO_ACK
									GatewayMsg hello_ack = GatewayMsgFactory.getGatewatMsg(GatewayMsgType.HELLO_ACK, (short)2, (short)1);
									ByteBuffer buf = hello_ack.toBuffer();
									buf.flip();
									this.sendToOnePeer(buf);
									this.helloSent = true;
								}
								else
									break;
							}
						}
						else{
							ByteBuffer buf = Buffer.clone(this.readBuffer);
							//deep clone is required
							cb.dispatchDaemon(buf);
						}
					}
					
					else if (key.isWritable()){
						log.debug(key.channel().toString() + " is writable");
						SocketChannel sockChan = (SocketChannel) key.channel();
						
						List queue = this.peningMsg.get(sockChan);
						
						synchronized (this.peningMsg){
							int numWrite = -1;
							while(!queue.isEmpty()){
								ByteBuffer buf = (ByteBuffer) queue.get(0);
								numWrite = sockChan.write(buf);
								
								if (buf.hasRemaining()){
									break; // will be written in next turn, this is a queue!
								}
								queue.remove(0);
							}
							
							if (queue.isEmpty()){
								log.info(numWrite + " bytes has been written to " + sockToString(sockChan.socket()));
								key.interestOps(SelectionKey.OP_READ);
							}
						}
						
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	public int sendToOnePeer(ByteBuffer msg){
		log.debug("SendToOne " + this.toString());
		SocketChannel sockChan;
		if(isRight){
			if (this.sockList_as_server.size() != 1){
				// TODO use exception
				return -1;
			}
			sockChan = this.sockList_as_server.get(0);
		}
		else {
			sockChan = this.sockChan_as_client;
		}
		this.sendToPeer(sockChan, msg);
		return 0;
	}
	
	public void sendToPeer(SocketChannel sock, ByteBuffer msg){
		log.debug("Seng " + this.toString());
		// NOTE asynchronized method from selecting thread
		synchronized (dispatchRequests){
			DispatchRequest dr = new DispatchRequest(sock, 
					DispatchRequest.CHANGEOPS, SelectionKey.OP_WRITE);
			this.dispatchRequests.add(dr);
			
			synchronized (this.peningMsg){
				List<ByteBuffer> queue = this.peningMsg.get(sock);
				if (queue == null){
					queue = new LinkedList<ByteBuffer>();
					this.peningMsg.put(sock, queue);
				}
				// FIXME not thread-safe msg, might be modified while processing by listen
				// better do a copy before adding to list
				queue.add(msg);
			}
		}
		// wake up selector
		this.sl.wakeup();
	}
	
	
	GatewayMsg parseMsg(ByteBuffer buf){
		//TODO: translate from buffer to GatewayMsg
		return null;
	}
	
	@Override
	public String toString(){
		String mode;
		if (this.isRight){
			mode = "Right listener ";
			return mode  
					+ this.serverSockChan.socket();
		}
		else {
			mode = "Left listener ";
			return mode + this.sockChan_as_client.socket();
		}

	}
	
	
	private String sockToString(Socket sock){
		return 
				sock.getLocalAddress().getHostAddress() + ":" + sock.getLocalPort()
				+ " --> "
				+ sock.getInetAddress().getHostAddress() + ":"	+ sock.getPort();
	}
}