package ONExProtocol;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-27
 * Time: PM4:31
 */
public class Util {
    public static String ipToString(int address){
        StringBuilder sb = new StringBuilder();
        int result = 0;
        for (int i = 0; i < 4; ++i) {
            result = (address >> ((3-i)*8)) & 0xff;
            sb.append(Integer.valueOf(result).toString());
            if (i != 3)
                sb.append(".");
        }
        return sb.toString();
    }

    public static String macToString(byte[] mac){
        StringBuilder builder = new StringBuilder();
        for (byte b: mac) {
            if (builder.length() > 0) {
                builder.append(":");
            }
            builder.append(String.format("%02X", b & 0xFF));
        }
        return builder.toString();
    }

    public static byte[] longToArray(long n){
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putLong(n);
        buf.flip();
        return buf.array();
    }

    public static long arrayToLong(byte[] array){
        if (array.length > 8){
            System.err.println("array must be <8-byte");
        }
        long re = 0L;
        long temp;
        for (int i = array.length - 1; i >= 0; i--){
            temp = array[i];
            re = re | ((temp << (array.length - 1 - i)*8 & 0xFFL << (array.length - 1 - i)*8));
        }
        return re;
    }

    public static long MACToLong(byte[] mac){
        if (mac.length != 6){
            System.err.println("error format MAC");
            return -1L;
        }
        return arrayToLong(mac);
    }

    public static byte[] longToMAC(long mac){
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putLong(mac);
        buf.flip();
        if (buf.getShort() != 0){
            System.err.println("error in long");
        }
        byte[] MAC = new byte[6];
        System.arraycopy(buf.array(), 2, MAC, 0, 6);
        return MAC;
    }

    public static String dumpArray(Object obj){
        if (obj == null){
            return "";
        }
        if (obj instanceof byte[]){
            byte[] array = (byte[])obj;
            if (array.length == 0)
                return "";
            String re = "[byte]{";
            for(byte b : array){
                re += b;
                re += ", ";
            }

            re += "}";

            return re;
        }
        return "";
    }
}
