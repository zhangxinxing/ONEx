package ONExProtocol;

import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

    private long nForest;
    Map<InetSocketAddress, ForestEntry> forestEntries;

    public GlobalTopo() {
        hostList = new HashSet<HostEntry>();
        switchLinks = new HashSet<SwitchLink>();
        forestEntries = new HashMap<InetSocketAddress, ForestEntry>();
    }

    public GlobalTopo(TLV tlv){
        if (tlv.getType() != TLV.Type.GLOBAL_TOPO){
            log.error("Error type");
        }
        else{
            hostList = new HashSet<HostEntry>();
            switchLinks = new HashSet<SwitchLink>();
            forestEntries = new HashMap<InetSocketAddress, ForestEntry>();
            ByteBuffer buf = ByteBuffer.wrap(tlv.getValue());

            nHost = buf.getLong();
            for(int i = 0; i < nHost; i++){
                hostList.add(new HostEntry(buf));
            }

            nSwitch = buf.getLong();
            for(int i = 0; i < nSwitch; i++){
                switchLinks.add(new SwitchLink(buf));
            }

            nForest = buf.getLong();
            for(int i = 0; i < nForest; i++){
                ForestEntry entry = new ForestEntry(buf);
                forestEntries.put(entry.getInetSocketAddress(), entry);
            }
        }
    }

    public Set<SwitchLink> getSwitchLinks(){
        return switchLinks;
    }

    public void mergeSwitchLinks(Set<SwitchLink> switchLinks){
        for(SwitchLink entry : switchLinks){
            if(!this.switchLinks.contains(entry)){
                this.switchLinks.add(entry);
                this.nSwitch += 1;
            }
        }
    }

    public Set<HostEntry> getHostList(){
        return hostList;
    }

    public void mergeHostList(Set<HostEntry> hostList){
        for(HostEntry entry : hostList){
            if(!this.hostList.contains(entry)){
                this.hostList.add(entry);
                this.nHost += 1;
            }
        }
    }

    public Map<InetSocketAddress, ForestEntry> getForestEntries(){
        return forestEntries;
    }

    public void mergeForestEntry(Set<ForestEntry> forest){
        for(ForestEntry entry : forest){
            if(!forestEntries.containsValue(entry)){
                forestEntries.put(entry.getInetSocketAddress(), entry);
                this.nForest += 1;
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
        SwitchLink link = new SwitchLink(
                srcDpid, srcPort, dstDpid, dstPort
        );
        if (!switchLinks.contains(link)){
            switchLinks.add(link);
            this.nSwitch += 1;
        }

    }

    public void addForestEntry(int ipv4, short tcpPort, long dpid){
        ForestEntry entry = new ForestEntry(ipv4, tcpPort, dpid);
        if (!forestEntries.containsValue(entry)){
            forestEntries.put(entry.getInetSocketAddress(), entry);
            this.nForest += 1;
        }
    }

    public int getLength(){
        return switchLinks.size() * SwitchLink.length +
                hostList.size() * HostEntry.length +
                forestEntries.size() * ForestEntry.getLength() +
                8 + 8 + 8;
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

        BB.putLong(nForest);
        for (ForestEntry fr : forestEntries.values()){
            fr.writeTo(BB);
        }
        return BB;
    }

    public String toString(){
        String to = String.format(
                "[GlobalTopo, #host=%d, #switch=%d, #forest=%d, len=%d]",
                nHost,
                nSwitch,
                nForest,
                getLength()
        );
        for (HostEntry hostEntry : hostList){
            to += hostEntry.toString();
        }
        for (SwitchLink swLink : switchLinks){
            to += swLink.toString();
        }

        for (ForestEntry entry : forestEntries.values()){
            to += entry.toString();
        }

        return to;
    }
}

