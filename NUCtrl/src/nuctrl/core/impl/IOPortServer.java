package nuctrl.core.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;

import nuctrl.core.IF.IDispatcher;
import nuctrl.core.datastruct.Buffer;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.GatewayMsgFactory;
import nuctrl.protocol.GatewayMsgType;

import org.apache.log4j.Logger;

public class IOPortServer extends IOPort{

	private IDispatcher cb;

	public IOPortServer(InetSocketAddress sockAddr, IDispatcher cb){
		// init as server end
		log = Logger.getLogger(Gateway.class.getName());
		this.cb = cb;

		// handling connection initialization
		try {
			this.sl = Selector.open();
			this.serverSockChan = ServerSocketChannel.open();
			this.serverSockChan.configureBlocking(false);
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
				if (msg.getType() == GatewayMsgType.HELLO.getType()){

					//send HELLO_ACK
					GatewayMsg hello_ack = GatewayMsgFactory.getGatewatMsg(GatewayMsgType.HELLO_ACK, (short)2, (short)1);
					this.sendToPeer(sc, (ByteBuffer) hello_ack.toBuffer().flip());
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


	protected int sendToOnePeer(ByteBuffer msg){
		log.debug("SendToOne " + this.toString());
		SocketChannel sockChan;
		if (this.peerSockList.size() != 1){
			// TODO use exception
			return -1;
		}
		sockChan = this.peerSockList.get(0);
		this.sendToPeer(sockChan, msg);
		return 0;
	}



	@Override
	public String toString(){
		String mode;
		mode = "Right listener ";
		return mode + this.sockToString(this.peerSockList.get(0).socket());
	}

}
