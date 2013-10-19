package ONExClient.Java;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-18
 * Time: PM11:46
 */
public class ONExProtocolFactory {
    public static ONExProtocol ONExSparePI(OFPacketIn pi){
        ONExProtocol op = new ONExProtocol(ONExProtocol.SPARE_PACKET_IN, ONExGate.ID);
        ByteBuffer PIBB = ByteBuffer.allocate(pi.getLengthU());
        pi.writeTo(PIBB);
        op.setTLV(new TLV(
                TLV.Type.PACKET_IN,
                pi.getLengthU(),
                PIBB.array()
        ));
        return op;
    }

    public static ONExProtocol ONExResSparePI(OFFlowMod flowMod, OFPacketOut po){
        ONExProtocol op = new ONExProtocol(ONExProtocol.RES_SPARE_PACKET_IN, ONExGate.ID);
        ByteBuffer FMBB = ByteBuffer.allocate(flowMod.getLengthU());
        flowMod.writeTo(FMBB);

        ByteBuffer POBB = ByteBuffer.allocate(po.getLengthU());
        po.writeTo(POBB);

        op.setTLV(new TLV(
                TLV.Type.FLOWMOD,
                flowMod.getLengthU(),
                FMBB.array()
        ));

        op.setTLV(new TLV(
                TLV.Type.PACKET_OUT,
                po.getLengthU(),
                POBB.array()
        ));

        return op;
    }

    public static ONExProtocol ONExUploadLocalTopo(LocalTopo topo){
        ONExProtocol op = new ONExProtocol(ONExProtocol.UPLOAD_LOCAL_TOPO, ONExGate.ID);
        op.setTLV(new TLV(
                TLV.Type.LOCAL_TOPO,
                topo.getLength(),
                topo.toByteBuffer().array()
        ));
        return op;
    }

    public static ONExProtocol ONExGetGlobalTopo(){
        ONExProtocol op = new ONExProtocol(ONExProtocol.GET_GLOBAL_TOPO, ONExGate.ID);
        return op;
    }

    public static ONExProtocol ONExResGlobalTopo(GlobalTopo globalTopo){
        // S -> C
        ONExProtocol op = new ONExProtocol(ONExProtocol.GET_GLOBAL_TOPO, ONExGate.ID);
        op.setTLV(new TLV(
                TLV.Type.GLOBAL_HOST_TOPO,
                globalTopo.getHostListLength(),
                globalTopo.hostToBuffer().array()
        ));
        op.setTLV(new TLV(
                TLV.Type.GLOBAL_SW_TOPO,
                globalTopo.getSwTopoLength(),
                globalTopo.swToBuffer().array()
        ));

        return op;
    }

    public static ONExProtocol ONExReqGlobalFlowMod(){
        ONExProtocol op = new ONExProtocol(ONExProtocol.REQ_GLOBAL_FLOW_MOD, ONExGate.ID);
        // TODO provide further information
        return op;

    }

    public static ONExProtocol ONExSCFlowMod(OFFlowMod flowMod){
        ONExProtocol op = new ONExProtocol(ONExProtocol.RES_SPARE_PACKET_IN, ONExGate.ID);
        ByteBuffer FMBB = ByteBuffer.allocate(flowMod.getLengthU());
        flowMod.writeTo(FMBB);
        op.setTLV(new TLV(
                TLV.Type.FLOWMOD,
                flowMod.getLengthU(),
                FMBB.array()
        ));

        return op;
    }

    public static ONExProtocol ONExParser(ByteBuffer msg) {
        if(msg.hasRemaining()){
            ONExProtocol op = new ONExProtocol(0xFFFFFFFF, ONExGate.ID);
            op.setHeader(msg);
            op.setTLVs(msg);
            return op;
        }
        else{
            return null;
        }
    }
}
