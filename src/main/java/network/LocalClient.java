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
        DataPacket returnPacket = null;
        Logger.info("LocalClient - Received message from " + dataPacket.SENDER.ipAddress + ":" + dataPacket.MESSAGE.name());
        switch (dataPacket.MESSAGE) {
            case TOO_CLOSE:
                break;
            case TOO_FAR_AWAY:
                break;
            case SET_LEFT_NEIGHBOUR:
                if(PairingHandler.getInstance().setRight(dataPacket.SENDER)) {
                    returnPacket = new DataPacket(Application.getLocalDevice(), clientDevice, Message.SET_LEFT_NEIGHBOUR_OK, null, null);
                } else {
                    returnPacket = new DataPacket(Application.getLocalDevice(), clientDevice, Message.SET_LEFT_NEIGHBOUR_DENIED, null, null);
                }
                clientWriter.writeObject(returnPacket);
                break;
            case SET_RIGHT_NEIGHBOUR:
                if(PairingHandler.getInstance().setLeft(dataPacket.SENDER)) {
                    returnPacket = new DataPacket(Application.getLocalDevice(), clientDevice, Message.SET_RIGHT_NEIGHBOUR_OK, null, null);
                } else {
                    returnPacket = new DataPacket(Application.getLocalDevice(), clientDevice, Message.SET_RIGHT_NEIGHBOUR_DENIED, null, null);
                }
                clientWriter.writeObject(returnPacket);
                break;
            case SET_LEFT_NEIGHBOUR_FAILURE:
                PairingHandler.getInstance().setLeft(dataPacket.SENDER, Message.SET_LEFT_NEIGHBOUR_FAILURE);
                break;
            case SET_RIGHT_NEIGHBOUR_FAILURE:
                PairingHandler.getInstance().setLeft(dataPacket.SENDER, Message.SET_RIGHT_NEIGHBOUR_FAILURE);
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
        if(returnPacket != null)
            Logger.info("LocalClient - Sent message to " + returnPacket.RECEIVER.ipAddress + ":" + returnPacket.MESSAGE.name());
        else
            Logger.info("LocalClient - Did not send a response to " + dataPacket.SENDER.ipAddress);
    }

    public void sendMessage(DataPacket dataPacket) throws IOException {
        clientWriter.writeObject(dataPacket);
    }

    private void close() {
        try {
            InformationHolder.localClients.remove(client.getInetAddress().getHostAddress());
            Logger.info("Closing socket for client: " + clientDevice);
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
            e.printStackTrace();
        } finally {
            close();
        }

    }
}
