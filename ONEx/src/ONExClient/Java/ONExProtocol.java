package ONExClient.Java;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM12:14
 */
public class ONExProtocol {
    public static final int SPARE_PACKET_IN     = 0x00000000;
    public static final int REP_SPARE_PACKET_IN = 0x00000001;
    public static final int UPLOAD_LOCAL_TOPO   = 0x00000002;
    public static final int GET_GLOBAL_TOPO     = 0x00000003;
    public static final int REP_GET_GLOBAL_TOPO = 0x00000004;
    public static final int REQ_GLOBAL_FLOW_MOD = 0x00000005;
    public static final int SC_FLOW_MOD         = 0x00000006;

    private static Logger log = Logger.getLogger(ONExProtocol.class);

    /* field */
    private ONExHeader header;
    private List<TLV> TLVs;

    public ONExProtocol(int INS, int ID) {
        header = new ONExHeader(INS, ID);
        TLVs = new LinkedList<TLV>();
    }

    public void setTLV(TLV tlv){
        if (!tlv.isValid()){
            log.error("TLV is invalid");
        }
        else {
            TLVs.add(tlv);
        }
    }

    public void writeTo(ByteBuffer ONExBB){
        header.writeTo(ONExBB);
        for (TLV tlv : TLVs){
            tlv.writeTo(ONExBB);
        }
    }


    class ONExHeader {
        private byte VERSION;
        private int LEN;
        private int INS;
        private int ID;


        ONExHeader(int INS, int ID) {
            this.VERSION    = 0x01;
            this.LEN        = HEADER_LENGTH;
            this.INS        = INS;
            this.ID         = ID;
        }

        public void writeTo(ByteBuffer HeaderBB){
            HeaderBB.putChar((char)VERSION);
            HeaderBB.putInt(LEN);
            HeaderBB.putInt(INS);
            HeaderBB.putInt(ID);
        }

        /* static final */
        private static final int HEADER_LENGTH = 1 + 4 + 4 + 4;
    }
}
