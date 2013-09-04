package nuctrl.core.IF;

import nuctrl.protocol.GlobalView;

public interface IGViewListener {
	
	void onGVupdate(GlobalView gv);
	
	void uploadGV();

}
