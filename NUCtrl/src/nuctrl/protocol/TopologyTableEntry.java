package nuctrl.protocol;

import java.net.InetAddress;

/**
 * User: fan
 * Date: 13-10-3
 * Time: PM10:56
 */
public class TopologyTableEntry {
    private InetAddress src;
    private InetAddress dst;
    private Object attr;

    public TopologyTableEntry(InetAddress src, InetAddress dst) {
        this.src = src;
        this.dst = dst;
        this.attr = null;
    }

    public InetAddress getSrc(){
        return src;
    }

    public void setAttr(Object attr) {
        this.attr = attr;
    }

    public InetAddress getDst() {
        return dst;
    }

    public Object getAttr() {
        return attr;
    }

    public void parseAttr(){

    }

    public String toString(){
        if(attr == null){
            return String.format("%s -> %s (null)",
                    this.src.toString(),
                    this.dst.toString()
            );
        } else{
        return String.format("%s -> %s (%s)",
                this.src.toString(),
                this.dst.toString(),
                this.attr.toString()
        );

        }
    }
}
