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
import nuctrl.protocol.DispatchRequest;
import nuctrl.protocol.EDispatchTarget;
import nuctrl.protocol.GatewayMsg;

import org.apache.log4j.Logger;

public class DispatchListener {
	
	private ServerSocketChannel serverSockChan;
	private SocketChannel sockChan_as_client; // used when isRight is false
	private List<SocketChannel> sockList_as_server = new LinkedList<SocketChannel>(); 
	/*used when isRight is true, designed for multiple connections  */
	
	private IDispatcher cb;
	private Selector sl;
	
	private static Logger log;
	private boolean isRight;
	
	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
	
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
						log.info("Left Socket: No Server is on, retry in 5s...");
						try {
							Thread.sleep(5000);//5 second
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
	


	public int sentToOne(ByteBuffer msg){
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
		this.send(sockChan, msg);
		return 0;
	}
	
	public void send(SocketChannel sock, ByteBuffer msg){
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
				
				queue.add(msg);
			}
		}
		// wake up selector
		this.sl.wakeup();
	}
	
	
	public void listen(){
		while(true){
			try {
				
				synchronized(dispatchRequests){
					Iterator changes = this.dispatchRequests.iterator();
					while (changes.hasNext()){
						DispatchRequest change = (DispatchRequest) changes.next();
						switch(change.type){
						case DispatchRequest.CHANGEOPS:
							change.sockChan.keyFor(this.sl).interestOps(change.ops);
						}
					}
					dispatchRequests.clear();
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
						log.info(this.toString() + " is readable");
						SocketChannel sc = (SocketChannel) key.channel();
						this.readBuffer.clear();
						int numRead;
						numRead = sc.read(this.readBuffer);
						if (numRead == -1){
							key.channel().close();
							key.cancel();
						}
						// FIXME set limit before send out the readBuffer
						// maybe more efficient after using GatewayMsg
						cb.dispatchDaemon(this.readBuffer);
						
					}
					
					else if (key.isWritable()){
						log.debug(key.channel().toString() + " is writable");
						SocketChannel sockChan = (SocketChannel) key.channel();
						
						List queue = this.peningMsg.get(sockChan);
						
						synchronized (this.peningMsg){
							while(!queue.isEmpty()){
								ByteBuffer buf = (ByteBuffer) queue.get(0);
								sockChan.write(buf);
								
								if (buf.hasRemaining()){
									break; // will be written in next turn
								}
								queue.remove(0);
							}
							
							if (queue.isEmpty()){
								log.debug("All data in queue has been written to socket");
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

	
	EDispatchTarget readMsg(GatewayMsg msg){
		return EDispatchTarget.TO_CENTER;
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