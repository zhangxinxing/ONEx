package ONExBox.protocol;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * User: fan
 * Date: 13-10-3
 * Time: PM8:25
 */
public class BusyTableEntry implements Serializable {
    // per controller
    private InetSocketAddress addr;
    private int sizeOfQueueIn;
    private Map<String, Integer> cpuAccountPerApp;

    public BusyTableEntry(InetSocketAddress addr) {
        this.addr = addr;
        this.sizeOfQueueIn = 0;
        this.cpuAccountPerApp = new HashMap<String, Integer>();
    }

    public int getSizeOfQueueIn() {
        return sizeOfQueueIn;
    }

    public void setSizeOfQueueIn(int sizeOfQueueIn) {
        this.sizeOfQueueIn = sizeOfQueueIn;
    }

    public Map<String, Integer> getCpuAccountPerApp() {
        return cpuAccountPerApp;
    }

    public void setCpuAccountPerApp(String id, int time) {
        if (this.cpuAccountPerApp.containsKey(id)){
            cpuAccountPerApp.put(id, time);
        }
        else{
            cpuAccountPerApp.put(id, time);
        }
    }

    public void setCpuAccount(Map<String, Integer> account){
        this.cpuAccountPerApp = account;
    }

    @Override
    public String toString(){
        return String.format(
                "ID:%s, sizeOfQ=%d, sizeOfA=%d",
                this.addr.toString(),
                this.sizeOfQueueIn,
                this.cpuAccountPerApp.size()
        );
    }
}
