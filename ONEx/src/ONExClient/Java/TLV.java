package ONExClient.Java;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM8:54
 */
class TLV{
    private static final int HEADER_LENGTH = 5;
    private byte TYPE;
    private int LEN;
    private byte[] VALUES;

    public TLV(byte Type, int len, byte[] values){
        this.TYPE = Type;
        this.LEN = len;
        this.VALUES = values;
    }

    public TLV(ByteBuffer TLVBB){
        this.TYPE = TLVBB.get();
        this.LEN = TLVBB.getInt();
        if (VALUES == null){
            VALUES = new byte[LEN];
        }
        TLVBB.get(this.VALUES, 0, this.LEN);
        assert isValid();
    }

    public boolean isValid(){
        return (LEN == VALUES.length);
    }

    public void writeTo(ByteBuffer TLVBB){
        assert TLVBB.limit() - TLVBB.position() >= getLength();
        TLVBB.put(TYPE);
        TLVBB.putInt(LEN);
        if(VALUES != null) {
            TLVBB.put(VALUES);
        }
    }

    public int getLength(){
        assert VALUES != null;
        assert LEN == VALUES.length;

        return LEN + HEADER_LENGTH;
    }

    public String toString(){
        assert LEN == VALUES.length;
        return String.format("[TLV]LEN=%d/%d", LEN, getLength());
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