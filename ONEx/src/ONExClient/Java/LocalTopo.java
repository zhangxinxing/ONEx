package ONExClient.Java;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM9:47
 */
public class LocalTopo {
    private List<SWInfo> swInfos;

    static final int PORTINFO_SIZE = 13;

    public LocalTopo() {
        swInfos = new LinkedList<SWInfo>();


        // FOR TEST
        SWInfo swInfo = new SWInfo(9677L);
        PortInfo portInfo = new PortInfo(
                (short)0xFFFF,
                Status.HOST,
                0xEEEEEEEE,
                new byte[] {(byte)0xEF,(byte)0xEF,(byte)0xEF,(byte)0XEF,(byte)0xEF,(byte)0xEF}
        );
        this.addSwitch(swInfo);
    }

    private void addSwitch(SWInfo swInfo){
        this.swInfos.add(swInfo);
    }

    public int getLength(){
        int bufSize = 0;
        for (SWInfo sw : swInfos){
            bufSize += sw.getLenght();
        }
        return bufSize;
    }

    public ByteBuffer toByteBuffer(){
        ByteBuffer topoBB = ByteBuffer.allocate(getLength());
        for (SWInfo sw : swInfos){
            sw.writeTo(topoBB);
        }
        return topoBB;
    }


    class SWInfo{
        private long dpid;
        private List<PortInfo> portInfoList;

        SWInfo(long dpid) {
            this.dpid = dpid;
            portInfoList = new LinkedList<PortInfo>();
        }

        public int getLenght(){
            return 8 + portInfoList.size()*PORTINFO_SIZE;
        }

        public void addPortStatus(PortInfo portInfo){
            this.portInfoList.add(portInfo);
        }

        public void writeTo(ByteBuffer swInfoBB){
            swInfoBB.putLong(dpid);
            for (PortInfo port : portInfoList){
                port.writeTo(swInfoBB);
            }
        }

    }
    class PortInfo{
        short portID;
        byte status;
        int IPv4;
        byte[] MAC;

        PortInfo(short portID, byte status, int IPv4, byte[] MAC) {
            this.portID = portID;
            this.status = status;
            this.IPv4 = IPv4;
            this.MAC = MAC;
        }

        public void writeTo(ByteBuffer portInfoBB){
            portInfoBB.putShort(portID);
            portInfoBB.put(status);
            portInfoBB.putInt(IPv4);
            assert MAC.length == 6;
            portInfoBB.put(MAC);
        }
    }

    class Status{
        public static final byte HOST   = 0x00;
        public static final byte DOWN   = 0x01;
        public static final byte SW     = 0x02;
    }
}
