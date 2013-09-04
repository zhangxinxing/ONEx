package nuctrl.core.IF;

import nuctrl.protocol.EDispatchTarget;
import nuctrl.protocol.GatewayMsg;

public interface IDispatcher {
	void dispatchTo(EDispatchTarget target, GatewayMsg msg);
	
	void linkToLeft();
	
	void linkToRight();
}
