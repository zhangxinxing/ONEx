package ONExBox.BoxDaemon;

import ONExBox.gateway.Gateway;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-20
 * Time: AM11:50
 */
public class TONExServerDaemon {
    public static void main(String[] args){
        Gateway gateway = new Gateway();
        ONExServerDaemon serverDamon = new ONExServerDaemon(gateway, 12345);
    }
}
