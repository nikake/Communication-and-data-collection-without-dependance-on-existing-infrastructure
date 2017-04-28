package main.java.network;

import main.java.Application;
import main.java.log.Logger;
import main.java.util.Device;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RemoteClient implements Runnable {

    private Socket host;
    private Device hostDevice = null;
    private ObjectOutputStream hostWriter = null;
    private ObjectInputStream hostReader = null;

    private String ip;
    private int port;


    public RemoteClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    private void connectToHost() throws IOException {
        host = new Socket(ip, port);
        Logger.info("Connected to remote host with IP: " + host.getRemoteSocketAddress());
        System.out.println("Connected to remote host with IP: " + host.getRemoteSocketAddress());

    }

    private void initiateStreams() throws IOException {
        hostWriter = new ObjectOutputStream(host.getOutputStream());
        hostReader = new ObjectInputStream(host.getInputStream());
    }

    public Device getHostDevice() {
        int attempt = 0;
        while(hostDevice == null && attempt < 10)
            try {
                Thread.sleep(100);
                attempt++;
            } catch (Exception e){

            }
        return hostDevice != null ? hostDevice: Application.NULL_DEVICE;
    }

    private void setHostDevice() {
        do {
            try {
                hostDevice = (Device) hostReader.readObject();
            } catch (Exception e) {
                Logger.info("Attempted to set clientDevice, but failed.\n\n" + e.getMessage());
            }
        } while (hostDevice == null);
        Logger.info("Communication with new device established. New device: " + hostDevice);
        System.out.println("Communication with new device established. New device: " + hostDevice);
    }

    private void readHostMessages() throws IOException, ClassNotFoundException {
        Object message = null;
        while((message = hostReader.readObject()) != null) {

        }
    }

    private void close() throws IOException {
        if (host != null)
            host.close();
    }

    @Override
    public void run() {
        try {
            connectToHost();
            initiateStreams();
            setHostDevice();
            readHostMessages();
        } catch (Exception e) {
            Logger.error("Error during connection to remote host with IP: " + ip + "\n\n" + e.getMessage());
        } finally {
            try {
                close();
            } catch (Exception e) {
                Logger.error("Error closing connection to remote host with IP: " + host.getRemoteSocketAddress());
            }
        }
    }
}
