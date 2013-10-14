package nuctrl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 * User: fan
 * Date: 13-10-3
 * Time: PM8:33
 */
public class Settings {
    /* random test */
    public static boolean   RANDOM_TEST;
    public static boolean   MULTI_THREAD;
    public static int       TEST_RUN;
    public static int       PACKET_INTERVAL;//ms
    public static int       MAX_WHILE_LOOP;
    public static int       MAX_QUEUE;

    public static String    BUSYTABLE_MAP;
    public static String    TOPO_MAP;
    public static int       PORT;
    public static int       BUSY_UPDATE_INT;//ms

    private String          APPNAME;

    /* information */
    public String IP;
    public InetSocketAddress socketAddr;
    public List<String> appNameList;
    public Map<String, Long> appPid;
    public long targetPid;

    // singleton
    private static Settings instance = new Settings();
    private Settings() {
        parseConfig();
        try {
            IP = new Sigar().getNetInterfaceConfig().getAddress();
            socketAddr = new InetSocketAddress(IP, PORT);
        } catch (SigarException e) {
            e.printStackTrace();
        }
        appNameList = new LinkedList<String>();
        appPid = new HashMap<String, Long>();
        regAppPid(APPNAME, new Sigar().getPid());
        targetPid = new Sigar().getPid();
    }
    public static Settings getInstance(){
        return instance;
    }

    public void parseConfig() {
        Properties config = new Properties();
        try {
            config.load(getClass().getClassLoader().getResourceAsStream("nuctrl.config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        RANDOM_TEST     = Boolean.parseBoolean(config.getProperty("RANDOM_TEST"));
        MULTI_THREAD    = Boolean.parseBoolean(config.getProperty("MULTI_THREAD"));
        PACKET_INTERVAL = Integer.parseInt(config.getProperty("PACKET_INTERVAL"));
        TEST_RUN        = Integer.parseInt(config.getProperty("TEST_RUN"));
        MAX_WHILE_LOOP  = Integer.parseInt(config.getProperty("MAX_WHILE_LOOP"));
        MAX_QUEUE       = Integer.parseInt(config.getProperty("MAX_QUEUE"));
        BUSY_UPDATE_INT = Integer.parseInt(config.getProperty("BUSY_UPDATE_INT"));
        BUSYTABLE_MAP   = config.getProperty("BUSYTABLE_MAP");
        TOPO_MAP        = config.getProperty("TOPO_MAP");
        PORT            = Integer.parseInt(config.getProperty("PORT"));
        APPNAME         = config.getProperty("nameOfApps");
    }

    public void regAppPid(String appName, long pid){
        appNameList.add(appName);
        appPid.put(appName, pid);
    }
}
