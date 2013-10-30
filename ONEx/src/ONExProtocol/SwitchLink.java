package ONExProtocol;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-27
 * Time: PM6:17
 */
public class SwitchLink implements Serializable{
    long srcDpid;
    short srcPort;
    long dstDpid;
    short dstPort;

    public static int length = 2*(8+2);

    public SwitchLink(long srcDpid, short srcPort, long dstDpid, short dstPort) {
        this.srcDpid = srcDpid;
        this.srcPort = srcPort;
        this.dstDpid = dstDpid;
        this.dstPort = dstPort;
    }

    public SwitchLink(ByteBuffer buf){
        srcDpid = buf.getLong();
        srcPort = buf.getShort();
        dstDpid = buf.getLong();
        dstPort = buf.getShort();
    }

    public void writeTo(ByteBuffer BB){
        BB.putLong(srcDpid);
        BB.putShort(srcPort);
        BB.putLong(dstDpid);
        BB.putShort(dstPort);
    }

    public long getSrcDpid() {
        return srcDpid;
    }

    public short getSrcPort() {
        return srcPort;
    }

    public long getDstDpid() {
        return dstDpid;
    }

    public short getDstPort() {
        return dstPort;
    }

    public String toString(){
        return String.format(
                "[swlink, %d:%d -> %d:%d]",
                srcDpid,
                srcPort,
                dstDpid,
                dstPort
        );
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (dstDpid ^ (dstDpid >>> 32));
        result = prime * result + dstPort;
        result = prime * result + (int) (srcDpid ^ (srcDpid >>> 32));
        result = prime * result + srcPort;
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
        SwitchLink other = (SwitchLink) obj;
        if (this.dstDpid != other.dstDpid)
            return false;
        if (this.dstPort != other.dstPort)
            return false;
        if (this.srcDpid != other.srcDpid)
            return false;
        if (this.srcPort != other.srcPort)
            return false;
        return true;
    }

}
