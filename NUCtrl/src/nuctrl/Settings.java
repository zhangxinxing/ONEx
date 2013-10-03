package nuctrl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * User: fan
 * Date: 13-10-3
 * Time: PM8:33
 */
public class Settings {
    public static String BUSYTABLE_MAP = "busyTable";

    /* data */
    private Map<Integer, InetAddress> addrMap;


    /* local information */
    public static InetAddress getAddrByID(int id) throws UnknownHostException {
        return InetAddress.getByName("127.0.0.1");
    }

    public static int getLocalID(){
        return 0;
    }
}
