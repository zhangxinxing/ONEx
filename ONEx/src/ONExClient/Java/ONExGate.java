package ONExClient.Java;

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

    private MessageHandler messageHandler;
    private ONExDaemon onExDaemon;
    private TopologyDealer topologyDealer;
    private SwitchDealer switchDealer;

    public ONExGate(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
        this.switchDealer = new SwitchDealer();
        this.topologyDealer = new TopologyDealer(switchDealer);
        this.onExDaemon = new ONExDaemon(msgHandler, topologyDealer, switchDealer);
        topologyDealer.setDaemon(onExDaemon);
    }

    public void dispatchOFMessage(OFMessage msg){
        switch(msg.getType()){
            case PACKET_IN:
                if (ONExClient.Java.Monitor.isBusy()){
                    onExDaemon.sparePacketIn(msg);
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

