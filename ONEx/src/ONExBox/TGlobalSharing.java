package ONExBox;

import ONExBox.gateway.GlobalShare;
import ONExProtocol.GlobalTopo;
import ONExProtocol.Util;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

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
        long macStart = 0;
        while(true){
            System.out.print("Test Counts: ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String count = null;
            try {
                count = br.readLine();
            } catch (IOException ioe) {
                System.out.println("IO error trying to read your name!");
                break;
            }
            if (count.equals("")){
                break;
            }
            System.out.println("Generating " + count + " entries");
            long countL = Long.parseLong(count);
            for (long mac = macStart; mac < macStart + countL; mac ++){
                topo.addHost(
                        new Random(System.currentTimeMillis()).nextLong(),
                        (short)new Random(System.currentTimeMillis()).nextInt(100),
                        new Random(System.currentTimeMillis()).nextInt(),
                        Util.longToMAC(mac));
            }
            macStart += countL + 1;
            Long totalSize = (8 + 2 +4 + 8)*countL;
            System.out.println("Begin! (total size=" + totalSize + "B)");
            globalShare.mergeTopologyTest(topo);
        }
    }
}
