package ONExProtocol;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-30
 * Time: PM10:38
 */
public class TGlobalTopo {

    public static void main(String[] args){
        GlobalTopo topo = new GlobalTopo();
        long dpid1  = 1L;
        long dpid2  = 2L;
        long dpid3  = 3L;
        long dpid4  = 4L;

        int ip1 = 121;
        int ip2 = 122;
        int ip3 = 123;
        int ip4 = 124;

        byte[] mac1 = new byte[] {1,1,1,1,1,1};
        byte[] mac2 = new byte[] {1,1,1,1,1,2};
        byte[] mac3 = new byte[] {1,1,1,1,1,3};
        byte[] mac4 = new byte[] {1,1,1,1,1,4};

        topo.addHost(dpid1, (short)1, ip1, mac1);
        topo.addHost(dpid2, (short)1, ip2, mac2);
        topo.addHost(dpid3, (short)1, ip3, mac3);
        topo.addHost(dpid4, (short)1, ip4, mac4);

        topo.addSwitchLink(dpid2, (short)2, dpid3, (short)2);

        topo.addForestEntry(6789, (short)9, dpid1);
        topo.addForestEntry(6789, (short)9, dpid2);

        topo.addForestEntry(1234, (short)5, dpid3);
        topo.addForestEntry(1234, (short)5, dpid4);

        System.out.println(topo.isHostWithin(mac1).toString());


        topo.findGlobalWayToDpid(dpid1, dpid3).get(0).toString();

    }
}
