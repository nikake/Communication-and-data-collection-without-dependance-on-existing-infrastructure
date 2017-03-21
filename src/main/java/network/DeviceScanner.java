package main.java.network;

import main.java.util.Device;

import java.util.concurrent.CopyOnWriteArrayList;

public class DeviceScanner implements Runnable {

    private CopyOnWriteArrayList<Device> devices = new CopyOnWriteArrayList<Device>();
    private static DeviceScanner instance = null;

    private DeviceScanner() {

    }

    public static DeviceScanner getInstance() {
        if (instance == null)
            instance = new DeviceScanner();
        return instance;
    }

    /*
        Scan for devices every 5 seconds and add them to the collection 'devices'.
     */
    private void scan() {

    }

    @Override
    public void run() {
        scan();
    }
}
