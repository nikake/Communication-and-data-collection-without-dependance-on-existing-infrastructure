package main.java.network;

import main.java.Application;
import main.java.log.Logger;
import main.java.messaging.DataPacket;
import main.java.messaging.Message;
import main.java.util.Device;
import main.java.util.InformationHolder;

import java.io.*;
import java.net.Socket;

public class LocalClient implements Runnable {

    private Socket client;
    private Device clientDevice = null;
    private ObjectOutputStream clientWriter = null;
    private ObjectInputStream clientReader = null;

    public LocalClient(Socket client) {
        this.client = client;
        InformationHolder.localClients.put(client.getInetAddress().getHostAddress(), this);
    }

    private void initiateStreams() throws IOException {
        clientWriter = new ObjectOutputStream(client.getOutputStream());
        clientReader = new ObjectInputStream(client.getInputStream());
    }

    private void setClientDevice() {
        do {
            try {
                clientDevice = (Device) clientReader.readObject();
            } catch (Exception e) {
                Logger.info("Attempted to set clientDevice, but failed.\n\n" + e.getMessage());
            }
        } while (clientDevice == null);
        Logger.info("Client opened: " + clientDevice);
        System.out.println("Client opened: " + clientDevice);
    }

    private void sendLocalDeviceDataToClient() throws IOException {
        clientWriter.writeObject(Application.getLocalDevice());
    }

    private void readClientMessages() throws IOException, ClassNotFoundException {
        Object message = null;
        while((message = clientReader.readObject()) != null) {
            if (message instanceof DataPacket)
                parsePacket((DataPacket) message);
        }
    }

    private void parsePacket(DataPacket dataPacket) throws IOException {
        if (dataPacket.RECEIVER.equals(Application.getLocalDevice())) {
            readMessage(dataPacket);
        } else {
            // Skicka vidare enligt dataPacket.ROUTING_TABLE
        }
    }

    private void readMessage(DataPacket dataPacket) throws IOException {
        DataPacket returnPacket;
        switch (dataPacket.MESSAGE) {
            case TOO_CLOSE:
                Logger.info("Too close to neighbour " + dataPacket.SENDER.ipAddress + ".");
                break;
            case TOO_FAR_AWAY:
                Logger.info("Too far away from neighbour " + dataPacket.SENDER.ipAddress + ".");
                break;
            case SET_LEFT_NEIGHBOUR:
                Logger.info(dataPacket.SENDER.ipAddress + " asking to set this device as its left neighbour.");
                if(PairingHandler.getInstance().setLeft(dataPacket.SENDER))
                    returnPacket = new DataPacket(Application.getLocalDevice(), clientDevice, Message.SET_LEFT_NEIGHBOUR_OK, null, null);
                else
                    returnPacket = new DataPacket(Application.getLocalDevice(), clientDevice, Message.SET_LEFT_NEIGHBOUR_DENIED, null, null);
                Logger.info("Trying to return message to: " + dataPacket.SENDER.ipAddress);
                clientWriter.writeObject(returnPacket);
                break;
            case SET_RIGHT_NEIGHBOUR:
                Logger.info(dataPacket.SENDER.ipAddress + " asking to set this device as its right neighbour.");
                if(PairingHandler.getInstance().setRight(dataPacket.SENDER))
                    returnPacket = new DataPacket(Application.getLocalDevice(), clientDevice, Message.SET_RIGHT_NEIGHBOUR_OK, null, null);
                else
                    returnPacket = new DataPacket(Application.getLocalDevice(), clientDevice, Message.SET_RIGHT_NEIGHBOUR_DENIED, null, null);
                clientWriter.writeObject(returnPacket);
                break;
            case GET_INFO:
                returnPacket = new DataPacket(Application.getLocalDevice(), clientDevice, Message.GET_INFO_OK, null, null);
                clientWriter.writeObject(returnPacket);
                break;
            case UPDATE_ROUTING_TABLE:
                break;
            default:
                break;
        }
    }

    public void sendMessage(DataPacket dataPacket) throws IOException {
        clientWriter.writeObject(dataPacket);
    }

    private void close() {
        try {
            InformationHolder.localClients.remove(client.getInetAddress().getHostAddress());
            if (client != null)
                client.close();
            if (clientWriter != null)
                clientWriter.close();
            if (clientReader != null)
                clientReader.close();
        } catch (Exception e){
            Logger.error("Could not close client socket to client "
                    + client.getLocalSocketAddress()
                    + ".\n\n" + e.getMessage());
        }
    }

    @Override
    public void run() {
        try{
            initiateStreams();
            sendLocalDeviceDataToClient();
            setClientDevice();
            readClientMessages();
        } catch (Exception e){
            Logger.error("Error while running client.\n\n" + e.getMessage());
        } finally {
            close();
        }

    }
}
