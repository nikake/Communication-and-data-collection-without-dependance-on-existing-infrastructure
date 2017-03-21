package main.java;

import main.java.network.DeviceScanner;

import java.net.ServerSocket;

/**
 * The main class of the application.
 */
public class Application implements Runnable {
    public static final int HOST_PORT = 8070;

    private static ServerSocket host;
    private static boolean keepRunning = true;

    public Application() {
        setHost();
    }

    private void setHost() {
        host = null;
    }

    public String[] getHostAddress() {
        return new String[4];
    }

    /*
        Run threads for DeviceScanner and PortListener.
     */
    public void run() {
        while(keepRunning) {
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
    }

    public static void main(String[] args) {
        Application app = new Application();
        app.run();
    }
}
