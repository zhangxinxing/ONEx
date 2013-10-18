package ONEx.gateway;

// STEP build Gateway module first to get familiar with socket as well as Java

import ONEx.Sharing.GlobalShare;
import ONExClient.Java.MessageHandler;
import ONEx.gateway.Port.IOClient;
import ONEx.gateway.Port.IOServer;
import ONEx.protocol.GatewayMsg;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Gateway {
    private static Logger log = Logger.getLogger(Gateway.class);
    private GlobalShare globalShare;

    private IOServer ioServer;
    private Map<InetSocketAddress, IOClient> clientPool;
    private MessageHandler packetHandler;


    public Gateway(MessageHandler msgHandler) {
        if (msgHandler == null){
            log.error("Null past to constructor");
            System.exit(-1);
        }
        this.packetHandler = msgHandler;
        this.globalShare = new GlobalShare();

        this.clientPool = new HashMap<InetSocketAddress, IOClient>();
        ioServer = new IOServer(packetHandler);
        ioServer.init();
        log.info("Gateway Server set up");
    }

    public GlobalShare getGlobalShare(){
        return globalShare;
    }


    public void send(InetSocketAddress addr, GatewayMsg msg){
        IOClient client;
        if (clientPool.containsKey(addr)){
            client = clientPool.get(addr);
        }
        else{
            client = new IOClient(addr, packetHandler);
            client.init();
            clientPool.put(addr, client);
        }

        client.send(msg);

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



