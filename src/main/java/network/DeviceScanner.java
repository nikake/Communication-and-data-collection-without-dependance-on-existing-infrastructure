package main.java.network;

import main.java.util.Device;
import main.java.Application;

import java.net.InetAddress;
import java.util.ArrayList;

public class DeviceScanner {

    private final int MAX_TIMEOUT = 50;
    private ArrayList<String> foundDevices = new ArrayList<>();

    public DeviceScanner() {

    }

    /*
        Scan for devices every 5 seconds and add them to the collection 'devices'.
        This should be refactored and changed to future....in the future.
     */
    public ArrayList<String> scan() {
        String[] subnet = Application.getInstance().getHostAddress();

        for (int i = 0; i < 256; i++) {
            subnet[3] = Integer.toString(i);
            try {
                InetAddress addr = InetAddress.getByName(subnet[0] + "." + subnet[1] + "." + subnet[2] + "." + subnet[3]);
                if (!foundDevices.contains(i) && !subnet[3].equals(Application.getInstance().getHostAddress()[3]) && addr.isReachable(MAX_TIMEOUT)){
                    foundDevices.add(addr.getHostAddress());
                }

            } catch (Exception e) {
                System.out.println(e);
            }
        }

        return foundDevices;

    }

}
