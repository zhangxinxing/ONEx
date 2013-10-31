package ONExBox.gateway;

// STEP build Gateway module first to get familiar with socket as well as onex4j

import ONExBox.BoxDaemon.BoxDaemon;
import ONExBox.ONExSetting;
import ONExBox.gateway.Port.IOClient;
import ONExBox.gateway.Port.IOServer;
import ONExProtocol.GlobalTopo;
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
        BoxDaemon serverDaemon = new BoxDaemon(this, ONExSetting.DAEMON_PORT);
        this.clientPool = new HashMap<InetSocketAddress, IOClient>();
        ioServer = new IOServer(serverDaemon);
        log.info("Gateway Server set up");
    }

    public InetSocketAddress sparePacketIn(ONExPacket op){
        if (op.getINS() != ONExPacket.SPARE_PACKET_IN){
            log.error("The method should only be called on SPARE_PACKET_IN");
            return null;
        }
        // TODO
        InetSocketAddress target = globalShare.getWhoIsIdle().get(0);
        assert target != null;
        log.debug(">>>idle target " + target.toString());
        send(target, op);
        return target;
    }

    public void sendBackPacketIn(ONExPacket op){
        assert op != null;
        assert op.getINS() == ONExPacket.RES_SPARE_PACKET_IN;
        InetSocketAddress src = op.getSrcHost();
        if(src == null){
            log.error("src not set");
        }
        else{
            log.info("sending back to " + op.getSrcHost().toString());
            send(src, op);
        }

    }

    private void send(InetSocketAddress addr, ONExPacket op){
        IOClient client;
        if (clientPool.containsKey(addr)){
            client = clientPool.get(addr);
        }
        else{
            client = new IOClient(addr);
            clientPool.put(addr, client);
        }

        client.send(op);

    }

    public void submitTopology(String db){
        log.debug("submit topology");
        GlobalTopo topo = new GlobalTopo();
        topo.loadFromDB(db);
        globalShare.mergeTopology(topo);
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



