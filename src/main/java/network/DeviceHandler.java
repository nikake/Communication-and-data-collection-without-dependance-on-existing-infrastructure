package main.java.network;

import main.java.Application;
import main.java.log.Logger;
import main.java.util.Device;
import main.java.util.InformationHolder;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class DeviceHandler implements Runnable {

    private static ConcurrentHashMap<String, Device> devices = new ConcurrentHashMap<>();
    private static DeviceHandler instance = null;
    private DeviceScanner deviceScanner = new DeviceScanner();
    private boolean updated = false;

    private DeviceHandler() {

    }

    public static DeviceHandler getInstance() {
        if (instance == null)
            instance = new DeviceHandler();
        return instance;
    }

    public void addDevice(String ip, Device device){
        Logger.info("Added device: " + device);
        devices.put(ip, device);
    }

    public Device removeDevice(String ip){
        Device removedDevice = devices.remove(ip);
        Logger.info("Removed device: " + removedDevice);
        return removedDevice;
    }

    public boolean deviceExists(String ip){
        return devices.containsKey(ip);
    }

    public void updateDevices(ArrayList<String> foundIps){
        System.out.println("Updating devices");
        updated = false;
        foundIps.parallelStream().forEach(ip -> {
            if(!deviceExists(ip)){
                try{
                    RemoteClient rc = new RemoteClient(ip, Application.HOST_PORT);
                    Thread remote = new Thread(rc);
                    remote.start();
                    Device d = rc.getHostDevice();
                    if(!d.equals(Application.NULL_DEVICE)) {
                        addDevice(ip, rc.getHostDevice());
                        updated = true;
                    }
                } catch (Exception e){
                    Logger.error("");
                }
            }
        });
        devices.forEach((String ip, Device d) -> {
            if(!foundIps.contains(ip)) {
                removeDevice(ip);
                updated = true;
            }
        });
        if(updated) {
            updateInformation();
            Logger.info("Device list updated: ");
            devices.forEach((String ip, Device d) -> Logger.info(d.toString()));
        }
    }

    private void updateInformation(){
        InformationHolder.setDevices((devices.values()));
    }

    public void run(){
        //Kör scan, populera devices med nya ips som får värde null. Samt ta bort ips som inte längre fångas upp.
        while(true)
            updateDevices(deviceScanner.scan());
    }

}
