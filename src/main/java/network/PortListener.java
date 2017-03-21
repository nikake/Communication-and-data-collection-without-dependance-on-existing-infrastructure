package main.java.network;


public class PortListener implements Runnable {

    private static PortListener instance = null;

    private PortListener() {

    }

    public static PortListener getInstance() {
        if(instance == null)
            instance = new PortListener();
        return instance;
    }

    /*
        Listen for communication.
     */
    private void listenForOtherDevices() {

    }

    @Override
    public void run() {
        listenForOtherDevices();
    }
}
