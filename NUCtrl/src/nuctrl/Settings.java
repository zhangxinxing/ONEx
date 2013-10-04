package nuctrl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * User: fan
 * Date: 13-10-3
 * Time: PM8:33
 */
public class Settings {
    public static String BUSYTABLE_MAP = "busyTable";
    public static String TOPO_MAP = "topoTable";
    public static int PORT = 12345;
    public static InetSocketAddress myAddr;

    public Settings(){
        try {
            myAddr = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), Settings.PORT);
        } catch (UnknownHostException e) {
        }

        // TODO read in config file to fill ID, port and so on

    }
}
