package nuctrl.protocol;

import java.util.Date;

public class GlobalView extends View{
	int id;
	Date lastUpdateTime;
	
	public void update(GlobalView gv){
		this.graph = gv.graph;
	}
}
