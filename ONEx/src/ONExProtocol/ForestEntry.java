package ONExProtocol;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: zhangf
 * Date: 13-10-28
 * Time: 下午12:44
 */
public class ForestEntry implements Serializable {
    private static Logger log = Logger.getLogger(ForestEntry.class);
    private int controllerIP;
    private short controllerONExPort;
    private long dpid;

    public ForestEntry(int ipv4, short port, long dpid) {
        this.controllerIP = ipv4;
        this.controllerONExPort = port;
        this.dpid = dpid;
    }

    public ForestEntry(ForestEntry old){
        this.controllerIP = old.getControllerIP();
        this.controllerONExPort = old.getControllerONExPort();
        this.dpid = old.getDpid();
    }

    public ForestEntry(ByteBuffer buf){
        if (buf.hasRemaining()){
            controllerIP = buf.getInt();
            dpid = buf.getLong();
        }
        else{
            log.error("Buf is empty");
        }
    }

    public void writeTo(ByteBuffer buf){
        buf.putInt(controllerIP);
        buf.putLong(dpid);
    }

    public static int getLength(){
        return 4 + 2+ 8;
    }

    public long getForestNode(){
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putInt(controllerIP);
        buf.putShort((short)0xABCD);
        buf.putShort(controllerONExPort);
        buf.flip();
        return buf.getLong();
    }

    public int getControllerIP() {
        return controllerIP;
    }

    public short getControllerONExPort() {
        return controllerONExPort;
    }

    public long getDpid() {
        return dpid;
    }

    public String toString(){
        return String.format(
                "[Forest Entry][controller = %s, dpid = %d]",
                Util.ipToString(controllerIP),
                dpid
        );
    }

    @Override
    public int hashCode() {
        final int prime = 11;
        int result = 1;
        result = prime * result + (int) (dpid ^ (dpid >>> 32));
        result = prime * result + controllerIP;
        result = prime * result + controllerONExPort;
        return result;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        ForestEntry other = (ForestEntry) obj;

        if (this.dpid != other.dpid)
            return false;
        if (this.controllerIP != other.controllerIP)
            return false;
        if (this.controllerONExPort != other.controllerONExPort)
            return false;
        return true;
    }
}
