package nuctrl.gateway.Port;

import nuctrl.core.InHandler;
import nuctrl.core.OutHandler;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.MessageFactory;
import nuctrl.protocol.MessageType;
import org.jboss.netty.channel.MessageEvent;

/**
 * User: fan
 * Date: 13-10-9
 * Time: PM3:39
 */
class gatewayDispatcher {

    private InHandler inHandler;
    private OutHandler outHandler;

    gatewayDispatcher() {
        this.inHandler = new InHandler();
        this.outHandler = new OutHandler();
    }

    public void dispatchFunc(MessageEvent event) {

        GatewayMsg msg = MessageFactory.getMessage(event);

        if (msg.getType() == MessageType.PACKET_IN.getType()){
            inHandler.insert(msg);
        }
        else if (msg.getType() == MessageType.PACKET_OUT.getType()){
            outHandler.insert(msg);
        }

    }
}