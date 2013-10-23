package ONExProtocol;

import org.openflow.protocol.OFFlowMod;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * User: fan
 * Date: 13-10-23
 * Time: PM9:20
 */
public class GlobalFlowMod extends TLV{

    private List<GlobalFlowModEntry> flowModEntries;

    public GlobalFlowMod(){
        super(Type.GLOBAL_FLOW_MOD, 0, null);
        this.flowModEntries = new LinkedList<GlobalFlowModEntry>();
    }

    public void addGlobalFlowModEntry(long dpid, OFFlowMod ofFlowMod){
        GlobalFlowModEntry newEntry = new GlobalFlowModEntry(
                dpid,
                ofFlowMod.getLengthU(),
                ofFlowMod
        );
        flowModEntries.add(newEntry);
        len += newEntry.getLength();
    }

    public void zip(){
        ByteBuffer buf = ByteBuffer.allocate(len);
        for (GlobalFlowModEntry entry : flowModEntries){
            entry.writeTo(buf);
        }
        value = buf.array();
        if (!isValid()){
            log.error(String.format(
                    "GlobalFlowMod zip not valid: except=%d, get=%d",
                    len,
                    value.length
            ));
        }
    }

    public GlobalFlowMod unzip(){
        ByteBuffer buf = ByteBuffer.wrap(value);
        while(buf.hasRemaining()){
            flowModEntries.add(new GlobalFlowModEntry(buf));
        }
        return null;
    }

    public void writeTo(ByteBuffer buf){
        buf.put(type);
        buf.putInt(len);
        for (GlobalFlowModEntry entry : flowModEntries){
            entry.writeTo(buf);
        }
    }

}

class GlobalFlowModEntry{
    private long dpid;
    private int len_OFM;
    private OFFlowMod ofFlowMod;

    GlobalFlowModEntry(long dpid, int len_OFM, OFFlowMod ofFlowMod) {
        this.dpid = dpid;
        this.len_OFM = len_OFM;
        this.ofFlowMod = ofFlowMod;
    }

    GlobalFlowModEntry(ByteBuffer buf){
        this.dpid = buf.getLong();
        this.len_OFM = buf.getInt();
        // TODO ofFlowMod = null;
        byte[] ofm = new byte[len_OFM];
        buf.get(ofm);

    }

    public void writeTo(ByteBuffer buf){
        buf.putLong(dpid);
        buf.putInt(len_OFM);
        ofFlowMod.writeTo(buf);
    }

    public int getLength(){
        return 8 + 4 + ofFlowMod.getLengthU();
    }

}
