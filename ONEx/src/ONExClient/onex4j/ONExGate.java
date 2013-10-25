package ONExClient.onex4j;

import ONExClient.onex4j.SDKDaemon.SDKDaemon;
import org.apache.log4j.Logger;
import org.openflow.protocol.OFMessage;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-18
 * Time: PM11:27
 */
public class ONExGate {
    private static Logger log = Logger.getLogger(ONExGate.class);
    public static int ID;

    private MessageHandler messageHandler;
    private SDKDaemon SDKDaemon;

    /*
        ONExGate is the entrance of ONEx
     */
    public ONExGate(MessageHandler msgHandler, TopologyDealer topo, SwitchDealer swDealer, int port) {
        this.messageHandler = msgHandler;
        this.SDKDaemon = new SDKDaemon(msgHandler, port, topo, swDealer);
    }

    public void dispatchOFMessage(OFMessage msg){
        switch(msg.getType()){
            case PACKET_IN:
                if (ONExClient.onex4j.Monitor.isBusy()){
                    SDKDaemon.sparePacketIn(msg);
                }
                else{
                    messageHandler.onLocalPacketIn(msg);
                }
                break;
            default:
                log.error("error in default");
                System.exit(-1);
        }
    }

    @Override
    public String toString(){
        return "ONExGate";
    }

}

