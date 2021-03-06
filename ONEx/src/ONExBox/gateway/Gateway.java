package ONExBox.gateway;

// STEP build Gateway module first to get familiar with socket as well as onex4j

import ONExBox.BoxDaemon.BoxDaemon;
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

    public Gateway(int daemonPort, int serverPort) {
        BoxDaemon ONExDaemon = new BoxDaemon(this, daemonPort);
        this.clientPool = new HashMap<InetSocketAddress, IOClient>();
        this.globalShare = new GlobalShare(false);
        ioServer = new IOServer(ONExDaemon, serverPort);
        log.info("Gateway Server set up");
    }

    public void setControllerID(Long ID) {
        globalShare.setControllerID(ID);
    }

    public InetSocketAddress sparePacketIn(ONExPacket op) {
        if (op.getINS() != ONExPacket.SPARE_PACKET_IN) {
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

    public void sendBackPacketIn(ONExPacket op) {
        assert op != null;
        assert op.getINS() == ONExPacket.RES_SPARE_PACKET_IN;
        InetSocketAddress src = op.getSrcHost();
        if (src == null) {
            log.error("src not set");
        } else {
            log.info("send handled PacketIn back to " + op.getSrcHost().toString());
            send(src, op);
        }

    }

    private void send(InetSocketAddress addr, ONExPacket op) {
        IOClient client;
        if (clientPool.containsKey(addr)) {
            client = clientPool.get(addr);
        } else {
            client = new IOClient(addr);
            clientPool.put(addr, client);
        }

        client.send(op);

    }

    public void submitTopology(String db) {
        GlobalTopo topo = new GlobalTopo();
        topo.loadFromDB(db);
        log.debug("complete loading: " + topo.toString());
        globalShare.mergeTopology(topo);
    }

    public void halfShutdown() {
        for (IOClient client : this.clientPool.values()) {
            client.destroy();
        }
    }

    public void fullShutdown() {
        globalShare.shutdown();
        halfShutdown();
        ioServer.destroy();
    }

}//end of class



