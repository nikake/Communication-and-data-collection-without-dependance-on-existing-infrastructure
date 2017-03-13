package main.java.network;

import main.java.MachineID;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
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
        synchronized (scanLock){
            System.out.println("Printing devices...");
            for (Map.Entry<byte[], byte[]> me : devices.entrySet())
                System.out.println("[IP: " + me.getKey()[0] + "." + me.getKey()[1] + "." + me.getKey()[2] + "." + me.getKey()[3] + "][MAC: " +  (0x0 + me.getValue()[0]) + "-" +  (0x0 + me.getValue()[1]) + "-" +  (0x0 + me.getValue()[2]) + "-" +  (0x0 + me.getValue()[3]) + "-" +  (0x0 + me.getValue()[4]) + "-" +  (0x0 + me.getValue()[5]) + "]");
        }
    }


    private void scan() {
        synchronized (scanLock) {
            System.out.println("Scanning for devices...");
            byte[] machine = new byte[4];
            try {
                System.out.println("1");
                Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
                while(e.hasMoreElements()){
                    NetworkInterface ni = e.nextElement();
                    Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();

                    if(ni.getName().equals("wlan0")){
                        while(inetAddresses.hasMoreElements()){
                            InetAddress ia = inetAddresses.nextElement();
                            if(!ia.isLinkLocalAddress()){
                                String[] parts = ia.getHostAddress().split("\\.");
                            }
                        }
                    }
                }
                System.out.println("2");
            } catch(Exception e) {
                System.out.println(e);
            }
            System.out.println("[Machine IP: " + machine[0] + "." + machine[1] + "." + machine[2] + "." + machine[3]);
            if (machine == null) {
                System.out.println("IP-adress for the machine was null.");
                return;
            }
            byte[] subnet = machine;
            System.out.println("4");
            for (byte i = 0; i < 256; i++) {
                System.out.println("[IP: " + subnet[0] + "." + subnet[1] + "." + subnet[2] + "." + subnet[3]);
                subnet[3] = i;
                try {
                    System.out.println("6");
                    if (subnet != machine && InetAddress.getByAddress(subnet).isReachable(MAX_TIMEOUT))
                        devices.put(subnet, NetworkInterface.getByInetAddress(InetAddress.getByAddress(subnet)).getHardwareAddress());
                    else
                        devices.remove(subnet);
                    System.out.println("7");
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            System.out.println("8");
        }
    }

    @Override
    public void run() {
        scan();
        printDevices();
    }
}
