package main.java.network;

import main.java.Application;
import main.java.log.Logger;
import main.java.messaging.DataPacket;
import main.java.messaging.Message;
import main.java.messaging.MessageReader;
import main.java.util.Device;
import main.java.util.InformationHolder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        InformationHolder.remoteClients.put(host.getInetAddress().getHostAddress(), this);
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

    private void sendLocalDeviceDataToHost() throws IOException {
        hostWriter.writeObject(Application.getLocalDevice());
    }

    private void readHostMessages() throws IOException, ClassNotFoundException {
        Object message;
        while((message = hostReader.readObject()) != null) {
            if (message instanceof DataPacket)
                parsePacket((DataPacket) message);
        }
    }

    private void parsePacket(DataPacket dataPacket) throws IOException {
        if (dataPacket.RECEIVER.equals(Application.getLocalDevice())) {
            readMessage(dataPacket);
        } else {
            // Skicka vidare enligt dataPacket.ROUTING_TABLE
            Logger.info("Received data packet to route forward to unit " + dataPacket.RECEIVER);
        }
    }

    private void readMessage(DataPacket dataPacket) throws IOException {
        DataPacket returnPacket = null;
        Logger.info("RemoteClient - Received from: " + dataPacket.SENDER.ipAddress + " Message: " + dataPacket.MESSAGE.name());
        switch (dataPacket.MESSAGE) {
            case SET_LEFT_NEIGHBOUR_OK:
                if (!PairingHandler.getInstance().setLeft(dataPacket.SENDER, dataPacket.MESSAGE)) {
                    returnPacket = new DataPacket(Application.getLocalDevice(), hostDevice, Message.SET_LEFT_NEIGHBOUR_FAILURE, null, null);
                    hostWriter.writeObject(returnPacket);
                }
                break;
            case SET_LEFT_NEIGHBOUR_DENIED:
                PairingHandler.getInstance().setLeft(dataPacket.SENDER, dataPacket.MESSAGE);
                break;
            case SET_RIGHT_NEIGHBOUR_OK:
                if (!PairingHandler.getInstance().setRight(dataPacket.SENDER, dataPacket.MESSAGE)) {
                    returnPacket = new DataPacket(Application.getLocalDevice(), hostDevice, Message.SET_RIGHT_NEIGHBOUR_FAILURE, null, null);
                    hostWriter.writeObject(returnPacket);
                }
                break;
            case SET_RIGHT_NEIGHBOUR_DENIED:
                PairingHandler.getInstance().setRight(dataPacket.SENDER, Message.DENIED);
                break;
            default:
                break;
        }
        if(returnPacket != null)
            Logger.info("RemoteClient - Sent message to " + returnPacket.RECEIVER.ipAddress + ":" + returnPacket.MESSAGE.name());
        else
            Logger.info("RemoteClient - Did not send a response to " + dataPacket.SENDER.ipAddress);
    }

    public void sendMessage(DataPacket dataPacket) throws IOException {
        hostWriter.writeObject(dataPacket);
    }

    private void close() {
        try {
            InformationHolder.remoteClients.remove(host.getInetAddress().getHostAddress());
            Logger.info("Closing socket for host: " + hostDevice);
            if (host != null)
                host.close();
            if (hostReader != null)
                hostReader.close();
            if (hostWriter != null)
                hostWriter.close();
        } catch (Exception e) {
            Logger.error("Error closing connection to remote host with IP: " + host.getRemoteSocketAddress());
        }
    }

    @Override
    public void run() {
        try {
            connectToHost();
            initiateStreams();
            sendLocalDeviceDataToHost();
            setHostDevice();
            readHostMessages();
        } catch (Exception e) {
            Logger.error("Error during connection to remote host with IP: " + ip + "\n\n" + e.getMessage());
            e.printStackTrace();
        } finally {
            close();
        }
    }
}
