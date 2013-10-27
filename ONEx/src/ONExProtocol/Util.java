package ONExProtocol;

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
}
