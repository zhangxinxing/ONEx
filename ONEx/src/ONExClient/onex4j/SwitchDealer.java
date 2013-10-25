package ONExClient.onex4j;

import org.openflow.protocol.OFFlowMod;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM12:32
 * To change this template use File | ONExSetting | File Templates.
 */
public abstract class SwitchDealer {

    public abstract void getTopo();

    public abstract void sendFlowMod(OFFlowMod ofFlowMod);
}
