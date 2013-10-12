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
    public static boolean RANDOM_TEST = true;
    public static String BUSYTABLE_MAP = "busyTable";
    public static String TOPO_MAP = "topoTable";
    public static String IP;
    public static int PORT = 12345;
    public static InetSocketAddress socketAddr;
    public static int BUSY_UPDATE_INT = 1000;// ms
    public static List<String> appNameList;
    public static Map<String, Long> appPid;
    public static long mainPid;
    private static Sigar sigar = new Sigar();

    // singleton
    private static Settings instance = new Settings();
    private Settings() {
        try {
            IP = sigar.getNetInterfaceConfig().getAddress();
            socketAddr = new InetSocketAddress(IP, PORT);
        } catch (SigarException e) {
            e.printStackTrace();
        }

        appNameList = new LinkedList<String>();
        appPid = new HashMap<String, Long>();
        mainPid = new Sigar().getPid();

        // TODO read in config
        parseConfig();

    }
    public static Settings getInstance(){
        return instance;
    }

    public void parseConfig() {
        if (Settings.RANDOM_TEST) {
            this.regAppPid("app1", mainPid);
        }
    }

    public void regAppPid(String appName, long pid){
        appNameList.add(appName);
        appPid.put(appName, pid);
    }

}
