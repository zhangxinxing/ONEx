package nuctrl.core.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import nuctrl.core.IF.IDispatcher;
import nuctrl.protocol.EDispatchTarget;
import nuctrl.protocol.GatewayMsg;

public class NeighborListener {
	private IDispatcher cb;
	private SelectionKey key;
	
	public NeighborListener(IDispatcher cb, SelectionKey key){
		this.cb = cb;
		this.key = key;
	}
	
	
	public void listen(){
		while(true){
		// running in while(1)
		if (key.isReadable()){
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
			
			cb.dispatchTo(result, msg);
					
			synchronized (output){
				output.clear();
			}
		}}
	}
	
	EDispatchTarget readMsg(GatewayMsg msg){
		return EDispatchTarget.TO_CENTER;
	}
	
	GatewayMsg parseMsg(ByteBuffer buf){
		//TODO: translate from buffer to GatewayMsg
		return null;
	}
}
