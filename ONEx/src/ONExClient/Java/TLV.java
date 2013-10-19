package ONExClient.Java;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM8:54
 */
class TLV{
    private byte TYPE;
    private int LEN;
    private byte[] VALUES;

    public int computeLength(){
        return (VALUES == null) ? LEN : LEN + VALUES.length;
    }

    public TLV(byte Type, int len, byte[] values){
        this.TYPE = Type;
        this.LEN = len;
        this.VALUES = values;
    }

    public boolean isValid(){
        return (LEN == VALUES.length);
    }

    public void writeTo(ByteBuffer TLVBB){
        TLVBB.putChar((char)TYPE);
        TLVBB.putInt(LEN);
        if(VALUES != null) {
            TLVBB.put(VALUES);
        }
    }

    public boolean readFrom(ByteBuffer TLVBB){
        this.TYPE = (byte)TLVBB.getChar();
        this.LEN = TLVBB.getInt();
        this.VALUES = TLVBB.array();
        return isValid();
    }


    class Type{
        public static final byte PACKET_IN          = 0x00;
        public static final byte FLOWMOD            = 0x01;
        public static final byte PACKET_OUT         = 0x02;
        public static final byte LOCAL_TOPO         = 0x03;
        public static final byte GLOBAL_SW_TOPO     = 0x04;
        public static final byte GLOBAL_HOST_TOPO   = 0x05;

    }

}