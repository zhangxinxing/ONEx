package ONExBox;

import ONExBox.gateway.GlobalShare;
import ONExProtocol.GlobalTopo;
import ONExProtocol.Util;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-27
 * Time: PM9:28
 */
public class TGlobalSharing {

    private static Logger log = Logger.getLogger(TGlobalSharing.class);

    public static void main(String[] args){

        ONExSetting.getInstance().setNetworkConfig(7888, 9000);

        GlobalShare globalShare = new GlobalShare(true);

        GlobalTopo topo = new GlobalTopo();

        for (long mac = 1; mac < 100; mac ++){
            topo.addHost(1L, (short)1, 123, Util.longToMAC(mac));
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Begin");
        globalShare.mergeTopologyTest(topo);

    }
}
