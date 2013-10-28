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
    static Logger log = Logger.getLogger(ForestEntry.class);
    int controllerIP;
    short controllerONExPort;
    long dpid;

    public ForestEntry(int ipv4, short port, long dpid) {
        this.controllerIP = ipv4;
        this.controllerONExPort = port;
        this.dpid = dpid;

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

    public InetSocketAddress getInetSocketAddress(){
        return new InetSocketAddress(Util.ipToString(controllerIP), controllerONExPort);
    }

    public String toString(){
        return String.format(
                "[Forest Entry] controller = %s, dpid = %d",
                Util.ipToString(controllerIP),
                dpid
        );
    }

    @Override
    public boolean equals(Object obj){
        if (obj != null && obj instanceof ForestEntry){
            ForestEntry other = (ForestEntry) obj;
            return this.dpid == other.dpid
                    && this.controllerONExPort == other.controllerONExPort
                    && this.controllerIP == other.controllerIP
                    ;
        }
        return false;
    }
}
