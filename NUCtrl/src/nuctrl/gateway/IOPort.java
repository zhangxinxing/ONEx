package nuctrl.gateway;

import nuctrl.datastruct.Buffer;
import nuctrl.protocol.DispatchRequest;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.GatewayMsgFactory;
import nuctrl.protocol.MessageType;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class IOPort {

	/*  Socket */
	protected ServerSocketChannel serverSockChan;
	protected List<SocketChannel> peerSockList = new LinkedList<SocketChannel>(); 
	protected Selector sl;
	protected ByteBuffer readBuffer = ByteBuffer.allocate(8192);
	
	/* data structures */
	protected static Logger log = Logger.getLogger(IOPort.class);
	protected Map<SocketChannel, Integer> helloSent = new HashMap<SocketChannel, Integer>();
	protected List<DispatchRequest> dispatchRequests = new LinkedList<DispatchRequest>();
	protected Map<SocketChannel, List<ByteBuffer>> peningMsg = new HashMap<SocketChannel, List<ByteBuffer>>();
	
	
	public void listen() throws IOException{
		while(true){
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

	protected void accept (SelectionKey key) throws IOException{
		ServerSocketChannel ssc = (ServerSocketChannel) key.channel();

		SocketChannel sockChan = ssc.accept();
		this.peerSockList.add(sockChan);
		this.helloSent.put(sockChan, 0);

		sockChan.configureBlocking(false);
		sockChan.register(this.sl, SelectionKey.OP_READ);
		Socket sock = sockChan.socket();

		log.info("Client connected: " + this.sockToString(sock));
	}



	protected void read(SelectionKey key) throws IOException{
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
				if (msg.getType() == MessageType.HELLO.getType()){

					//send HELLO_ACK
					GatewayMsg hello_ack = GatewayMsgFactory.getGatewatMsg(MessageType.HELLO_ACK, (short)2, (short)1);
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

	protected void write(SelectionKey key) throws IOException{
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



	protected String sockToString(Socket sock){
		return 
				sock.getLocalAddress().getHostAddress() + ":" + sock.getLocalPort()
				+ " --> "
				+ sock.getInetAddress().getHostAddress() + ":"	+ sock.getPort();
	}
}