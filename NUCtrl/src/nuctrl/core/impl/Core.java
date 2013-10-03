package nuctrl.core.impl;

import nuctrl.gateway.Gateway;
import nuctrl.interfaces.IGatewayListener;
import nuctrl.protocol.GlobalView;
import nuctrl.protocol.LocalView;

public class Core implements IGatewayListener{
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


	
	
}
