package main.java;

import main.java.network.DeviceScanner;

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

    private static ServerSocket host;
    private static boolean keepRunning = true;

    private Application() {
        setHost();
    }

    public static Application getInstance() {
        if(instance == null)
            instance = new Application();
        return instance;
    }

    private void setHost() {
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
            host = new ServerSocket(HOST_PORT, 50, InetAddress.getByName(localAddress));
        } catch (Exception e) {
            System.out.println("Error setting host: " + e);
        }
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
                DeviceScanner ds = DeviceScanner.getInstance();
                Thread scan = new Thread(ds);
                try {
                    scan.start();
                    scan.join();
                } catch (InterruptedException e) {
                    System.out.println("Scan was interrupted.");
                }
                //ds.printDevices();
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

    public static void main(String[] args) {
        Application app = Application.getInstance();
        app.run();
    }
}
