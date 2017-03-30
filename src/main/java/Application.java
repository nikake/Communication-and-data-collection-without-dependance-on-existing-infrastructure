package main.java;

import main.java.log.LogWriter;
import main.java.log.Logger;
import main.java.network.DeviceScanner;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.io.File;

/**
 * The main class of the application.
 */
public class Application implements Runnable {

    private File logFile = new File("./Log/App.log");
    private boolean logAppend = true;
    private Logger log;

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
        startLogger();
        log.info("Starting up!");
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

    private void startLogger() {
        LogWriter.setLogFile(logFile);
        LogWriter.setAppend(logAppend);
        LogWriter lw = LogWriter.getInstance();
        if (lw != null)
            new Thread(lw).start();
        log = Logger.getLogger(this.getClass().getSimpleName());
    }

    public static void main(String[] args) {
        Application app = Application.getInstance();
        app.run();
    }
}
