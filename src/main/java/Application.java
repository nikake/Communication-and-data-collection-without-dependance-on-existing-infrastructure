package main.java;

import main.java.network.DeviceScanner;

import java.net.InetAddress;
import java.net.ServerSocket;

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
        try {
            host = new ServerSocket(HOST_PORT, 50, InetAddress.getByName("127.0.0.1"));
        } catch (Exception e) {
            System.out.println("Error setting host: " + e);
        }
    }

    public String[] getHostAddress() {
        InetAddress hostAddress = host.getInetAddress();
        return hostAddress.getHostAddress().split("\\.");
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
