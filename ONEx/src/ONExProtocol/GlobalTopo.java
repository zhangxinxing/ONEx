package ONExProtocol;

import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM10:27
 */
public class GlobalTopo {
    private static Logger log = Logger.getLogger(GlobalTopo.class);
    private long nHost;
    Set<HostEntry> hostList;
    private long nSwitch;
    Set<SwitchLink> switchLinks;

    public GlobalTopo() {
        hostList = new HashSet<HostEntry>();
        switchLinks = new HashSet<SwitchLink>();
    }

    public GlobalTopo(TLV tlv){
        if (tlv.getType() != TLV.Type.GLOBAL_TOPO){
            log.error("Error type");
        }
        else{
            hostList = new HashSet<HostEntry>();
            switchLinks = new HashSet<SwitchLink>();
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

    public Set<SwitchLink> getSwitchLinks(){
        return switchLinks;
    }

    public void mergeSwitchLinks(Set<SwitchLink> switchLinks){
        this.nSwitch += switchLinks.size();
        this.switchLinks.addAll(switchLinks);
    }

    public Set<HostEntry> getHostList(){
        return hostList;
    }

    public void mergeHostList(Set<HostEntry> hostList){
        this.nHost += hostList.size();
        this.hostList.addAll(hostList);
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
        SwitchLink link = new SwitchLink(
                srcDpid, srcPort, dstDpid, dstPort
        );
        if (!switchLinks.contains(link)){
            switchLinks.add(link);
            this.nSwitch += 1;
        }

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

