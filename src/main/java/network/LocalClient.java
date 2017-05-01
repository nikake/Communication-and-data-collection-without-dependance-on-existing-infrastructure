package main.java.network;

import main.java.Application;
import main.java.log.Logger;
import main.java.messaging.DataPacket;
import main.java.messaging.MessageReader;

import java.io.*;
import java.net.Socket;

public class LocalClient implements Runnable {

    private Socket client;
    private ObjectOutputStream clientWriter = null;
    private ObjectInputStream clientReader = null;

    public LocalClient(Socket client) {
        this.client = client;
    }

    private void initiateStreams() throws IOException {
        clientWriter = new ObjectOutputStream(client.getOutputStream());
        clientReader = new ObjectInputStream(client.getInputStream());
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

    private void parsePacket(DataPacket dataPacket) {
        if (dataPacket.RECEIVER.equals(Application.getLocalDevice())) {
            MessageReader messageReader = new MessageReader();
            messageReader.readMessage(dataPacket.MESSAGE);
        } else {
            // Skicka vidare enligt dataPacket.ROUTING_TABLE
        }
    }

    private void close() {
        try {
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
            readClientMessages();
        } catch (Exception e){
            Logger.error("Error while running client.\n\n" + e.getMessage());
        } finally {
            close();
        }

    }
}
