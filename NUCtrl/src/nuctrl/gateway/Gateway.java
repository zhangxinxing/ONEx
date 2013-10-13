package nuctrl.gateway;

// STEP build Gateway module first to get familiar with socket as well as Java

import nuctrl.core.MessageHandler;
import nuctrl.gateway.Port.IOClient;
import nuctrl.gateway.Port.IOServer;
import nuctrl.protocol.GatewayMsg;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Gateway {
    private static Logger log = Logger.getLogger(Gateway.class);
    private GlobalShare globalShare;
    private Thread globalShareThread;

    private IOServer ioServer;
    private Map<InetSocketAddress, IOClient> clientPool;
    private MessageHandler messageHandler;


    public Gateway(MessageHandler msgHandler) {
        if (msgHandler == null){
            log.error("Null past to constructor");
            System.exit(-1);
        }
        this.messageHandler = msgHandler;
        this.globalShare = new GlobalShare();
        this.globalShareThread = new Thread(globalShare);
        globalShareThread.start();

        this.clientPool = new HashMap<InetSocketAddress, IOClient>();
        ioServer = new IOServer(messageHandler);
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
            client = new IOClient(addr, messageHandler);
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
        globalShare.terminate();
        try {
            globalShareThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        globalShare.shutdown();
        halfShutdown();
        ioServer.destroy();
    }

}//end of class



