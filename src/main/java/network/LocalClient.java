package main.java.network;

import main.java.Application;
import main.java.log.Logger;
import main.java.util.Device;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LocalClient implements Runnable {

    private Socket client;

    public LocalClient(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try{
            //Send localDevice to client
            ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
            oos.writeObject(Application.getLocalDevice());

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
