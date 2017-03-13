package main.java;

import main.java.network.DeviceScanner;

/**
 * The main class of the application.
 */
public class Application {

    private static boolean keepRunning = true;

    public static void main(String[] args) {

        while(keepRunning) {
            DeviceScanner ds = DeviceScanner.getInstance();
            Thread scan = new Thread(ds);
            try {
                scan.start();
                scan.join();
            } catch (InterruptedException e) {
                System.out.println("Scan was interrupted.");
            }
            ds.printDevices();
        }
    }

    public void shutdown() {
        keepRunning = false;
    }
}
