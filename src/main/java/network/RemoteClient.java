package main.java.network;

import main.java.log.Logger;
import main.java.util.Device;

import java.io.ObjectOutputStream;
import java.net.Socket;

public class RemoteClient implements Runnable {

    private Device localDevice;
    private String hostIp;
    private int hostPort;

    public RemoteClient(Device localDevice, String hostIp, int hostPort) {
        this.localDevice = localDevice;
        this.hostIp = hostIp;
        this.hostPort = hostPort;
    }

    @Override
    public void run() {
        Socket host = null;
        try {
            host = new Socket(hostIp, hostPort);
            Logger.info("Connected to remote host with IP: " + host.getRemoteSocketAddress());

            ObjectOutputStream oos = new ObjectOutputStream(host.getOutputStream());
            oos.writeObject(localDevice);

            while(true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Logger.error("Error during connection to remote host with IP: " + host.getRemoteSocketAddress());
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
