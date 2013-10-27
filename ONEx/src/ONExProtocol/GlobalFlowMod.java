package ONExProtocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.openflow.protocol.OFFlowMod;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * User: fan
 * Date: 13-10-23
 * Time: PM9:20
 */
public class GlobalFlowMod implements ITLV{
    private List<GlobalFlowModEntry> flowModEntries;

    public GlobalFlowMod(){
        this.flowModEntries = new LinkedList<GlobalFlowModEntry>();
    }

    public GlobalFlowMod(TLV tlv){
        this.flowModEntries = new LinkedList<GlobalFlowModEntry>();
        ByteBuffer buf = ByteBuffer.wrap(tlv.getValue());
        while(buf.hasRemaining()){
            flowModEntries.add(
                    new GlobalFlowModEntry(buf)
            );
        }
    }

    public void addGlobalFlowModEntry(long dpid, OFFlowMod ofFlowMod){
        GlobalFlowModEntry newEntry = new GlobalFlowModEntry(
                dpid,
                ofFlowMod.getLengthU(),
                ofFlowMod
        );
        flowModEntries.add(newEntry);
    }

    @Override
    public ByteBuffer toByteBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(getLength());
        for (GlobalFlowModEntry entry : flowModEntries){
            entry.writeTo(buf);
        }
        return buf;
    }

    @Override
    public int getLength() {
        int len = 0;
        for (GlobalFlowModEntry entry : flowModEntries){
            len += entry.getLength();
        }
        return len;
    }

    public String toString(){
        String to = String.format(
                "[GlobalFlowMod, #entry=%d]",
                flowModEntries.size()
                );
        for (GlobalFlowModEntry entry : flowModEntries){
            to += entry.toString();
        }

        return to;
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

    public String toString(){
        return String.format(
                "[GlobalFlowModEntry, dpid=%d, ,len_ofm=%d, ofm=%s]",
                dpid,
                len_OFM,
                (ofFlowMod==null)? "null" : ofFlowMod.toString()
        );
    }

}
