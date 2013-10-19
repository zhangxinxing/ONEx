package ONExClient.Java;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM10:27
 * To change this template use File | Settings | File Templates.
 */
public class GlobalTopo {
    List<HOSTS_TOPO> hostList;
    List<SW_TOPO> swTopos;

    private static final int HostSize = 4 + 8 + 2;
    private static final int SwSize = 2*(8 + 2);

    public int getHostListLength(){
        return hostList.size() * HostSize;
    }

    public int getSwTopoLength(){
        return swTopos.size() * SwSize;
    }

    public ByteBuffer hostToBuffer(){
        ByteBuffer BB = ByteBuffer.allocate(getHostListLength());
        for (HOSTS_TOPO ht : hostList){
            ht.writeTo(BB);
        }

        return BB;
    }

    public ByteBuffer swToBuffer(){
        ByteBuffer BB = ByteBuffer.allocate(getSwTopoLength());
        for (SW_TOPO sw : swTopos){
            sw.writeTo(BB);
        }

        return BB;
    }

    class HOSTS_TOPO{
        int host;
        long dpid;
        short port;

        public void writeTo(ByteBuffer BB){
            BB.putInt(host);
            BB.putLong(dpid);
            BB.putShort(port);
        }

    }

    class SW_TOPO{
        long src_dpid;
        short src_port;
        long dst_dpid;
        short dst_port;

        public void writeTo(ByteBuffer BB){
            BB.putLong(src_dpid);
            BB.putShort(src_port);
            BB.putLong(dst_dpid);
            BB.putShort(dst_port);
        }
    }
}
