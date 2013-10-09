package nuctrl.core;

import nuctrl.Settings;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class Monitor {

    // data structure
    private static Logger log = Logger.getLogger(Monitor.class);
    private Map<String, Integer> cpuAccount;
    private int sizeOfQueueIn;
//    private Map<String, Integer> sizeOfQueueIn;

    // singleton
    private static final Monitor instance = new Monitor();

    private Monitor(){
        cpuAccount = new HashMap<String, Integer>();
//        sizeOfQueueIn = new HashMap<String, Integer>();
    }

    public static Monitor getInstance(){
        return instance;
    }

    // getCpuAccount
    public Map<String, Integer> getCpuAccount(){

        List<String> apps = Settings.appList;
        for (String app : apps){
            if (Settings.RANDOM_TEST){
                cpuAccount.put(app, new Random().nextInt());
            }
            // TODO add some logic here
        }
        return cpuAccount;
    }

    // get Account by name
    public int getCpuAccountByName(String appName){

        if (Settings.RANDOM_TEST){
            return new Random().nextInt();
        }
        else {
            return getCpuAccount().get(appName);
        }
    }

    // internal
    private void updateMapSizeOfQueue(){
        List<String> apps = Settings.appList;
        for (String app : apps){
            // TODO add some logic here
            if (Settings.RANDOM_TEST){
//                this.sizeOfQueueIn.put(app, new Random().nextInt());
                sizeOfQueueIn = new Random().nextInt();
            }
        }
    }

    // get size of queue by name
    public int getSizeOfQueueIn(){

        updateMapSizeOfQueue();
//        return sizeOfQueueIn.get(appName);
        return this.sizeOfQueueIn;
    }

    // get local info
	public static boolean isCpuBusy(){

        if (Settings.RANDOM_TEST){
            Random r = new Random(100);
            boolean isBusy = r.nextBoolean();
            log.debug("[random test] isBusy=" + isBusy);
            return isBusy;
        }
        // TODO add logic here
		return false;
	}
	
}
