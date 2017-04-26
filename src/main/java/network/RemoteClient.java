package main.java.network;

import main.java.Application;
import main.java.log.Logger;
import main.java.util.Device;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RemoteClient implements Runnable {

    private String hostIp;
    private int hostPort;
    private Device hostDevice = null;


    public RemoteClient(String hostIp, int hostPort) {
        this.hostIp = hostIp;
        this.hostPort = hostPort;
    }

    public Device getHostDevice() {
        int attempt = 0;
        while(hostDevice == null && attempt < 10)
            try {
                Thread.sleep(1000);
                attempt++;
            } catch (Exception e){

            }
        return hostDevice != null ? hostDevice: Application.NULL_DEVICE;
    }

    @Override
    public void run() {
        Socket host = null;
        try {
            host = new Socket(hostIp, hostPort);
            Logger.info("Connected to remote host with IP: " + host.getRemoteSocketAddress());
            System.out.println("Connected to remote host with IP: " + host.getRemoteSocketAddress());
            ObjectInputStream ois = new ObjectInputStream(host.getInputStream());
            do {
                try {
                    hostDevice = (Device) ois.readObject();
                } catch (Exception e) {
                    Logger.info("Attempted to set clientDevice, but failed.\n\n" + e.getMessage());
                }
            } while (hostDevice == null);
            Logger.info("Communication with new device established. New device: " + hostDevice);
            System.out.println("Communication with new device established. New device: " + hostDevice);

            while(true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Logger.error("Error during connection to remote host with IP: " + hostIp);
        } finally {
            try {
                if (host != null)
                    host.close();
            } catch (Exception e) {
                Logger.error("Error closing connection to remote host with IP: " + host.getRemoteSocketAddress());
            }
        }
    }
}
