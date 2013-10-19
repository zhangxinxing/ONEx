package ONExClient.Java;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM9:47
 */
public class Topology {
    List<SWInfo> swInfos;

    static final int PORTINFO_SIZE = 13;


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

        public int getLenght(){
            return 8 + portInfoList.size()*PORTINFO_SIZE;
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

        public void writeTo(ByteBuffer portInfoBB){
            portInfoBB.putShort(portID);
            portInfoBB.putChar((char)status);
            portInfoBB.putInt(IPv4);
            assert MAC.length == 6;
            portInfoBB.put(MAC);
        }
    }

    class PortStatus{
        public static final byte HOST   = 0x00;
        public static final byte DOWN   = 0x01;
        public static final byte SW     = 0x02;
    }
}
