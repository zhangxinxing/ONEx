package nuctrl.core.test;

import nuctrl.core.impl.Gateway;
import nuctrl.protocol.EDispatchTarget;
import nuctrl.protocol.GatewayMsg;

public class TGateway {

	public static void main(String[] args) {
		String local = "127.0.0.1";
		Gateway g1 = new Gateway("g1", local, 12345, local, 12346);
		Gateway g2 = new Gateway("g2", local, 12346, local, 12345);
		g1.init();
		g2.init();
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// XXX fot test only
		GatewayMsg msg = new GatewayMsg('A', 'B');
		g1.dispatchTo(EDispatchTarget.TO_LEFT, msg);
	}

}
