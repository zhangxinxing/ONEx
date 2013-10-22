package ONExProtocol;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionType;
import org.openflow.protocol.factory.OFActionFactory;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM12:14
 */
public class ONExPacket implements Serializable {
    public static final int SPARE_PACKET_IN     = 0x00000000;
    public static final int RES_SPARE_PACKET_IN = 0x00000001;
    public static final int UPLOAD_LOCAL_TOPO   = 0x00000002;
    public static final int GET_GLOBAL_TOPO     = 0x00000003;
    public static final int RES_GET_GLOBAL_TOPO = 0x00000004;
    public static final int REQ_GLOBAL_FLOW_MOD = 0x00000005;
    public static final int SC_FLOW_MOD         = 0x00000006;
    public static final int ONEX_INS_MAX        = SC_FLOW_MOD;
    public static final int ONEX_INS_MIN        = SPARE_PACKET_IN;


    private static Logger log = Logger.getLogger(ONExPacket.class);

    /* field */
    private ONExHeader header;
    private List<TLV> TLVs;

    public ONExPacket(int INS, int ID) {
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

    public void setSrcHost(InetSocketAddress address){
        if (header.INS != SPARE_PACKET_IN && header.INS != RES_SPARE_PACKET_IN){
            log.error("Error: Wrong type");
            return;
        }
        ByteBuffer buf = ByteBuffer.allocate(8);
        byte[] addr = address.getAddress().getAddress();
        for (byte i : addr){
            buf.put(i);
        }
        buf.putInt(address.getPort());

        boolean ok = false;

        for (TLV tlv : TLVs){
            if (tlv.getType() == TLV.Type.SRC_HOST){
                tlv.setValue(buf.array());
                ok = true;
                break;
            }
        }
        if (!ok){
            log.error(this.toString() + " doesnt contain TLV SRC_HOST");
        }
    }

    public InetSocketAddress getSrcHost(){
        if (header.INS != SPARE_PACKET_IN && header.INS != RES_SPARE_PACKET_IN){
            log.error("Error: wrong type");
            return null;
        }

        byte[] srcHost = null;

        for (TLV tlv : TLVs){
            if (tlv.getType() == TLV.Type.SRC_HOST){
                srcHost = tlv.getValue();
                break;
            }
        }

        if (srcHost == null){
            log.error("src == null");
            return null;
        }

        byte[] addr = new byte[4];
        System.arraycopy(srcHost, 0, addr, 0, 4);
        try {
            InetAddress address = InetAddress.getByAddress(addr);
            int port = ByteBuffer.wrap(srcHost, 4, 4).getInt();
            return new InetSocketAddress(address, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        log.error("dont know why here");
        return null;
    }

    public OFPacketIn getOFPacketIn(){
        if (header.INS != SPARE_PACKET_IN){
            log.error("this method should only be called in SPARE_PACKET_IN");
            return null;
        }

        byte[] pi = null;
        for(TLV tlv : TLVs){
            if (tlv.getType() == TLV.Type.PACKET_IN){
                pi = tlv.getValue();
                break;
            }
        }
        if (pi == null)
            return null;
        OFPacketIn ofpi = new OFPacketIn();
        ofpi.readFrom(ByteBuffer.wrap(pi));
        return ofpi;
    }

    public OFFlowMod getFlowMod(){
        // TODO bitwise
        if (header.INS != SC_FLOW_MOD && header.INS != RES_SPARE_PACKET_IN){
            log.error("this method should only be called in SC_FLOW_MOD or RES_SPARE_PACKET_IN");
            return null;
        }
        byte[] flowMod = null;
        for(TLV tlv : TLVs){
            if (tlv.getType() == TLV.Type.FLOW_MOD){
                flowMod = tlv.getValue();
                break;
            }
        }
        if (flowMod == null)
            return null;
        OFFlowMod offm = new OFFlowMod();
        // TODO offm.readFrom(ByteBuffer.wrap(flowMod));
        return offm;
    }

    public OFPacketOut getOFPacketOut(){
        // TODO bitwise
        if (header.INS != RES_SPARE_PACKET_IN){
            log.error("this method should only be called in RES_SPARE_PACKET_IN");
            return null;
        }
        byte[] po = null;
        for(TLV tlv : TLVs){
            if (tlv.getType() == TLV.Type.PACKET_OUT){
                po = tlv.getValue();
                break;
            }
        }
        if (po == null)
            return null;
        OFPacketOut ofpo = new OFPacketOut();
        // TODO ofpo.readFrom(ByteBuffer.wrap(po));
        return ofpo;
    }

    public void writeTo(ByteBuffer ONExBB){
        header.writeTo(ONExBB);
        for (TLV tlv : TLVs){
            tlv.writeTo(ONExBB);
        }
    }

    public int getINS(){
        return header.INS;
    }

    public int getLength(){
        int length = 0;
        length += header.getLength();
        for (TLV tlv : TLVs){
            length += tlv.getLength();
        }

        return length;
    }

    public boolean isValid(){
        // TODO
        return true;
    }

    public byte[] toByteArray(){
        ByteBuffer buf = toByteBuffer();
        return buf.array();
    }

    public ByteBuffer toByteBuffer(){
        ByteBuffer buf = ByteBuffer.allocate(getLength());
        writeTo(buf);
        return buf;
    }

    @Override
    public String toString(){
        String tostring = "[ONExPacket, length=" + getLength() +
                "]" + header.toString();
        tostring += String.format("[TLVs, #=%d]", TLVs.size());
        for (TLV tlv : TLVs){
            tostring += tlv.toString();
        }
        return tostring;
    }

    class ONExHeader implements Serializable{

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
