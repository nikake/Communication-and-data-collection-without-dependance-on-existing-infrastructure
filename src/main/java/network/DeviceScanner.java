package main.java.network;

import main.java.MachineID;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.Map;

public class DeviceScanner implements Runnable {

    private static final int MAX_TIMEOUT = 1000;

    private static DeviceScanner instance = null;
    private HashMap<byte[], byte[]> devices = null;
    private Object scanLock = new Object();

    private DeviceScanner() {
        devices = new HashMap<byte[], byte[]>();
    }

    public static DeviceScanner getInstance() {
        if(instance == null)
            instance = new DeviceScanner();
        return instance;
    }

    public HashMap<byte[], byte[]> getDevices() {
        synchronized (scanLock) {
            return devices;
        }
    }

    public void printDevices() {
        for (Map.Entry<byte[], byte[]> me : devices.entrySet())
            System.out.print("[IP: " + me.getKey()[0] + "." + me.getKey()[1] + "." + me.getKey()[2] + "." + me.getKey()[3] + "][MAC: " +  (0x0 + me.getValue()[0]) + "-" +  (0x0 + me.getValue()[1]) + "-" +  (0x0 + me.getValue()[2]) + "-" +  (0x0 + me.getValue()[3]) + "-" +  (0x0 + me.getValue()[4]) + "-" +  (0x0 + me.getValue()[5]) + "]");
    }

    private void scan() {
        synchronized (scanLock) {
            byte[] machine = null;
            try {
                machine = InetAddress.getLocalHost().getAddress();
            } catch(Exception e) {
                System.out.println(e);
            }
            if (machine == null) {
                System.out.println("IP-adress for the machine was null.");
                return;
            }
            byte[] subnet = machine;
            for (byte i = 0; i < 256; i++) {
                subnet[3] = i;
                try {
                    if (subnet != machine && InetAddress.getByAddress(subnet).isReachable(MAX_TIMEOUT))
                        devices.put(subnet, NetworkInterface.getByInetAddress(InetAddress.getByAddress(subnet)).getHardwareAddress());
                    else
                        devices.remove(subnet);
                } catch (Exception e) {

                }
            }
        }
    }

    @Override
    public void run() {
        scan();
    }
}
