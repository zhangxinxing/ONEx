package nuctrl.protocol;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import nuctrl.Settings;

import java.io.IOException;
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
public class BusyTable implements Serializable {
    // per controller
    private int ID;
    private String nodeName;
    private InetAddress addr;
    private int sizeOfQueueIn;
    private Map<String, Integer> cpuAccountPerApp;

    public BusyTable(int id) {
        this.ID = id;
        try {
            this.addr = Settings.getAddrByID(id);
        } catch (UnknownHostException e) {
            this.addr = null;
        }
        this.nodeName = Integer.toString(id);
        this.sizeOfQueueIn = 0;
        this.cpuAccountPerApp = new HashMap<String, Integer>();
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public InetAddress getAddr() {
        return addr;
    }

    public void setAddr(InetAddress addr) {
        this.addr = addr;
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
                "%d: Addr=%s, sizeOfQ=%d, sizeOfA=%d",
                this.ID,
                this.addr.toString(),
                this.sizeOfQueueIn,
                this.cpuAccountPerApp.size()
        );
    }
}
