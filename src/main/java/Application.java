package main.java;

import main.java.network.DeviceHandler;
import main.java.util.Device;
import main.java.util.CommandExecutor;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * The main class of the application.
 */
public class Application implements Runnable {

    public static final int HOST_PORT = 8000;
    private static Application instance;
    private static Device localDevice;

    private static ServerSocket host;
    private static boolean keepRunning = true;

    private Application() {
        localDevice = setHost();
    }

    public static Application getInstance() {
        if(instance == null)
            instance = new Application();
        return instance;
    }

    private Device setHost() {
        String localAddress = "";
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

            while(e.hasMoreElements()){
                NetworkInterface ni = e.nextElement();
                Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                if(ni.getName().equals("wlan0")){
                    while(inetAddresses.hasMoreElements()){
                        InetAddress ia = inetAddresses.nextElement();
                        if(!ia.isLinkLocalAddress()){
                            localAddress = ia.getHostAddress();
                        }
                    }
                }
            }
        } catch(Exception e) {
            System.out.println(e);
        }

        try {
            return new Device(localAddress, setMacAddress(InetAddress.getByName(localAddress)), setBtMacAddress());
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public String[] getHostAddress() {
        return host.getInetAddress().getHostAddress().split("\\.");
    }

    public void close() {
        keepRunning = false;
    }

    /*
        Run threads for DeviceScanner and PortListener.
     */
    public void run() {
        try {
            while (keepRunning) {
                DeviceHandler dh = DeviceHandler.getInstance();
                Thread scan = new Thread(dh);
                try {
                    scan.start();
                    scan.join();
                } catch (InterruptedException e) {
                    System.out.println("Scan was interrupted.");
                }
            }
        } finally {
            try {
                if (host != null)
                    host.close();
            } catch (Exception e) {
                System.out.println("Error closing down Application: " + e);
            }
        }
    }

    private String setMacAddress(InetAddress addr){
        StringBuilder sb = new StringBuilder();
        try {
            NetworkInterface netInterface = NetworkInterface.getByInetAddress(addr);
            byte[] mac = netInterface.getHardwareAddress();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
        } catch (Exception e){
            System.out.println(e);
        }

        return sb.toString();
    }

    private String setBtMacAddress(){
        CommandExecutor c = new CommandExecutor();
        String[] results = c.execute("hcitool dev".split("\\s+"));
        for (String s : results) {
            if (s.contains("hci0")) {
                return s.split("\\s+")[1];
            }
        }
        return "";
    }

    public static void main(String[] args) {
        Application app = Application.getInstance();
        app.run();
    }
}
