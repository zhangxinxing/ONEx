package ONExBox.gateway;

// STEP build Gateway module first to get familiar with socket as well as onex4j

import ONExBox.Sharing.GlobalShare;
import ONExClient.onex4j.MessageHandler;
import ONExBox.gateway.Port.IOClient;
import ONExBox.gateway.Port.IOServer;
import ONExProtocol.ONExPacket;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Gateway {
    private static Logger log = Logger.getLogger(Gateway.class);
    private GlobalShare globalShare;

    private IOServer ioServer;
    private Map<InetSocketAddress, IOClient> clientPool;

    public Gateway() {
        this.globalShare = new GlobalShare();

        this.clientPool = new HashMap<InetSocketAddress, IOClient>();
        ioServer = new IOServer();
        ioServer.init();
        log.info("Gateway Server set up");
    }

    public GlobalShare getGlobalShare(){
        return globalShare;
    }

    public InetSocketAddress sparePacketIn(ONExPacket op){
        if (op.getINS() != ONExPacket.SPARE_PACKET_IN){
            log.error("The method should only be called on SPARE_PACKET_IN");
            return null;
        }
        // TODO
        InetSocketAddress target = globalShare.getWhoIsIdle().get(0);
        assert target != null;
        log.debug("idle target " + target.toString());
        send(target, op);
        return target;
    }

    public void sendBackPacketIn(ONExPacket op){
        log.info("Send packet back to " + op.getSrcHost().toString());
        // ADD src field in OPSpearPacketIN

    }

    private void send(InetSocketAddress addr, ONExPacket op){
        IOClient client;
        if (clientPool.containsKey(addr)){
            client = clientPool.get(addr);
        }
        else{
            client = new IOClient(addr);
            client.init();
            clientPool.put(addr, client);
        }

        client.send(op);

    }

    public void halfShutdown(){
        for (IOClient client : this.clientPool.values()){
            client.destroy();
        }
    }

    public void fullShutdown(){
        globalShare.shutdown();
        halfShutdown();
        ioServer.destroy();
    }

}//end of class



