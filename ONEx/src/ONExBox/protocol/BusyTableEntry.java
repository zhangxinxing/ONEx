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
    private Map<String, Double> cpuAccount;

    public BusyTableEntry(InetSocketAddress addr) {
        this.addr = addr;
        this.sizeOfQueueIn = 0;
        this.cpuAccount = new HashMap<String, Double>();
    }

    public int getSizeOfQueueIn() {
        return sizeOfQueueIn;
    }

    public void setSizeOfQueueIn(int sizeOfQueueIn) {
        this.sizeOfQueueIn = sizeOfQueueIn;
    }

    public Map<String, Double> getCpuAccount() {
        return cpuAccount;
    }

    public void setCpuAccountByApp(String id, Double time) {
        if (this.cpuAccount.containsKey(id)){
            cpuAccount.put(id, time);
        }
        else{
            cpuAccount.put(id, time);
        }
    }

    public void setCpuAccount(Map<String, Double> account){
        this.cpuAccount = account;
    }

    @Override
    public String toString(){
        return String.format(
                "ID:%s, size=%d, cpu=%1.2f",
                this.addr.toString(),
                this.cpuAccount.size(),
                this.cpuAccount.get("all")
        );
    }
}
