package nuctrl.core;

import nuctrl.gateway.Gateway;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.MessageType;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.List;


public class Core {
    private CoreDispatcher dispatcher;
    private MessageHandler messageHandler;
    private static Logger log = Logger.getLogger(Core.class);

	public Core(Gateway gateway, MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
        this.dispatcher = new CoreDispatcher(gateway, messageHandler);
	}

    public void dispatchFunc(GatewayMsg msg){
        dispatcher.dispatchFunc(msg);
    }

}


interface dispatcherCallback {
    public void dispatchFunc(GatewayMsg msg);
}


class CoreDispatcher implements dispatcherCallback {
    private MessageHandler messageHandler;
    private Gateway gateway;
    private static Logger log = Logger.getLogger(CoreDispatcher.class);

    public CoreDispatcher(Gateway gateway, MessageHandler msgHandler) {
        this.gateway = gateway;
        if (msgHandler == null){
            log.error("null msgHandler is not allowed");
            System.exit(-1);
        }
        this.messageHandler = msgHandler;
        new Thread(messageHandler).start();
    }

    @Override
    public void dispatchFunc(GatewayMsg msg){
        // TODO use enumeration and switch here
        // if pkt-in comes
        if (msg.getType() == MessageType.PACKET_IN.getType()){
            log.debug("[Core] dispatching PktIn");
            onPacketIn(msg);
        }

        // if pkt-out comes
        else if (msg.getType() == MessageType.PACKET_OUT.getType()){
            log.debug("[Core] dispatching PktOut");
            onPacketOut(msg);
        }
    }

    private void onPacketIn(GatewayMsg msg) {
        if (Monitor.isBusy()){
            // dispatcher to gateway
            List<InetSocketAddress> idleList =
                    gateway.getDataSharing().getWhoIsIdle();
            gateway.send(idleList.get(0), msg);
        }

        else{
            // handle it
            messageHandler.insert(msg);
        }
    }

    private void onPacketOut(GatewayMsg msg) {
        messageHandler.insert(msg);
    }
}