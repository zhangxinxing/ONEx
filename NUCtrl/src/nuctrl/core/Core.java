package nuctrl.core;

import nuctrl.gateway.Gateway;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.MessageType;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.List;


public class Core {
    private CoreDispatcher dispatcher;
    private static Logger log = Logger.getLogger(Core.class);

	public Core(Gateway gateway) {
        dispatcher = new CoreDispatcher(gateway);
	}


    public void dispatchFunc(GatewayMsg msg){
        dispatcher.dispatchFunc(msg);
    }

}


interface dispatcherCallback {
    public void dispatchFunc(GatewayMsg msg);
}


class CoreDispatcher implements dispatcherCallback {
    private InHandler inHandler;
    private OutHandler outHandler;
    private Gateway gateway;
    private static Logger log = Logger.getLogger(CoreDispatcher.class);

    public CoreDispatcher(Gateway gateway) {
        this.gateway = gateway;
        this.inHandler = new InHandler();
        this.outHandler = new OutHandler();

        // start the worker thread
        new Thread(inHandler).start();
        new Thread(outHandler).start();
    }

    @Override
    public void dispatchFunc(GatewayMsg msg){

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
        if (Monitor.isCpuBusy()){
            // dispatcher to gateway
            List<InetSocketAddress> idleLit =
                    gateway.getDataSharing().getWhoIsIdle();
            gateway.send(idleLit.get(0), msg);
        }

        else{
            // handle it
            inHandler.insert(msg);
        }
    }

    private void onPacketOut(GatewayMsg msg) {
        outHandler.insert(msg);
    }
}