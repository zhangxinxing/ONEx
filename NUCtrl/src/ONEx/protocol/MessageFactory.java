package ONEx.protocol;

import org.jboss.netty.channel.MessageEvent;

public class MessageFactory {

    static public GatewayMsg getMessage(MessageEvent event){
        GatewayMsg msg =  (GatewayMsg)event.getMessage();
        msg.setEvent(event);
        return msg;
    }

}
