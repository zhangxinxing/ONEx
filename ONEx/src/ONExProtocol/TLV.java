package ONExProtocol;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM8:54
 */
public class TLV{
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
        if (value == null){
            value = new byte[len];
        }
        TLVBB.get(this.value, 0, this.len);
        assert isValid();
    }

    public boolean isValid(){
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
        assert TLVBB.limit() - TLVBB.position() >= getLength();
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

    public String toString(){
        assert len == value.length;
        return String.format("[TLV]len=%d/%d", len, getLength());
    }

    public class Type{
        public static final byte PACKET_IN          = 0x00;
        public static final byte FLOW_MOD = 0x01;
        public static final byte PACKET_OUT         = 0x02;
        public static final byte LOCAL_TOPO         = 0x03;
        public static final byte GLOBAL_SW_TOPO     = 0x04;
        public static final byte GLOBAL_HOST_TOPO   = 0x05;
        public static final byte SRC_HOST   = 0x06;
    }

}