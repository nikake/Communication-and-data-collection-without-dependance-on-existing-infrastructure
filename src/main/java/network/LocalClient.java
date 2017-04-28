package main.java.network;

import main.java.Application;
import main.java.log.Logger;
import main.java.util.Device;

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

        }
    }

    private void close() throws IOException {
        if (client != null)
            client.close();
        if (clientWriter != null)
            clientWriter.close();
        if (clientReader != null)
            clientReader.close();
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
            try {
                close();
            } catch (Exception e){
                Logger.error("Could not close client socket to client "
                        + client.getLocalSocketAddress()
                        + ".\n\n" + e.getMessage());
            }
        }

    }
}
