package ONExProtocol;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-27
 * Time: PM6:17
 */
public class HostEntry implements Serializable{
    long dpid;
    short port;
    int ipv4;
    byte[] MAC;

    public static int length = 8+2+4+6;

    public static Logger log = Logger.getLogger(HostEntry.class);

    HostEntry(long dpid, short port, int ipv4, byte[] MAC) {
        if (MAC.length != 6){
            log.error("MAC.length != 6");
            return;
        }
        this.dpid = dpid;
        this.port = port;
        this.ipv4 = ipv4;
        this.MAC = MAC;
    }

    public HostEntry(ByteBuffer buf){
        dpid = buf.getLong();
        port = buf.getShort();
        ipv4 = buf.getInt();
        MAC = new byte[6];
        buf.get(MAC);

    }

    public void writeTo(ByteBuffer BB){
        BB.putLong(dpid);
        BB.putShort(port);
        BB.putInt(ipv4);
        BB.put(MAC);
    }

    public String toString(){
        return String.format(
                "[hostEntry, dpid=%d, port=%d, ipv4=%s, MAC=%s]",
                dpid,
                port,
                Util.ipToString(ipv4),
                Util.macToString(MAC)
        );
    }

}
