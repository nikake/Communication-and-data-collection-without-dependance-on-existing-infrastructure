package main.java;

import main.java.network.DeviceScanner;

/**
 * The main class of the application.
 */
public class Application {

    private static boolean keepRunning = true;

    public static void main(String[] args) {

        while(keepRunning) {
            System.out.println("Initiating thread...");
            DeviceScanner ds = DeviceScanner.getInstance();
            Thread scan = new Thread(ds);
            try {
                scan.start();
                scan.join();
            } catch (InterruptedException e) {
                System.out.println("Scan was interrupted.");
            }
        }
    }

    public void shutdown() {
        keepRunning = false;
    }
}
