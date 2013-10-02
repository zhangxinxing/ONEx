package nuctrl.interfaces;

import java.nio.ByteBuffer;

public interface IDispatcher {
	
	void dispatchDaemon(ByteBuffer msg);
	
	void dispatch();
	
}
