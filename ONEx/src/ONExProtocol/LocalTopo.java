package ONExProtocol;

import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM9:47
 */
public class LocalTopo {
    private Map<Long, SWInfo> swInfos;
    private static Logger log = Logger.getLogger(LocalTopo.class);
    static final int PORTINFO_SIZE = 13;

    public LocalTopo() {
        swInfos = new HashMap<Long, SWInfo>();
    }

    public LocalTopo(TLV tlv){
        swInfos = new HashMap<Long, SWInfo>();
        if (tlv.getType() != TLV.Type.LOCAL_TOPO){
            log.error("Error type");
        }
        else{
            ByteBuffer buf = ByteBuffer.wrap(tlv.getValue());
            while(buf.hasRemaining()){
                SWInfo swinfo = new SWInfo(buf);
                swInfos.put(swinfo.getDpid(), swinfo);
            }
        }
    }

    public SWInfo addOrUpdateSwitch(long dpid){
        SWInfo item = swInfos.get(dpid);
        if (item == null){
            swInfos.put(dpid, new SWInfo(dpid));
            return swInfos.get(dpid);
        }
        return swInfos.get(dpid);
    }

    public void udpatePortInfo(long dpid, short portID, byte status, InetAddress ipv4, byte[] MAC){
        if (MAC == null || MAC.length != 6){
            log.error("errer MAC address");
        }
        SWInfo sw = addOrUpdateSwitch(dpid);

        int ip4byte = ByteBuffer.wrap(ipv4.getAddress()).getInt();
        sw.addOrUpdatePortStatus(portID, status, ip4byte, MAC);


    }

    public int getLength(){
        int bufSize = 0;
        for (SWInfo sw : swInfos.values()){
            bufSize += sw.getLength();
        }
        return bufSize;
    }

    public ByteBuffer toByteBuffer(){
        ByteBuffer topoBB = ByteBuffer.allocate(getLength());
        for (SWInfo sw : swInfos.values()){
            sw.writeTo(topoBB);
        }
        return topoBB;
    }

    public String toString(){
        String to = "LocalTopo[#sw=" + swInfos.size();

        for (SWInfo sw : swInfos.values()){
            to += sw.toString();
        }

        return to += "]";
    }

    class SWInfo{
        private long dpid;
        private short nPort;
        private Map<Short, PortInfo> ports;

        SWInfo(long dpid) {
            this.dpid = dpid;
            nPort = 0;
            ports = new HashMap<Short, PortInfo>();
        }

        public SWInfo(ByteBuffer swInfoBB){
            ports = new HashMap<Short, PortInfo>();//Map<Short, PortInfo> ports;
            dpid = swInfoBB.getLong();
            nPort = swInfoBB.getShort();
            for ( int i = 0; i < nPort ; i++){
                PortInfo portInfo = new PortInfo(swInfoBB);
                ports.put(portInfo.getPortID(), portInfo);
            }
        }

        public int getLength(){
            return 8 + 2 + ports.size()*PORTINFO_SIZE;
        }

        public long getDpid(){
            return dpid;
        }

        public void addOrUpdatePortStatus(short portID, byte status, int IPv4, byte[] MAC){
            PortInfo portinfo = ports.get(portID);
            if (portinfo == null){
                ports.put(portID, new PortInfo(portID, status, IPv4, MAC));
                nPort += 1;
            }
            else{
                portinfo.update(status, IPv4, MAC);
            }
        }

        public void writeTo(ByteBuffer swInfoBB){
            swInfoBB.putLong(dpid);
            swInfoBB.putShort(nPort);
            for (PortInfo port : ports.values()){
                port.writeTo(swInfoBB);
            }
        }

        public String toString(){
            String toString = String.format(
                    "[SW, dpid=%d, #port=%d",
                    this.dpid,
                    this.nPort
            );
            for (PortInfo portInfo : ports.values()){
                toString += portInfo.toString();
            }
            return toString += "]";
        }

    }

    class PortInfo{
        short portID;
        byte status;
        int ipv4;
        byte[] MAC;

        PortInfo(short portID, byte status, int ipv4, byte[] MAC) {
            this.portID = portID;
            this.status = status;
            this.ipv4 = ipv4;
            this.MAC = MAC;
        }

        PortInfo(ByteBuffer buf){
            this.portID = buf.getShort();
            this.status = buf.get();
            this.ipv4 = buf.getInt();
            this.MAC = new byte[6];
            buf.get(MAC);
        }

        short getPortID(){
            return this.portID;
        }

        public void update(byte status, int IPv4, byte[] MAC){
            this.status = status;
            this.ipv4 = IPv4;
            this.MAC = MAC;
        }

        public void writeTo(ByteBuffer portInfoBB){
            portInfoBB.putShort(portID);
            portInfoBB.put(status);
            portInfoBB.putInt(ipv4);
            assert MAC.length == 6;
            portInfoBB.put(MAC);
        }

        public String toString(){
            return String.format(
                    "[portID=%h, status=%d, ip=%s, MAC=%s]",
                    portID,
                    status,
                    Util.ipToString(ipv4),
                    Util.macToString(MAC)
            );
        }
    }

    public class Status{
        public static final byte HOST   = 0x00;
        public static final byte DOWN   = 0x01;
        public static final byte SW     = 0x02;
    }
}
