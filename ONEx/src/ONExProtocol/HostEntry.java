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
public class HostEntry implements Serializable {
    long dpid;
    short port;
    int ipv4;
    byte[] MAC;

    public static int length = 8 + 2 + 4 + 6;

    public static Logger log = Logger.getLogger(HostEntry.class);

    public HostEntry(long dpid, short port, int ipv4, byte[] MAC) {
        if (MAC.length != 6) {
            log.error("MAC.length != 6");
            return;
        }
        this.dpid = dpid;
        this.port = port;
        this.ipv4 = ipv4;
        this.MAC = MAC;
    }

    public HostEntry(ByteBuffer buf) {
        dpid = buf.getLong();
        port = buf.getShort();
        ipv4 = buf.getInt();
        MAC = new byte[6];
        buf.get(MAC);

    }

    public void writeTo(ByteBuffer BB) {
        BB.putLong(dpid);
        BB.putShort(port);
        BB.putInt(ipv4);
        BB.put(MAC);
    }

    @Override
    public int hashCode() {
        final int prime = 11;
        int result = 1;
        //result = prime * result + (int) (dpid ^ (dpid >>> 32));
        //result = prime * result + (port ^ (port >>> 8));

        for (byte b : MAC) {
            result = prime * result + (b ^ (b >>> 4));
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        HostEntry other = (HostEntry) obj;

        for (int i = 0; i < 6; i++)
            if (this.MAC[i] != other.MAC[i])
                return false;
        return true;
    }

    public String toString() {
        return String.format(
                "[hostEntry, dpid=%d, port=%d, ipv4=%s, MAC=%s]",
                dpid,
                port,
                Util.ipToString(ipv4),
                Util.MACToString(MAC)
        );
    }

    public long getDpid() {
        return dpid;
    }

    public short getPort() {
        return port;
    }

    public int getIpv4() {
        return ipv4;
    }

    public byte[] getMAC() {
        return MAC;
    }

    public String getMACString() {
        return Util.MACToString(MAC);
    }

    public void setMAC(byte[] MAC) {
        this.MAC = MAC;
    }

    public void setIpv4(int ipv4) {
        this.ipv4 = ipv4;
    }

    public void setPort(short port) {
        this.port = port;
    }

    public void setDpid(long dpid) {
        this.dpid = dpid;
    }
}
