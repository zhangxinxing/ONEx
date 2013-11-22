package ONExProtocol;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: zhangf
 * Date: 13-10-28
 * Time: 下午12:44
 */
public class ForestEntry implements Serializable {
    private static Logger log = Logger.getLogger(ForestEntry.class);
    private long controllerID;
    private long dpid;

    public ForestEntry(Long controllerID, long dpid) {
        this.controllerID = controllerID;
        this.dpid = dpid;
    }

    public ForestEntry(ForestEntry old) {
        this.controllerID = old.getControllerID();
        this.dpid = old.getDpid();
    }

    public ForestEntry(ByteBuffer buf) {
        if (buf.hasRemaining()) {
            controllerID = buf.getLong();
            dpid = buf.getLong();
        } else {
            log.error("Buf is empty");
        }
    }

    public void writeTo(ByteBuffer buf) {
        buf.putLong(controllerID);
        buf.putLong(dpid);
    }

    public static int getLength() {
        return 8 + 8;
    }

    public long getForestNode() {
        return controllerID;
    }

    public long getControllerID() {
        return controllerID;
    }


    public long getDpid() {
        return dpid;
    }

    public String toString() {
        return String.format(
                "[Forest Entry][controllerID = %d, dpid = %d]",
                controllerID,
                dpid
        );
    }

    @Override
    public int hashCode() {
        final int prime = 11;
        int result = 1;
        result = prime * result + (int) (dpid ^ (dpid >>> 32));
        result = prime * result + (int) controllerID;
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

        ForestEntry other = (ForestEntry) obj;

        if (this.dpid != other.dpid)
            return false;
        if (this.controllerID != other.controllerID)
            return false;
        return true;
    }
}
