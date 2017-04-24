package network;

import main.java.util.Device;
import main.java.network.DeviceScanner;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.net.Socket;


public class DeviceHandler implements Runnable {

    private static DeviceHandler instance = null;
    private ConcurrentHashMap<Socket, Device> devices = new ConcurrentHashMap<Socket, Device>();
    private DeviceScanner deviceScanner = new DeviceScanner();

    private DeviceHandler() {

    }

    public static DeviceHandler getInstance() {
        if (instance == null)
            instance = new DeviceHandler();
        return instance;
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
