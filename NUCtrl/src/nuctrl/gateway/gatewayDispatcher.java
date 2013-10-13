package nuctrl.gateway;

import nuctrl.Settings;
import nuctrl.core.MessageHandler;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.MessageFactory;
import nuctrl.protocol.MessageType;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.MessageEvent;

/**
 * User: fan
 * Date: 13-10-9
 * Time: PM3:39
 */
public class gatewayDispatcher {

    private MessageHandler msgHandler;
    private static Logger log = Logger.getLogger(gatewayDispatcher.class);

    public gatewayDispatcher(MessageHandler msgHandler) {
        if (msgHandler != null){
            this.msgHandler = msgHandler;
        }
        else {
            log.error("Null msgHandler past to gatewayDispatcher");
            System.exit(-1);
        }
    }

    public void dispatchFunc(MessageEvent event) {

        GatewayMsg msg = MessageFactory.getMessage(event);

        switch(MessageType.fromByte(msg.getType())){
            case PACKET_IN:
                if (Settings.MULTI_THREAD){
                    msgHandler.insert(msg);
                }
                else {
                    msgHandler.packetHandler.onPacket(msg);
                }
                break;

            case PACKET_OUT:
                if (Settings.MULTI_THREAD){
                    msgHandler.insert(msg);
                }
                else {
                    msgHandler.packetHandler.onPacket(msg);
                }
                break;
        }

    }
}