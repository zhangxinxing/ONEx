package ONExClient.Java;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM12:41
 * To change this template use File | Settings | File Templates.
 */
public class TClient {
    public static void main(String[] args){
        System.out.println("Hello world");
        MessageHandler msgH = new MessageHandler();
        ONExGate onExGate = new ONExGate(msgH);

        System.out.println(onExGate.toString());
    }
}
