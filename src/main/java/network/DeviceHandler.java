package main.java.network;

import main.java.util.Device;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class DeviceHandler implements Runnable {

    private static CopyOnWriteArrayList<Device> devices = new CopyOnWriteArrayList<>();
    private static DeviceHandler instance = null;
    private DeviceScanner deviceScanner = new DeviceScanner();

    private DeviceHandler() {

    }

    public static DeviceHandler getInstance() {
        if (instance == null)
            instance = new DeviceHandler();
        return instance;
    }

    public void addDevice(Device device){
        devices.add(device);
    }

    public boolean removeDevice(Device device){
        return devices.remove(device);
    }

    public boolean deviceExists(Device device){
        return devices.contains(device);
    }

    public void run(){
        if(devices.isEmpty()){
            ArrayList<String> foundDevices = deviceScanner.scan();
            for(String s : foundDevices){
                System.out.println(s);
            }
            if(foundDevices.isEmpty()){
                System.out.println("No devices found");
            }
        }
    }

}
