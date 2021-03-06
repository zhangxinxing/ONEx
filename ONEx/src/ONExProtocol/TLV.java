package ONExProtocol;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM8:54
 */
public class TLV implements Serializable{
    private static Logger log = Logger.getLogger(TLV.class);
    private static final int HEADER_LENGTH = 5;
    private byte type;
    private int len;
    private byte[] value;


    public TLV(byte Type, int len, byte[] values){
        this.type = Type;
        this.len = len;
        this.value = values;
    }

    public TLV(ByteBuffer TLVBB){
        this.type = TLVBB.get();
        this.len = TLVBB.getInt();
        if (len == 0){
            value = null;
        }
        else{
            if (value == null){
                value = new byte[len];
            }
            TLVBB.get(this.value, 0, this.len);
        }
    }

    public boolean isValid(){
        if (value == null && len == 0){
            return true;
        }
        if (value == null){
            return type == Type.SRC_HOST || type == Type.SRC_DPID;
        }
        return (len == value.length);
    }

    public int getType(){
        return type;
    }

    public byte[] getValue(){
        return value;
    }

    public void setValue(byte[] value){
        this.value = value;
    }

    public void writeTo(ByteBuffer TLVBB){
        TLVBB.put(type);
        TLVBB.putInt(len);
        if(value != null) {
            TLVBB.put(value);
        }
    }

    public int getLength(){
        assert value != null;
        assert len == value.length;

        return len + HEADER_LENGTH;
    }

    public TLV unzip(){
        // from byte[] to POJO
        return null;
    }

    public String toString(){
        assert len == value.length;
        String to = String.format("[TLV,T=%d,L=%d/%d,V=", getType(), len, getLength());
        to +=  (value != null) ? value.length : "null";
        return to += "]";
    }

    public class Type{
        public static final byte PACKET_IN          = 0x00;
        public static final byte FLOW_MOD           = 0x01;
        public static final byte PACKET_OUT         = 0x02;
        public static final byte LOCAL_TOPO         = 0x03;
        public static final byte GLOBAL_TOPO        = 0x04;
        public static final byte SRC_HOST           = 0x06;
        public static final byte SRC_DPID           = 0x08;// flowmod, spare_packet_in, res_spare_packet_in;
        public static final byte GLOBAL_FLOW_MOD    = 0x07;
        public static final byte FILE_NAME          = 0x09;
    }

}
