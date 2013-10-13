package nuctrl.core;

import nuctrl.Settings;
import nuctrl.gateway.Gateway;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.MessageType;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.List;


public class Core {
    private MessageHandler messageHandler;
    private Gateway gateway;
    private static Logger log = Logger.getLogger(Core.class);

	public Core(Gateway gateway, MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
        this.gateway = gateway;
        if (Settings.MULTI_THREAD){
            /*
                if in multiple threads mode, open new thread for message handler
             */
            new Thread(messageHandler).start();
        }
	}

    public void dispatchFunc(GatewayMsg msg){
        // TODO use enumeration and switch here
        // if pkt-in comes
        if (msg.getType() == MessageType.PACKET_IN.getType()){
            log.debug("[Core] dispatching PktIn");
            if (Monitor.getInstance().isBusy()){
                // dispatcher to gateway
                List<InetSocketAddress> idleList = gateway.getGlobalShare().getWhoIsIdle();
                gateway.send(idleList.get(0), msg);
            }
            else{
                if (Settings.MULTI_THREAD){
                    messageHandler.insert(msg);
                }
                else {
                    messageHandler.packetHandler.onPacket(msg);
                }
            }
        }
        // if pkt-out comes
        else if (msg.getType() == MessageType.PACKET_OUT.getType()){
            log.debug("[Core] dispatching PktOut");
            if (Settings.MULTI_THREAD){
                messageHandler.insert(msg);
            }
            else {
                messageHandler.packetHandler.onPacket(msg);
            }
        }
    }

}

