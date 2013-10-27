package ONExBox.gateway;

import ONExProtocol.GlobalTopo;
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
        GlobalShare globalShare = new GlobalShare();


        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        GlobalTopo globalTopo = new GlobalTopo();
        globalTopo.addHostEntry(123L, (short)123, 123, new byte[]{1,2,3,4,5,6});
        globalTopo.addSwitchLink(1L, (short)1, 2L, (short)2);

        globalShare.mergeTopology(globalTopo);

        GlobalTopo global = globalShare.getGlobalTopo();

        log.info(global.toString());

        globalShare.shutdown();



    }
}
