package ONEx.protocol;


import org.jboss.netty.channel.MessageEvent;

import java.io.Serializable;
import java.net.InetSocketAddress;


public class GatewayMsg implements Serializable{

	/* header */
	private byte type;
    private InetSocketAddress from;
    private MessageEvent event;

	/* attach */
	private byte[] ofm;
	
	//Raw 
	public GatewayMsg(byte type, InetSocketAddress from){
		super();
		this.type = type;
        this.from = from;
        this.event = null;
        this.ofm = null;
	}

    // Message Event
    public void setEvent(MessageEvent event){
        this.event = event;
    }

    public MessageEvent getEvent(){
        return event;
    }

	public void attachOFMessage(byte[] ofMsg){
		if (ofMsg != null){
			this.ofm = ofMsg;
		}
	}

	public byte getType(){
		return this.type;
	}

    public InetSocketAddress getFrom(){
        return this.from;
    }

	public String toString(){
		return String.format("GatewayMsg<Type=%d, From=%s>",
                this.type, this.from.toString());
	}
	
}

