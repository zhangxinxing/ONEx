package nuctrl.core.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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

import nuctrl.core.datastruct.Buffer;
import nuctrl.protocol.DispatchRequest;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.GatewayMsgFactory;
import nuctrl.protocol.GatewayMsgType;

import org.apache.log4j.Logger;

public class CenterIO {

	private InetSocketAddress sockAddr;

	private ServerSocketChannel serverSockChan;
	private List<SocketChannel> sockList_as_server = new LinkedList<SocketChannel>(); 
	/*used when isRight is true, designed for multiple connections  */

	private Selector sl;

	private static Logger log;
	private Map<SocketChannel, Integer> helloSent;

	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
	private ByteBuffer writeBuffer = ByteBuffer.allocate(8192);

	private List<DispatchRequest> dispatchRequests = new LinkedList<DispatchRequest>();
	private Map<SocketChannel, List<ByteBuffer>> peningMsg = 
			new HashMap<SocketChannel, List<ByteBuffer>>();

	public CenterIO(String ip, int port){
		this.helloSent = new HashMap<SocketChannel, Integer>();
		try {
			InetAddress addr = InetAddress.getByName(ip);
			this.sockAddr = new InetSocketAddress(addr, port);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		
		log = Logger.getLogger(Gateway.class.getName());

		// handling connection initialization
		try {
			this.sl = Selector.open();
			this.serverSockChan = ServerSocketChannel.open();
			this.serverSockChan.configureBlocking(false);
			this.serverSockChan.socket().bind(this.sockAddr);
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
			//				//XXX DEBUG ONLY
			//				SocketChannel debug_sock = null;
			//				if (this.isRight)
			//					if (!this.sockList_as_server.isEmpty())
			//						debug_sock = this.sockList_as_server.get(0);
			//					else
			//						if (this.sockChan_as_client != null)
			//							debug_sock = this.sockChan_as_client;
			//
			//				if (debug_sock != null){
			//					SelectionKey debug_key= debug_sock.keyFor(this.sl);
			//					log.info(Dump.key(debug_key));
			//				}
			//				//XXX

			try {
				//TODO simplify ?
				synchronized(dispatchRequests){
					Iterator<DispatchRequest> changes = this.dispatchRequests.iterator();
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

				Iterator<SelectionKey> keys = this.sl.selectedKeys().iterator();
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
						this.helloSent.put(sockChan, 0);

						sockChan.configureBlocking(false);
						sockChan.register(this.sl, SelectionKey.OP_READ);
						Socket sock = sockChan.socket();

						log.info("Client connected: " + this.sockToString(sock));

					}
					else if (key.isReadable()){
						//read the key
						SocketChannel sc = (SocketChannel) key.channel();

						int numRead;
						numRead = sc.read(this.readBuffer);
						this.readBuffer.flip();

						if (numRead == -1){
							key.channel().close();
							key.cancel();
						}

						ByteBuffer buf2go = Buffer.safeClone(this.readBuffer);
						this.readBuffer.compact();

						log.info(this.sockToString(sc.socket()) + " got " + numRead);
						
						 boolean hello = helloSent.get(sc) == 1 ? true:false;
						
						if(!hello){
							List<GatewayMsg> msgs = GatewayMsgFactory.parseGatewayMsg(buf2go);
							Iterator<GatewayMsg> iter = msgs.iterator();
							while (iter.hasNext()){
								GatewayMsg msg = (GatewayMsg) iter.next();
								log.info(msg.toString());
								if (msg.getType() == GatewayMsgType.HELLO.getType()){
									//send HELLO_ACK
									GatewayMsg hello_ack = GatewayMsgFactory.getGatewatMsg(GatewayMsgType.HELLO_ACK, (short)2, (short)1);
									ByteBuffer buf = hello_ack.toBuffer();
									buf.flip();
									this.sendToPeer(sc, buf);
									
									this.helloSent.put(sc, 1);
								}
								else
									break;
							}
						}
						else{
							log.info("dispatcher");
						}
					}

					else if (key.isWritable()){
						log.debug(key.channel().toString() + " is writable");
						SocketChannel sockChan = (SocketChannel) key.channel();
						List<ByteBuffer> queue = this.peningMsg.get(sockChan);

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

	@Override
	public String toString(){
		String mode;
		mode = "Right listener ";
		return mode  
				+ this.serverSockChan.socket();
	}

	private String sockToString(Socket sock){
		return 
				sock.getLocalAddress().getHostAddress() + ":" + sock.getLocalPort()
				+ " --> "
				+ sock.getInetAddress().getHostAddress() + ":"	+ sock.getPort();
	}
}