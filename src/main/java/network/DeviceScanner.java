package main.java.network;

import main.java.util.Device;
import main.java.Application;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.net.Socket;

public class DeviceScanner implements Runnable {

    private CopyOnWriteArrayList<Device> devices = new CopyOnWriteArrayList<Device>();
    private static DeviceScanner instance = null;
    private final int MAX_TIMEOUT = 50;
    private ArrayList<Integer> knownIPs = new ArrayList<Integer>();

    private DeviceScanner() {

    }

    public static DeviceScanner getInstance() {
        if (instance == null)
            instance = new DeviceScanner();
        return instance;
    }

    /*
        Scan for devices every 5 seconds and add them to the collection 'devices'.
        This should be refactored and changed to future....in the future.
     */
    private void scan() {
        String[] subnet = Application.getInstance().getHostAddress();

        for (int i = 0; i < 256; i++) {
            subnet[3] = Integer.toString(i);
            try {
                InetAddress addr = InetAddress.getByName(subnet[0] + "." + subnet[1] + "." + subnet[2] + "." + subnet[3]);
                if (!knownIPs.contains(i) && !subnet[3].equals(Application.getInstance().getHostAddress()[3]) && addr.isReachable(MAX_TIMEOUT)){
                    devices.add(new Device(new Socket(addr, Application.getInstance().HOST_PORT)));
                    knownIPs.add(i);
                }

            } catch (Exception e) {
                System.out.println(e);
            }
            //Remove later, just for testing...
            if(i == 255){
                for(Device d : devices){
                    System.out.println("Found devices: ");
                    System.out.println(d.getIp());
                }
            }
        }

    }

    @Override
    public void run() {
        scan();
    }
}
