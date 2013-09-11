package nuctrl.core.impl;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;

import nuctrl.core.IF.IDispatcher;
import nuctrl.core.datastruct.Buffer;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.GatewayMsgFactory;
import nuctrl.protocol.GatewayMsgType;

import org.apache.log4j.Logger;

public class IOPortClient extends IOPort{
	// only for LEFT
	private static final int RETRY_INTERVAL = 1000;

	private IDispatcher cb;

	private SocketChannel peerSock;

	public IOPortClient(InetSocketAddress sockAddr, IDispatcher cb){
		// init as client end
		log = Logger.getLogger(Gateway.class.getName());
		this.cb = cb;

		// handling connection initialization
		try {
			this.sl = Selector.open();
			while (true){
				try {
					this.peerSockList.add(SocketChannel.open(sockAddr));
					break;
				} catch (ConnectException ce){
					log.info(String.format("Left Socket: No Server is on, retry in %ds...", IOPortClient.RETRY_INTERVAL/1000));
					try {
						Thread.sleep(IOPortClient.RETRY_INTERVAL);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
				} catch (IOException e) {
					e.printStackTrace();
				}
			} // end of while(1) waiting for host

			this.peerSock = peerSockList.get(0);
			Socket sock = this.peerSock.socket();

			this.helloSent.put(peerSock, 0);
			log.info("Left Connected: " + sockToString(sock));

			this.peerSock.configureBlocking(false);
			this.peerSock.register(this.sl, SelectionKey.OP_READ);

		} catch (IOException e) {
			e.printStackTrace();
		}

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
				if (msg.getType() == GatewayMsgType.HELLO_ACK.getType()){
					this.helloSent.put(sc, 1);
				}
				else
					break;
			}
		}
		else{
			// TODO add dispatcher
			log.info("dispatcher");
			cb.dispatchDaemon(buf2go);
		}
	}

	public int sendToOnePeer(ByteBuffer msg){
		log.debug("SendToOne " + this.toString());
		SocketChannel sockChan = this.peerSock;
		this.sendToPeer(sockChan, msg);
		return 0;
	}

	@Override
	public String toString(){
		String mode;
		mode = "Left listener ";
		return mode + this.peerSock.socket();

	}


}
