package org.openflow.protocol.statistics;

import java.nio.ByteBuffer;
import java.util.List;

import org.openflow.protocol.OFMatch;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.factory.OFActionFactory;
import org.openflow.protocol.factory.OFActionFactoryAware;
import org.openflow.util.U16;

/**
 * Represents an ofp_flow_stats structure
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFFlowStatisticsReply implements OFStatistics, OFActionFactoryAware {
    public static int MINIMUM_LENGTH = 88;

    protected OFActionFactory actionFactory;
    protected short length = (short) MINIMUM_LENGTH;
    protected byte tableId;
    protected OFMatch match;
    protected int durationSeconds;
    protected int durationNanoseconds;
    protected short priority;
    protected short idleTimeout;
    protected short hardTimeout;
    protected long cookie;
    protected long packetCount;
    protected long byteCount;
    protected List<OFAction> actions;

    /**
     * @return the tableId
     */
    public byte getTableId() {
        return tableId;
    }

    /**
     * @param tableId the tableId to set
     */
    public OFFlowStatisticsReply setTableId(byte tableId) {
        this.tableId = tableId;
        return this;
    }

    /**
     * @return the match
     */
    public OFMatch getMatch() {
        return match;
    }

    /**
     * @param match the match to set
     */
    public OFFlowStatisticsReply setMatch(OFMatch match) {
        this.match = match;
        return this;
    }

    /**
     * @return the durationSeconds
     */
    public int getDurationSeconds() {
        return durationSeconds;
    }

    /**
     * @param durationSeconds the durationSeconds to set
     */
    public OFFlowStatisticsReply setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
        return this;
    }

    /**
     * @return the durationNanoseconds
     */
    public int getDurationNanoseconds() {
        return durationNanoseconds;
    }

    /**
     * @param durationNanoseconds the durationNanoseconds to set
     */
    public OFFlowStatisticsReply setDurationNanoseconds(int durationNanoseconds) {
        this.durationNanoseconds = durationNanoseconds;
        return this;
    }

    /**
     * @return the priority
     */
    public short getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public OFFlowStatisticsReply setPriority(short priority) {
        this.priority = priority;
        return this;
    }

    /**
     * @return the idleTimeout
     */
    public short getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * @param idleTimeout the idleTimeout to set
     */
    public OFFlowStatisticsReply setIdleTimeout(short idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

    /**
     * @return the hardTimeout
     */
    public short getHardTimeout() {
        return hardTimeout;
    }

    /**
     * @param hardTimeout the hardTimeout to set
     */
    public OFFlowStatisticsReply setHardTimeout(short hardTimeout) {
        this.hardTimeout = hardTimeout;
        return this;
    }

    /**
     * @return the cookie
     */
    public long getCookie() {
        return cookie;
    }

    /**
     * @param cookie the cookie to set
     */
    public OFFlowStatisticsReply setCookie(long cookie) {
        this.cookie = cookie;
        return this;
    }

    /**
     * @return the packetCount
     */
    public long getPacketCount() {
        return packetCount;
    }

    /**
     * @param packetCount the packetCount to set
     */
    public OFFlowStatisticsReply setPacketCount(long packetCount) {
        this.packetCount = packetCount;
        return this;
    }

    /**
     * @return the byteCount
     */
    public long getByteCount() {
        return byteCount;
    }

    /**
     * @param byteCount the byteCount to set
     */
    public OFFlowStatisticsReply setByteCount(long byteCount) {
        this.byteCount = byteCount;
        return this;
    }

    /**
     * @param length the length to set
     */
    public void setLength(short length) {
        this.length = length;
    }

    @Override
    public int getLength() {
        return U16.f(length);
    }

    /**
     * @param actionFactory the actionFactory to set
     */
    @Override
    public void setActionFactory(OFActionFactory actionFactory) {
        this.actionFactory = actionFactory;
    }

    /**
     * @return the actions
     */
    public List<OFAction> getActions() {
        return actions;
    }

    /**
     * @param actions the actions to set
     */
    public OFFlowStatisticsReply setActions(List<OFAction> actions) {
        this.actions = actions;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        this.length = data.getShort();
        this.tableId = data.get();
        data.get(); // pad
        if (this.match == null)
            this.match = new OFMatch();
        this.match.readFrom(data);
        this.durationSeconds = data.getInt();
        this.durationNanoseconds = data.getInt();
        this.priority = data.getShort();
        this.idleTimeout = data.getShort();
        this.hardTimeout = data.getShort();
        data.getInt(); // pad
        data.getShort(); // pad
        this.cookie = data.getLong();
        this.packetCount = data.getLong();
        this.byteCount = data.getLong();
        if (this.actionFactory == null)
            throw new RuntimeException("OFActionFactory not set");
        this.actions = this.actionFactory.parseActions(data, getLength() -
                MINIMUM_LENGTH);
    }

    @Override
    public void writeTo(ByteBuffer data) {
        data.putShort(this.length);
        data.put(this.tableId);
        data.put((byte) 0);
        this.match.writeTo(data);
        data.putInt(this.durationSeconds);
        data.putInt(this.durationNanoseconds);
        data.putShort(this.priority);
        data.putShort(this.idleTimeout);
        data.putShort(this.hardTimeout);
        data.getInt(); // pad
        data.getShort(); // pad
        data.putLong(this.cookie);
        data.putLong(this.packetCount);
        data.putLong(this.byteCount);
        if (actions != null) {
            for (OFAction action : actions) {
                action.writeTo(data);
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 419;
        int result = 1;
        result = prime * result + (int) (byteCount ^ (byteCount >>> 32));
        result = prime * result + (int) (cookie ^ (cookie >>> 32));
        result = prime * result + durationNanoseconds;
        result = prime * result + durationSeconds;
        result = prime * result + hardTimeout;
        result = prime * result + idleTimeout;
        result = prime * result + length;
        result = prime * result + ((match == null) ? 0 : match.hashCode());
        result = prime * result + (int) (packetCount ^ (packetCount >>> 32));
        result = prime * result + priority;
        result = prime * result + tableId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OFFlowStatisticsReply)) {
            return false;
        }
        OFFlowStatisticsReply other = (OFFlowStatisticsReply) obj;
        if (byteCount != other.byteCount) {
            return false;
        }
        if (cookie != other.cookie) {
            return false;
        }
        if (durationNanoseconds != other.durationNanoseconds) {
            return false;
        }
        if (durationSeconds != other.durationSeconds) {
            return false;
        }
        if (hardTimeout != other.hardTimeout) {
            return false;
        }
        if (idleTimeout != other.idleTimeout) {
            return false;
        }
        if (length != other.length) {
            return false;
        }
        if (match == null) {
            if (other.match != null) {
                return false;
            }
        } else if (!match.equals(other.match)) {
            return false;
        }
        if (packetCount != other.packetCount) {
            return false;
        }
        if (priority != other.priority) {
            return false;
        }
        if (tableId != other.tableId) {
            return false;
        }
        return true;
    }

    @Override
    public int computeLength() {
        int l = MINIMUM_LENGTH;
        if (actions != null) {
            for (OFAction action : actions) {
                l += action.getLengthU();
            }
        }
        this.length = U16.t(l);
        return l;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFFlowStatisticsReply [actionFactory=" + actionFactory
                + ", length=" + length + ", tableId=" + tableId + ", match="
                + match + ", durationSeconds=" + durationSeconds
                + ", durationNanoseconds=" + durationNanoseconds
                + ", priority=" + priority + ", idleTimeout=" + idleTimeout
                + ", hardTimeout=" + hardTimeout + ", cookie=" + cookie
                + ", packetCount=" + packetCount + ", byteCount=" + byteCount
                + ", actions=" + actions + "]";
    }
}
