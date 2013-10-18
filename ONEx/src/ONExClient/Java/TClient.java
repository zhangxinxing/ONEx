package ONExClient.Java;

import org.openflow.protocol.OFPacketIn;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM12:41
 */
public class TClient {
    public static void main(String[] args){
        System.out.println("Hello world");
        MessageHandler msg_h = new MessageHandler();
        ONExGate onExGate = new ONExGate(msg_h);

        byte[] ba = {};
        OFPacketIn pi = new OFPacketIn();
        pi.setReason(OFPacketIn.OFPacketInReason.NO_MATCH);
        pi.setPacketData(ba);
        onExGate.dispatchOFMessage(pi);
    }
}
