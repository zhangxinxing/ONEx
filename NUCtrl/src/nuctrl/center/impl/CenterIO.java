package nuctrl.center.impl;

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
import nuctrl.core.impl.Gateway;
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
	private CenterDispatcher cb;

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
						this.accept(key);

					}
					else if (key.isReadable()){
						//read the key
						this.read(key);
					}

					else if (key.isWritable()){
						this.write(key);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void sendToPeer(SocketChannel sock, ByteBuffer msg){
		log.debug("Send " + this.toString());
		// NOTE unsynchronized method from selecting thread
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

	private void read(SelectionKey key) throws IOException{
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

		log.debug(this.sockToString(sc.socket()) + " got " + numRead);

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
					this.sendToPeer(sc, (ByteBuffer) hello_ack.toBuffer().flip());
					this.helloSent.put(sc, 1);
				}
				else
					// TODO
					break;
			}
		}
		else{
			// TODO add dispatcher
			log.info("dispatcher");
		}
	}

	private void write(SelectionKey key) throws IOException{
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
				log.debug(numWrite + " bytes has been written to " + sockToString(sockChan.socket()));
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}



	private void accept (SelectionKey key) throws IOException{
		ServerSocketChannel ssc = (ServerSocketChannel) key.channel();

		SocketChannel sockChan = ssc.accept();
		this.sockList_as_server.add(sockChan);
		this.helloSent.put(sockChan, 0);

		sockChan.configureBlocking(false);
		sockChan.register(this.sl, SelectionKey.OP_READ);
		Socket sock = sockChan.socket();

		log.info("Client connected: " + this.sockToString(sock));
	}

	@Override
	public String toString(){
		String mode;
		mode = "Center I/O Server ";
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