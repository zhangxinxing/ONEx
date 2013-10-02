package nuctrl.debug;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class Dump {
	static public String buf(ByteBuffer buf){
		return String.format(
				"Buf <p>=%d <limit>=%d <cap>=%d <len>=%d",
				buf.position(),
				buf.limit(),
				buf.capacity(),
				buf.limit() - buf.position()
				);
	}
	
	static public String key(SelectionKey key){
		return String.format("Key <i>=%d <r>=%b <w>=%b", 
				key.interestOps(),
				key.isReadable(),  
				key.isWritable()
				);
	}
}
