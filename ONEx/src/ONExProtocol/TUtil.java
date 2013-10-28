package ONExProtocol;

/**
 * Created with IntelliJ IDEA.
 * User: zhangf
 * Date: 13-10-28
 * Time: 上午9:48
 */
public class TUtil {
    public static void main(String[] args){
        byte[] arr = Util.longToArray(1234567890L);

        System.out.println(Util.dumpArray(arr));

        long dpid = Util.arrayToLong(arr);

        System.out.println(dpid);
        System.out.println(Util.dumpArray(Util.longToArray(dpid)));

    }
}
