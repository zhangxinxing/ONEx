package ONEx.test;


import org.apache.log4j.Logger;
import org.hyperic.sigar.NetFlags;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarLoader;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-12
 * Time: PM5:04
 */

public class TSigar  {

    static Logger log = Logger.getLogger(TSigar.class);

    public static void main(String[] args){
        Sigar sigar = new Sigar();

        try {

            // cpu
            log.info(iter(sigar.getCpuList()));
            log.info(iter(sigar.getCpuInfoList()));
            log.info(iter(sigar.getCpuPercList()));
            log.info(sigar.getCpuPerc().getCombined());

            log.info(sigar.getMem());

            // process
            long pid = sigar.getPid();
            //log.info(iter(sigar.getProcList()));
            log.info(sigar.getProcCpu(pid));
            log.info(sigar.getProcMem(pid));
            log.info(sigar.getProcEnv(pid));

            if (SigarLoader.IS_LINUX){
                log.info(sigar.getProcPort(NetFlags.CONN_TCP, (int)pid));
                log.info(sigar.getProcExe(pid));
                log.info(sigar.getProcFd(pid));
            }
            log.info(sigar.getProcState(pid));
            log.info(sigar.getProcTime(pid));


            //network
            log.info(sigar.getNetInfo());
            log.info(iter(sigar.getNetInterfaceList()));
            log.info((sigar.getNetInterfaceConfig("en0")));
            log.info(sigar.getNetStat().toString());

            //thread
            log.info(sigar.getThreadCpu());


        } catch (SigarException e) {
            e.printStackTrace();
        }
    }

    private static String iter(Object object){
        String r = "\n>>>\n";
        if (object instanceof Object[]){
            Object[] obj = (Object []) object;
            for ( int i = 0; i < obj.length; i++){
                r += obj[i].toString() + '\n';
            }
        }
        else if (object instanceof long[]){
            r += Arrays.toString((long [])object);
        }
        r += ">>>\n";
        return r;
    }
}

