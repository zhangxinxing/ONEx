package nuctrl.core.impl;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import nuctrl.core.IF.IDispatcher;
import nuctrl.protocol.EDispatchTarget;
import nuctrl.protocol.GatewayMsg;

import org.apache.log4j.Logger;

public class NeighborListener {
	private IDispatcher cb;
	private Selector sl;
	private static Logger log;
	Iterator keys;
	SelectionKey key;
	ReentrantLock lockOnSel;
	
	public NeighborListener(IDispatcher cb, Selector sl, ReentrantLock lock){
		this.cb = cb;
		this.sl = sl;
		this.lockOnSel = lock;
		log = Logger.getLogger(Gateway.class.getName());
	}
	
	
	public void listen(){
		log.info("Listening");
		
		while(true){
			this.lockOnSel.lock();
			this.lockOnSel.unlock();
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			try {
				log.trace("test for block");
				sl.select();
				log.trace("block failure");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			keys = sl.selectedKeys().iterator();
			
			while(keys.hasNext()){
				key = (SelectionKey) keys.next();
				keys.remove();
			
			
				SocketChannel soc = (SocketChannel)key.channel();
				Socket sc = soc.socket();
				if (key.isReadable()){
					log.info(sc.toString() + "is readable");
					SocketChannel client = (SocketChannel)key.channel();
					ByteBuffer output = (ByteBuffer) key.attachment();
					GatewayMsg msg = null;
					synchronized(output){
						try {
							client.read(output);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						// TODO change into list?
						msg = this.parseMsg(output);
					} // end of synchronize on output
					
					//dispatch
					EDispatchTarget result = readMsg(msg);
					
					//cb.dispatchTo(result, msg);
					
					synchronized (output){
						output.clear();
					}
				}
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
}
