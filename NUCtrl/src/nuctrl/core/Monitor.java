package nuctrl.core;

import nuctrl.Settings;
import org.apache.log4j.Logger;
import org.hyperic.sigar.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class Monitor {
    // data structure
    private Map<String, Integer> cpuAccount;
    private int sizeOfQueueIn;
    private static long pid;
    private static long monitor_pid;
    private static Sigar sigar = new Sigar();
    private static Logger log = Logger.getLogger(Monitor.class);

    // singleton
    private static final Monitor instance = new Monitor();

    // private constructor
    private Monitor(){
        this.cpuAccount = new HashMap<String, Integer>();
        this.sizeOfQueueIn = 0;

        monitor_pid = sigar.getPid();
        pid = -1;
    }

    public static Monitor getInstance(){
        return instance;
    }


    // getCpuAccount
    public Map<String, Integer> getCpuAccount() {
        List<String> apps = Settings.appNameList;
        for (String app : apps){
            if (Settings.RANDOM_TEST){
                cpuAccount.put(app, new Random().nextInt());
            }
            else {
                long pid = Settings.appPid.get(app);
                // TODO sigar.getProcCpu(pid).getPercent();
            }
        }
        return cpuAccount;
    }

    // get Account by name
    public int getCpuAccountByName(String appName) {

        if (Settings.RANDOM_TEST){
            return new Random().nextInt();
        }
        else {
            return getCpuAccount().get(appName);
        }
    }

    // get size of queue by name
    public int getSizeOfQueueIn(){
        List<String> apps = Settings.appNameList;
        for (String app : apps){
            if (Settings.RANDOM_TEST){
                sizeOfQueueIn = new Random().nextInt() % 10;
            }
            else {
                sizeOfQueueIn = MessageHandler.sizeOfQueue();
            }
        }
        return sizeOfQueueIn;
    }



    // get local info
    private void updateLocalSysInfo(){

    }


    public static boolean isBusy(){
        // an judgement taken all factors into consideration

        if (Settings.RANDOM_TEST){
            Random r = new Random();
            boolean isBusy = r.nextBoolean();
            log.debug("[random test] isBusy=" + isBusy);
            return isBusy;
        }
        else{
            double user, sys, idle;
            try {
                user = sigar.getCpuPerc().getUser();
                sys = sigar.getCpuPerc().getSys();
                idle = sigar.getCpuPerc().getIdle();
            } catch (SigarException e) {
                e.printStackTrace();
                return true;
            }

            return (idle < .30);
        }
	}

}

class SysInfo{
    int nCPU;
    CpuInfo CPUInfo[];
    CpuPerc CPUPerc[];
    Mem mem;
}

class ProcInfo{
    long pid;
    double CPUPerc;
    double memPerc;

}
