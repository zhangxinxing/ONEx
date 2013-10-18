package ONEx.gateway;

import ONEx.Settings;
import ONEx.core.MessageHandler;
import ONEx.protocol.GatewayMsg;
import ONEx.protocol.MessageFactory;
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

        if (Settings.MULTI_THREAD){
            //msgHandler.insert(msg);
            msgHandler.onMessageForThreadPool(msg);
        }
        else {
            msgHandler.packetHandler.onPacket(msg);
        }
    }

}