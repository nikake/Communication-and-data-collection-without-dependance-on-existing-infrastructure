package main.java.network;

import main.java.Application;
import main.java.log.Logger;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.*;

public class DeviceScanner {
    private final static int MAX_CLIENTS = 256;
    private final static int MAX_TIMEOUT = 50;
    private ArrayList<String> foundDevices;
    private ArrayList<InetAddress> addresses;
    private final static Executor POOL = Executors.newFixedThreadPool(MAX_CLIENTS);

    public DeviceScanner() {
        foundDevices = new ArrayList<>();
        addresses = new ArrayList<>();
        initAddresses();
    }

    private void initAddresses() {
        String[] subnet = Application.getInstance().getHostAddress();
        for (int i = 0; i < 256; i++) {
            subnet[3] = Integer.toString(i);
            try {
                InetAddress addr = InetAddress.getByName(subnet[0] + "." + subnet[1] + "." + subnet[2] + "." + subnet[3]);
                if(!subnet[3].equals(Application.getInstance().getHostAddress()[3]))
                    addresses.add(addr);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    /*
        Scan for devices every 50 (minimum) milliseconds and add them to the collection 'foundDevices' if it has not already been discovered and is reachable.
     */
    public ArrayList<String> scan() {
        System.out.println("DeviceScanner: Before parallel!");
        addresses.parallelStream().forEach(addr -> {
            System.out.println("Running scan for address: " + addr.getHostAddress());
            try {
                if (!foundDevices.contains(addr) && addr.isReachable(MAX_TIMEOUT))
                    foundDevices.add(addr.getHostAddress());
            } catch (Exception e) {
                Logger.error("Exception while adding device with IP " + addr + " to foundDevices while scanning. Exception: " + e.getMessage());
            }
        });
        System.out.println("DeviceScanner: After parallel!");
        return foundDevices;
    }
}
