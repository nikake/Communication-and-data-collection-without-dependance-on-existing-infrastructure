package main.java.network;

import main.java.util.Device;

import java.util.concurrent.ConcurrentHashMap;


public class DeviceHandler implements Runnable {

    private static DeviceHandler instance = null;
    private ConcurrentHashMap<String, Device> devices = new ConcurrentHashMap<String, Device>();

    private DeviceHandler() {

    }

    public static DeviceHandler getInstance() {
        if (instance == null)
            instance = new DeviceHandler();
        return instance;
    }

    public void run(){
        //// TODO: 2017-04-22
    }

}
