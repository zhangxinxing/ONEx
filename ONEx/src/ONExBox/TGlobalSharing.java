package ONExBox;

import ONExBox.gateway.GlobalShare;
import ONExProtocol.GlobalTopo;
import ONExProtocol.Util;
import org.apache.log4j.Logger;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

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

    public static void main(String[] args) throws SigarException {

        ONExSetting.getInstance().setNetworkConfig(7888, 9000);

        GlobalShare globalShare = new GlobalShare(true);
        GlobalTopo topo = new GlobalTopo();
        Sigar sigar = new Sigar();
        long pid = sigar.getPid();
        System.err.println("Pid:" + pid);
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
            if (count.equals("go")){
                System.out.println("# Timestamp(rel,%) Size(KB) Duration(ms) CPU(%) Mem(MB)");
                Long start = System.currentTimeMillis();
                for (int i = 0; i < 100000; i+= 1000){
                    Long totalSize = (long)8 + 2 +4 + 8;
                    Long begin = System.currentTimeMillis();

                    Long baseMem = sigar.getMem().getActualUsed();
                    gen(i, topo, globalShare);
                    Long end = System.currentTimeMillis();
                    System.out.println(String.format(
                            "%d, %1.2f, %d, %1.4f, %1.2f",
                            end - start,
                            totalSize*topo.getHostEntrySet().size()/1024.0,
                            end-begin,
                            sigar.getCpuPerc().getCombined(),
                            sigar.getProcMem(pid).getResident()/1024.0/1024.0
                    ));

                }
                System.exit(0);
            }
            System.out.println("Generating " + count + " entries");
            long countL = Long.parseLong(count);
            gen(countL, topo, globalShare);
        }
    }

    private static void gen(long countL, GlobalTopo topo, GlobalShare globalShare){
        long macStart = 0;
        for (long mac = macStart; mac < macStart + countL; mac ++){
            topo.addHost(
                    new Random(System.currentTimeMillis()).nextLong(),
                    (short)new Random(System.currentTimeMillis()).nextInt(100),
                    new Random(System.currentTimeMillis()).nextInt(),
                    Util.longToMAC(mac));
        }
        macStart += countL;

        globalShare.mergeTopologyTest(topo);





    }
}
