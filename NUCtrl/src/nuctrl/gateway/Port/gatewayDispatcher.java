package nuctrl.gateway.Port;

import nuctrl.core.InHandler;
import nuctrl.core.OutHandler;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.MessageFactory;
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
        // if in
        inHandler.insert(MessageFactory.getMessage(event));

        // if out
        outHandler.insert(MessageFactory.getMessage(event));
    }
}