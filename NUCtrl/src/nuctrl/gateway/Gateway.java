package nuctrl.gateway;

// STEP build Gateway module first to get familiar with socket as well as Java

import nuctrl.gateway.Port.IOClient;
import nuctrl.gateway.Port.IOServer;
import nuctrl.protocol.GatewayMsg;

import java.net.InetSocketAddress;

public class Gateway {
    private DataSharing dataSharing;

    public Gateway() {
        this.dataSharing = new DataSharing();
    }

    public DataSharing getDataSharing(){
        return dataSharing;
    }

    public void getGlobalInfo(){

        dataSharing.getWhoIsIdle();
        dataSharing.getBusyTableOnline();
    }

    public void send(InetSocketAddress addr, GatewayMsg msg){
        this.send(addr.getAddress().getHostAddress(), addr.getPort(), msg);
    }


    public void send(String host, int port, Object obj){
        IOClient client = new IOClient(host, port);

        client.init();

        client.send(obj);

    }

    public void run(){
        IOServer server = new IOServer();

        // initialize server thread
        server.init();

    }

}//end of class



