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
    public static final int RES_SPARE_PACKET_IN = 0x00000001;
    public static final int UPLOAD_LOCAL_TOPO   = 0x00000002;
    public static final int GET_GLOBAL_TOPO     = 0x00000003;
    public static final int RES_GET_GLOBAL_TOPO = 0x00000004;
    public static final int REQ_GLOBAL_FLOW_MOD = 0x00000005;
    public static final int SC_FLOW_MOD         = 0x00000006;
    public static final int ONEX_INS_MAX        = SC_FLOW_MOD;
    public static final int ONEX_INS_MIN        = SPARE_PACKET_IN;


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

    public void setTLVs(ByteBuffer msg){
        while(msg.hasRemaining()){
            setTLV(new TLV(msg));
        }
    }

    public void setHeader(ByteBuffer headerBB){
        if(headerBB.hasRemaining())
            header.readFrom(headerBB);
        else{
            System.err.println("ByteBuffer sucks, " + headerBB.toString());
        }
    }

    public void writeTo(ByteBuffer ONExBB){
        header.writeTo(ONExBB);
        for (TLV tlv : TLVs){
            tlv.writeTo(ONExBB);
        }
    }

    public int getLength(){
        int length = 0;
        length += header.getLength();
        for (TLV tlv : TLVs){
            length += tlv.getLength();
        }

        return length;
    }

    @Override
    public String toString(){
        String tostring = "[ONExProtocol, length=" + getLength() +
                "]" + header.toString();
        tostring += String.format("[TLVs, #=%d]", TLVs.size());
        for (TLV tlv : TLVs){
            tostring += tlv.toString();
        }
        return tostring;
    }

    class ONExHeader {
        /* static final */
        private static final int HEADER_LENGTH = 1 + 4 + 4 + 4;

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
            HeaderBB.put(VERSION);
            HeaderBB.putInt(LEN);
            HeaderBB.putInt(INS);
            HeaderBB.putInt(ID);
        }

        public void readFrom(ByteBuffer headerBB){
            VERSION = (byte)headerBB.get();
            LEN = headerBB.getInt();
            INS = headerBB.getInt();
            ID = headerBB.getInt();
            assert (INS <= ONEX_INS_MAX && INS >= ONEX_INS_MIN);
        }

        public int getLength(){
            return HEADER_LENGTH;
        }

        @Override
        public String toString(){
            return String.format("[header length=%d,INS=%d,ID=%d]",
                    LEN,INS,ID);
        }


    }
}
