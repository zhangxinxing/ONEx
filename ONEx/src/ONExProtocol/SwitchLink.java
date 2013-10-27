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
    long src_dpid;
    short src_port;
    long dst_dpid;
    short dst_port;

    public static int length = 2*(8+2);

    public SwitchLink(long src_dpid, short src_port, long dst_dpid, short dst_port) {
        this.src_dpid = src_dpid;
        this.src_port = src_port;
        this.dst_dpid = dst_dpid;
        this.dst_port = dst_port;
    }

    public SwitchLink(ByteBuffer buf){
        src_dpid = buf.getLong();
        src_port = buf.getShort();
        dst_dpid = buf.getLong();
        dst_port = buf.getShort();
    }

    public void writeTo(ByteBuffer BB){
        BB.putLong(src_dpid);
        BB.putShort(src_port);
        BB.putLong(dst_dpid);
        BB.putShort(dst_port);
    }

    public String toString(){
        return String.format(
                "[swlink, %d:%d -> %d:%d]",
                src_dpid,
                src_port,
                dst_dpid,
                dst_port
        );

    }

    @Override
    public boolean equals(Object obj){
        if (obj != null && obj instanceof SwitchLink){
            SwitchLink link = (SwitchLink) obj;
            return this.src_dpid == link.src_dpid
                            && this.src_port == link.src_port
                            && this.dst_dpid == link.dst_dpid
                            && this.dst_port == link.dst_port
                    ;

        }
        return false;
    }

}
