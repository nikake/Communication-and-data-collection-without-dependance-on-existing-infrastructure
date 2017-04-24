package main.java.network;

import main.java.Application;
import main.java.log.Logger;
import main.java.util.Device;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class DeviceHandler implements Runnable {

    private static CopyOnWriteArrayList<Device> devices = new CopyOnWriteArrayList<>();
    private static DeviceHandler instance = null;
    private DeviceScanner deviceScanner = new DeviceScanner();
    private static ArrayList<String> foundDevices = new ArrayList<>();

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
        if(foundDevices.isEmpty()){
            foundDevices = deviceScanner.scan();
            for(String s : foundDevices){
                try{
                    RemoteClient rc = new RemoteClient(s, Application.HOST_PORT);
                    Thread remote = new Thread(rc);
                    remote.run();;
                } catch (Exception e){
                    Logger.error("");
                }
            }
            if(foundDevices.isEmpty()){
                System.out.println("No devices found");
            }
        }
    }

}
