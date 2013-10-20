package MyDebugger;

import org.openflow.util.HexString;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM1:29
 */
public class Dumper {
    public static String ByteBuffer(ByteBuffer BB){
        return BB.toString();
    }

    public static String byteArray(byte[] array){
        int len = array.length;
        int nPerLine = 16;
        String toString = "";
        for(int i = 0; i < 96 && i < len; i++){
            int a = array[i]>=0 ? array[i] : array[i] + 256;
            toString += (a<10)? "0"+a : a;
            if(i != 0 && i%nPerLine == 0){
                toString += "\n";
            }
            toString += ":";
        }

        return toString;
    }
}
