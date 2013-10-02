package nuctrl.core.impl;

import nuctrl.interfaces.IGViewListener;
import nuctrl.interfaces.IGatewayListener;
import nuctrl.gateway.Gateway;
import nuctrl.protocol.GlobalView;
import nuctrl.protocol.LocalView;

public class Core implements IGatewayListener, IGViewListener{
	private NDB networkDB;
	private LocalView localView;
	private GlobalView globalView;
	private Gateway gateway;
	
	public Core() {
		System.out.println("In Core()");
	}

	@Override
	public void handleMessageg() {
		// TODO Auto-generated method stub
		/*
		 *  copy from Beacon
		 */
		
	}

	@Override
	public void onGVupdate(GlobalView newGV) {
		this.globalView.update(newGV);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void uploadGV() {
		// TODO Auto-generated method stub
		
	}

	
	
}
