package ONExBox;

import org.apache.log4j.Logger;
import org.hyperic.sigar.*;

import java.util.*;


public class Monitor {
    // data structure
    private static Sigar sigar = new Sigar();
    private static Logger log = Logger.getLogger(Monitor.class);

    /* data */
    private Map<String, Integer> CPUAccount;
    private int sizeOfQueueIn;
    private SysInfo sysInfo;
    private ProcInfo procInfo;

    // singleton
    private static final Monitor instance = new Monitor();

    // private constructor
    private Monitor(){
        this.CPUAccount = new HashMap<String, Integer>();
        this.sizeOfQueueIn = 0;
        this.sysInfo = new SysInfo();
        this.procInfo = new ProcInfo();
    }

    public static Monitor getInstance(){
        return instance;
    }

    // getCPUAccount
    public Map<String, Integer> getCPUAccount() {
        List<String> apps = ONExSetting.getInstance().appNameList;
        for (String app : apps){
            if (ONExSetting.RANDOM_TEST){
                CPUAccount.put(app, new Random().nextInt());
            }
            else {
                long pid = ONExSetting.getInstance().appPid.get(app);
                // TODO sigar.getProcCpu(pid).getPercent();
            }
        }
        return CPUAccount;
    }

    // get Account by name
    public int getCpuAccountByName(String appName) {

        if (ONExSetting.RANDOM_TEST){
            return new Random().nextInt();
        }
        else {
            return getCPUAccount().get(appName);
        }
    }

    // get local info
    private void update(){
        try {
            sysInfo.nCPU = sigar.getCpuList().length;
            sysInfo.CPUInfo = sigar.getCpuInfoList();
            sysInfo.CPU_idle = sigar.getCpuPerc().getIdle();
            sysInfo.mem_free = sigar.getMem().getFreePercent();

            long pid = ONExSetting.getInstance().targetPid;
            if (pid != -1){
                procInfo.CPU_used = sigar.getProcCpu(pid).getPercent();
                procInfo.mem_used = sigar.getProcMem(pid).getSize()/sigar.getMem().getTotal();
                procInfo.sysTime = sigar.getProcTime(pid).getSys();
                procInfo.usrTime = sigar.getProcTime(pid).getUser();
            }
        } catch (SigarException e) {
            e.printStackTrace();
        }
    }

    public boolean isBusy(){
        // an judgement taken all factors into consideration

        if (ONExSetting.RANDOM_TEST){
            boolean isBusy = new Random().nextBoolean();
            log.debug("[random test] isBusy=" + isBusy);
            return isBusy;
        }
        else{
            update();
            return (sysInfo.CPU_idle < .30);
        }
	}

    public void overview() throws SigarException {
        log.info(iter(sigar.getCpuList()));
        log.info(iter(sigar.getCpuInfoList()));
        log.info(iter(sigar.getCpuPercList()));
        log.info(sigar.getCpuPerc());

        log.info(sigar.getMem());

        // process
        long pid = ONExSetting.getInstance().targetPid;
        if (pid == -1){
            log.info("PID unset");
        }
        else{
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
        }

        //network
        log.info(sigar.getNetInfo());
        for (String netInterface : sigar.getNetInterfaceList()){
            log.info((sigar.getNetInterfaceConfig(netInterface)));
        }
        log.info(sigar.getNetStat().toString());

        //thread
        log.info(sigar.getThreadCpu());
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
            r += Arrays.toString((long[]) object);
        }
        r += ">>>\n";
        return r;
    }

}

class SysInfo{
    int nCPU;
    CpuInfo CPUInfo[];
    double CPU_idle;
    double mem_free;
}

class ProcInfo{
    double CPU_used;
    double mem_used;
    long sysTime;
    long usrTime;
}

