package main.java.network;

import main.java.log.Logger;
import main.java.util.Device;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;

public class LocalClient implements Runnable {

    private Socket client;

    public LocalClient(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try{
            //Set clientDevice
            Device clientDevice = null;
            ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
            do {
                try {
                    clientDevice = (Device) ois.readObject();
                } catch (Exception e) {
                    Logger.info("Attempted to set clientDevice, but failed.\n\n" + e.getMessage());
                }
            } while (clientDevice == null);
            Logger.info("Communication with new device established. New device: " + clientDevice);
            // Add clientDevice to collection

            //Read messages from client
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String inputLine = clientReader.readLine();
            while(inputLine != null) {
                final String message = inputLine;
                // Do something with message
                inputLine = clientReader.readLine();
            }
        } catch (Exception e){
            Logger.error("Error while running client.\n\n" + e.getMessage());
        } finally {
            try {
                if (client != null)
                    client.close();
            } catch (Exception e){
                Logger.error("Could not close client socket to client "
                        + client.getLocalSocketAddress()
                        + ".\n\n" + e.getMessage());
            }
        }

    }
}
