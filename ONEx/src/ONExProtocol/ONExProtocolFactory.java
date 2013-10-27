package ONExProtocol;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
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
    private static Logger log = Logger.getLogger(ONExProtocolFactory.class);

    public static ONExPacket ONExSparePI(OFPacketIn pi){
        ONExPacket op = new ONExPacket(ONExPacket.SPARE_PACKET_IN, -1);
        ByteBuffer PIBB = ByteBuffer.allocate(pi.getLength());
        pi.writeTo(PIBB);
        op.setTLV(new TLV(
                TLV.Type.PACKET_IN,
                pi.getLengthU(),
                PIBB.array()
        ));

        op.setTLV(new TLV(
                TLV.Type.SRC_HOST,
                8,
                null // to be filled when Server daemon dispatch it
        ));
        return op;
    }

    public static ONExPacket ONExResSparePI(OFFlowMod flowMod, OFPacketOut po){
        ONExPacket op = new ONExPacket(ONExPacket.RES_SPARE_PACKET_IN, -1);
        ByteBuffer FMBB = ByteBuffer.allocate(flowMod.getLength());
        flowMod.writeTo(FMBB);

        ByteBuffer POBB = ByteBuffer.allocate(po.getLength());
        po.writeTo(POBB);

        op.setTLV(new TLV(
                TLV.Type.FLOW_MOD,
                flowMod.getLengthU(),
                FMBB.array()
        ));

        op.setTLV(new TLV(
                TLV.Type.PACKET_OUT,
                po.getLengthU(),
                POBB.array()
        ));

        op.setTLV(new TLV(
                TLV.Type.SRC_HOST,
                8,
                null // to be filled when Server daemon dispatch it
        ));

        return op;
    }

    public static ONExPacket ONExUploadLocalTopo(GlobalTopo topo){
        ONExPacket op = new ONExPacket(ONExPacket.UPLOAD_LOCAL_TOPO, -1);
        op.setTLV(new TLV(
                TLV.Type.GLOBAL_TOPO,
                topo.getLength(),
                topo.toByteBuffer().array()
        ));
        return op;
    }

    public static ONExPacket ONExRequestGlobalTopo(){
        // with no TLV
        ONExPacket op = new ONExPacket(ONExPacket.REQUEST_GLOBAL_TOPO, -1);
        return op;
    }

    public static ONExPacket ONExResGlobalTopo(GlobalTopo globalTopo){
        // S -> C
        if(globalTopo == null){
            log.error("globalTopo cannot be null");
            return null;
        }
        ONExPacket op = new ONExPacket(ONExPacket.RETURN_GLOBAL_TOPO, -1);
        op.setTLV(new TLV(
                TLV.Type.GLOBAL_TOPO,
                globalTopo.getLength(),
                globalTopo.toByteBuffer().array()
        ));

        return op;
    }

    public static ONExPacket ONExReqGlobalFlowMod(GlobalFlowMod globalFlowMod){
        if (globalFlowMod == null){
            log.error("globalFlowMod cannot be null");
            return null;
        }
        ONExPacket op = new ONExPacket(ONExPacket.REQ_GLOBAL_FLOW_MOD, -1);
        op.setTLV(new TLV(
                TLV.Type.GLOBAL_FLOW_MOD,
                globalFlowMod.getLength(),
                globalFlowMod.toByteBuffer().array()
        ));
        return op;

    }

    public static ONExPacket ONExSCFlowMod(OFFlowMod flowMod){
        ONExPacket op = new ONExPacket(ONExPacket.RES_SPARE_PACKET_IN, -1);
        ByteBuffer FMBB = ByteBuffer.allocate(flowMod.getLength());
        flowMod.writeTo(FMBB);
        op.setTLV(new TLV(
                TLV.Type.FLOW_MOD,
                flowMod.getLengthU(),
                FMBB.array()
        ));

        return op;
    }

    public static ONExPacket parser(ByteBuffer msg) {
        if (msg == null){
            log.error("msg == null");
            return null;
        }
        if(msg.hasRemaining()){
            ONExPacket op = new ONExPacket(0xFFFFFFFF, -1);
            op.setHeader(msg);
            op.setTLVs(msg);
            return op;
        }
        else{
            log.error("msg == empty");
            return null;
        }
    }

    public static ONExPacket parser(byte[] array){
        ByteBuffer buf = ByteBuffer.wrap(array);
        return parser(ByteBuffer.wrap(array));
    }
}
