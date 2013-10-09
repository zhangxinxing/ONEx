package nuctrl.core;

import nuctrl.interfaces.IGatewayListener;

public class Core{

	public Core() {
		System.out.println("In Core()");
	}

    public void run(){
        Monitor mn = new Monitor();
        CoreDispatcher dispatcher = new CoreDispatcher(mn);
        PacketInListener pktInListener = new Alerter(dispatcher);

        pktInListener.await();    // running as a thread
    }

}

interface PacketInListener {
    public void await();
}

interface dispatcherCallback {
    public void dispatchFunc();
}

class Alerter implements PacketInListener {
    private dispatcherCallback dispatcher;

    Alerter(dispatcherCallback cb) {
        this.dispatcher = cb;
    }

    @Override
    public void await() {
        while(true){
            // wait

            // until pkt comes
            dispatcher.dispatchFunc();
        }
    }
}

class CoreDispatcher implements dispatcherCallback {
    private Monitor mn;
    private InHandler inHandler;
    private OutHandler outHandler;

    public CoreDispatcher(Monitor mn) {
        this.mn = mn;
    }

    @Override
    public void dispatchFunc(){
        // if pkt-in comes
        onPacketIn();

        // if pkt-out comes
        onPacketOut();

    }

    private void onPacketIn() {
        if (mn.isCpuBusy()){
            // dispatcher to gateway
        }

        else{
            // handle it
            inHandler.insert();
        }
    }

    private void onPacketOut() {
        outHandler.insert();
    }
}