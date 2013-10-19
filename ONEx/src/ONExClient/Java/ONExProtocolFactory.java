package ONExClient.Java;

import ONExClient.Java.Interface.IONExProtocolFactory;
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
public class ONExProtocolFactory implements IONExProtocolFactory {
    public static ONExProtocol buildONEx(int INS, int ID){
        return null;
    }

    public ONExProtocol ONExSparePI(OFPacketIn pi){
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

    public ONExProtocol ONExRepSparePI(OFFlowMod flowMod, OFPacketOut po){
        ONExProtocol op = new ONExProtocol(ONExProtocol.REP_SPARE_PACKET_IN, ONExGate.ID);
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

    public ONExProtocol ONExUploadLocalTopo(Topology topo){
        ONExProtocol op = new ONExProtocol(ONExProtocol.UPLOAD_LOCAL_TOPO, ONExGate.ID);
        op.setTLV(new TLV(
                TLV.Type.LOCAL_TOPO,
                topo.getLength(),
                topo.toByteBuffer().array()
        ));
        return op;
    }

    public ONExProtocol ONExGetGlobalTopo(){
        ONExProtocol op = new ONExProtocol(ONExProtocol.GET_GLOBAL_TOPO, ONExGate.ID);
        return op;
    }

    public ONExProtocol ONExRepGlobalTopo(GlobalTopo globalTopo){
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

    public ONExProtocol ONExReqGlobalFlowMod(){
        ONExProtocol op = new ONExProtocol(ONExProtocol.REQ_GLOBAL_FLOW_MOD, ONExGate.ID);
        // TODO provide further information
        return op;

    }

    public ONExProtocol ONExSCFlowMod(OFFlowMod flowMod){
        ONExProtocol op = new ONExProtocol(ONExProtocol.REP_SPARE_PACKET_IN, ONExGate.ID);
        ByteBuffer FMBB = ByteBuffer.allocate(flowMod.getLengthU());
        flowMod.writeTo(FMBB);
        op.setTLV(new TLV(
                TLV.Type.FLOWMOD,
                flowMod.getLengthU(),
                FMBB.array()
        ));

        return op;
    }

    @Override
    public void ONExFactory() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void ONExParser() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
