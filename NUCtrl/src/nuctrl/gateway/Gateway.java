package nuctrl.gateway;

// STEP build Gateway module first to get familiar with socket as well as Java

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

    public Gateway() {
        dataSharing = new DataSharing();
        clientPool = new HashMap<InetSocketAddress, IOClient>();
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
            client = new IOClient(addr);
            client.init();
            clientPool.put(addr, client);
        }

        client.send(msg);

    }


    public void setup(){

        log.info("Gateway setting up Server thread");
        IOServer server = new IOServer();

        // initialize server thread
        server.init();

    }

}//end of class



