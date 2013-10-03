package nuctrl.center.impl;

import nuctrl.datastruct.Buffer;
import nuctrl.gateway.IOPort;
import nuctrl.interfaces.IDispatcher;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.GatewayMsgFactory;
import nuctrl.protocol.GatewayMsgType;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.List;

public class CenterIOPort extends IOPort{
	/*
	 * 
	protected ServerSocketChannel serverSockChan;
	protected List<SocketChannel> peerSockList = new LinkedList<SocketChannel>(); 
	protected Selector sl;
	protected ByteBuffer readBuffer = ByteBuffer.allocate(8192);
	
	protected static Logger log = Logger.getLogger(IOPort.class);
	protected Map<SocketChannel, Integer> helloSent = new HashMap<SocketChannel, Integer>();
	protected List<DispatchRequest> dispatchRequests = new LinkedList<DispatchRequest>();
	protected Map<SocketChannel, List<ByteBuffer>> p;
	 */

	private static InetSocketAddress sockAddr;
	private IDispatcher cb;
	
	public CenterIOPort(String ip, int port, IDispatcher cb){
		/* initialize fields */
		this.cb = cb;
		
		
		try {
			InetAddress addr = InetAddress.getByName(ip);
			CenterIOPort.sockAddr = new InetSocketAddress(addr, port);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}

		// handling connection initialization
		try {
			this.sl = Selector.open();
			this.serverSockChan = ServerSocketChannel.open();
			this.serverSockChan.configureBlocking(false);
			this.serverSockChan.socket().bind(CenterIOPort.sockAddr);
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
					// TODO
					break;
			}
		}
		else{
			// TODO add dispatcher
			log.info("dispatcher");
		}
	}

	@Override
	public String toString(){
		String mode;
		mode = "Center I/O Server ";
		return mode  
				+ this.serverSockChan.socket();
	}
}