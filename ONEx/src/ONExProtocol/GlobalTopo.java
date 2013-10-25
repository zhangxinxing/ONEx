package ONExProtocol;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM10:27
 */
public class GlobalTopo {
    private static Logger log = Logger.getLogger(GlobalTopo.class);
    private long nHost;
    List<HostEntry> hostList;
    private long nSwitch;
    List<SwitchLink> switchLinks;

    public GlobalTopo() {
        hostList = new LinkedList<HostEntry>();
        switchLinks = new LinkedList<SwitchLink>();
    }

    public GlobalTopo(TLV tlv){
        if (tlv.getType() != TLV.Type.GLOBAL_TOPO){
            log.error("Error type");
        }
        else{
            hostList = new LinkedList<HostEntry>();
            switchLinks = new LinkedList<SwitchLink>();
            ByteBuffer buf = ByteBuffer.wrap(tlv.getValue());
            nHost = buf.getLong();
            for(int i = 0; i < nHost; i++){
                hostList.add(new HostEntry(buf));
            }
            nSwitch = buf.getLong();
            for(int i = 0; i < nSwitch; i++){
                switchLinks.add(new SwitchLink(buf));
            }
        }
    }

    public void addHostEntry(long dpid, short port, int IPv4, byte[] MAC){
        hostList.add(new HostEntry(
                dpid,
                port,
                IPv4,
                MAC
        ));
        this.nHost += 1;
    }

    public void addSwitchLink(long srcDpid, short srcPort, long dstDpid, short dstPort){
        switchLinks.add(new SwitchLink(
                srcDpid, srcPort, dstDpid, dstPort
        ));
        this.nSwitch += 1;

    }

    public int getLength(){
        return switchLinks.size() * SwitchLink.length +
                hostList.size() * HostEntry.length +
                8 + 8;
    }

    public ByteBuffer toByteBuffer(){
        ByteBuffer BB = ByteBuffer.allocate(getLength());
        BB.putLong(nHost);
        for (HostEntry ht : hostList){
            ht.writeTo(BB);
        }
        BB.putLong(nSwitch);
        for (SwitchLink sw : switchLinks){
            sw.writeTo(BB);
        }
        return BB;
    }

    public String toString(){
        String to = String.format(
                "[GlobalTopo, #host=%d, #switch=%d]",
                nHost,
                nSwitch
        );
        for (HostEntry hostEntry : hostList){
            to += hostEntry.toString();
        }
        for (SwitchLink swLink : switchLinks){
            to += swLink.toString();
        }

        return to;
    }
}

class HostEntry {
    long dpid;
    short port;
    int IPv4;
    byte[] MAC;

    public static int length = 8+2+4+6;

    public static Logger log = Logger.getLogger(HostEntry.class);

    HostEntry(long dpid, short port, int IPv4, byte[] MAC) {
        if (MAC.length != 6){
            log.error("MAC.length != 6");
            return;
        }
        this.dpid = dpid;
        this.port = port;
        this.IPv4 = IPv4;
        this.MAC = MAC;
    }

    public HostEntry(ByteBuffer buf){
        dpid = buf.getLong();
        port = buf.getShort();
        IPv4 = buf.getInt();
        MAC = new byte[6];
        buf.get(MAC);

    }

    public void writeTo(ByteBuffer BB){
        BB.putLong(dpid);
        BB.putShort(port);
        BB.putInt(IPv4);
        BB.put(MAC);
    }

    public String toString(){
        return String.format(
                "[hostEntry, dpid=%d, port=%d, ipv4=%d, MAC=%s]",
                dpid,
                port,
                IPv4,
                "TOBEDONE"
        );
    }

}

class SwitchLink {
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

}
