package ONExClient.Java;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM12:14
 */
public class ONExProtocol {
    public static final int SPARE_PACKET_IN     = 0x00000000;
    public static final int REP_SPARE_PACKET_IN = 0x00000001;
    public static final int UPLOAD_LOCAL_TOPO = 0x00000002;
    public static final int GET_GLOBAL_TOPO     = 0x00000003;
    public static final int REP_GET_GLOBAL_TOPO = 0x00000004;
    public static final int REQ_GLOBAL_FLOW_MOD = 0x00000005;
    public static final int SC_FLOW_MOD         = 0x00000006;
}
