package nuctrl.protocol;

import java.nio.channels.SocketChannel;

public class DispatchRequest {
	  public static final int REGISTER = 1;
	  public static final int CHANGEOPS = 2;
	  
	  public SocketChannel sockChan;
	  public int type;
	  public int ops;
	  
	  public DispatchRequest(SocketChannel socket, int type, int ops) {
	    this.sockChan = socket;
	    this.type = type;
	    this.ops = ops;
	  }
	}