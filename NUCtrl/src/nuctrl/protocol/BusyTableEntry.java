package nuctrl.protocol;

import nuctrl.Settings;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: fan
 * Date: 13-10-3
 * Time: PM8:25
 */
public class BusyTableEntry implements Serializable {
    // per controller
    private int ID;
    private int sizeOfQueueIn;
    private Map<String, Integer> cpuAccountPerApp;

    public BusyTableEntry(int id) {
        this.ID = id;
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

    @Override
    public String toString(){
        return String.format(
                "ID:%d, sizeOfQ=%d, sizeOfA=%d",
                this.ID,
                this.sizeOfQueueIn,
                this.cpuAccountPerApp.size()
        );
    }
}
