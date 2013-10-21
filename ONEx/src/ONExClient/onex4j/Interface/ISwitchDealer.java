package ONExClient.onex4j.Interface;

import org.openflow.protocol.OFFlowMod;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM12:32
 * To change this template use File | ONExSetting | File Templates.
 */
public interface ISwitchDealer {
    void getTopo();

    void sendFlowMod(OFFlowMod off);
}
