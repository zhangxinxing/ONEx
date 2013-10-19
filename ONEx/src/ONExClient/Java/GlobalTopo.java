package ONExClient.Java;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM10:27
 */
public class GlobalTopo {
    List<hostsTopo> hostList;
    List<swTopo> swTopos;

    public GlobalTopo() {
        hostList = new LinkedList<hostsTopo>();
        swTopos = new LinkedList<swTopo>();

        // TODO for test only
        hostList.add(new hostsTopo(0xABABABAB, 123L, (short)92));
        swTopos.add(new swTopo(123L, (short)123, 124L, (short)12));
    }


    private void addEntry(){

    }

    private static final int HostSize = 4+8+2;
    private static final int SwSize = 2*(8+2);

    public int getHostListLength(){
        return hostList.size() * HostSize;
    }

    public int getSwTopoLength(){
        return swTopos.size() * SwSize;
    }

    public ByteBuffer hostToBuffer(){
        ByteBuffer BB = ByteBuffer.allocate(getHostListLength());
        for (hostsTopo ht : hostList){
            ht.writeTo(BB);
        }

        return BB;
    }

    public ByteBuffer swToBuffer(){
        ByteBuffer BB = ByteBuffer.allocate(getSwTopoLength());
        for (swTopo sw : swTopos){
            sw.writeTo(BB);
        }

        return BB;
    }

    class hostsTopo {
        int host;
        long dpid;
        short port;

        hostsTopo(int host, long dpid, short port) {
            this.host = host;
            this.dpid = dpid;
            this.port = port;
        }

        public void writeTo(ByteBuffer BB){
            BB.putInt(host);
            BB.putLong(dpid);
            BB.putShort(port);
        }

    }

    class swTopo {
        long src_dpid;
        short src_port;
        long dst_dpid;
        short dst_port;

        swTopo(long src_dpid, short src_port, long dst_dpid, short dst_port) {
            this.src_dpid = src_dpid;
            this.src_port = src_port;
            this.dst_dpid = dst_dpid;
            this.dst_port = dst_port;
        }

        public void writeTo(ByteBuffer BB){
            BB.putLong(src_dpid);
            BB.putShort(src_port);
            BB.putLong(dst_dpid);
            BB.putShort(dst_port);
        }
    }
}
