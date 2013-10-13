package nuctrl;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 * User: fan
 * Date: 13-10-3
 * Time: PM8:33
 */
public class Settings {
    /* random test */
    public static final boolean RANDOM_TEST = true;
    public static final boolean MULTI_THREAD = true;
    public static final int TEST_RUN = 1;
    public static final int PACKET_INTERVAL = 0;//ms
    public static final int MAX_WHILE_LOOP = 5000;
    public static final int MAX_QUEUE = 50;

    public static final String BUSYTABLE_MAP = "busyTable";
    public static final String TOPO_MAP = "topoTable";
    public static final int PORT = 12345;
    public static final int BUSY_UPDATE_INT = 1000;//ms

    /* information */
    public String IP;
    public InetSocketAddress socketAddr;
    public List<String> appNameList;
    public Map<String, Long> appPid;
    public String targetApp;
    public long targetPid;

    // singleton
    private static Settings instance = new Settings();
    private Settings() {
        try {
            IP = new Sigar().getNetInterfaceConfig().getAddress();
            socketAddr = new InetSocketAddress(IP, PORT);
        } catch (SigarException e) {
            e.printStackTrace();
        }
        appNameList = new LinkedList<String>();
        appPid = new HashMap<String, Long>();

        parseConfig();

    }
    public static Settings getInstance(){
        return instance;
    }

    public void parseConfig() {
        // TODO parseConfig
        if (Settings.RANDOM_TEST) {
            this.regAppPid("app1", new Sigar().getPid());
            this.targetPid = new Sigar().getPid();
            this.targetApp = "app1";
        }
    }

    public void regAppPid(String appName, long pid){
        appNameList.add(appName);
        appPid.put(appName, pid);
    }

}
