package nuctrl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

/**
 * User: fan
 * Date: 13-10-3
 * Time: PM8:33
 */
public class Settings {
    public static boolean RANDOM_TEST = true;
    public static String BUSYTABLE_MAP = "busyTable";
    public static String TOPO_MAP = "topoTable";
    public static int PORT = 12345;
    public static InetSocketAddress myAddr;
    public static int BUSY_UPDATE_INT = 1000;// ms
    public static List<String> appList;

    public Settings(){
        try {
            myAddr = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), Settings.PORT);
        } catch (UnknownHostException e) {
        }

        appList = new LinkedList<String>();

        // TODO read in config file to fill ID, port and so on
        parseConfig();

    }

    public void parseConfig(){
        if (Settings.RANDOM_TEST){
            appList.add("App1");
        }
    }
}
