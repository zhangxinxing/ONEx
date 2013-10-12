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
    private DataSharing dataSharing;
    private Map<InetSocketAddress, IOClient> clientPool;
    private MessageHandler messageHandler;

    public Gateway(MessageHandler msgHandler) {
        if (msgHandler == null){
            log.error("Null past to constructor");
            System.exit(-1);
        }
        this.messageHandler = msgHandler;
        this.dataSharing = new DataSharing();
        this.clientPool = new HashMap<InetSocketAddress, IOClient>();
    }

    public DataSharing getDataSharing(){
        return dataSharing;
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


    public void setup(){

        log.info("Gateway setting up Server thread");
        IOServer server = new IOServer(messageHandler);

        // initialize server thread
        server.init();

    }

}//end of class



