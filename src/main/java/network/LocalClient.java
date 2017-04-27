package main.java.network;

import main.java.Application;
import main.java.log.Logger;
import main.java.util.Device;

import java.io.*;
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
            PrintWriter clientWriter = new PrintWriter(client.getOutputStream());

            //Read messages from client
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String inputLine = clientReader.readLine();
            while(inputLine != null) {
                final String message = inputLine;
                // Do something with message
                switch(message) {
                    case "left_neighbour":
                        boolean hasLeft = PairingHandler.getInstance().getLeft() == null;
                        clientWriter.print(hasLeft);
                        break;
                    case "right_neighbour":
                        boolean hasRight = PairingHandler.getInstance().getLeft() == null;
                        clientWriter.print(hasRight);
                        break;
                    default:
                        break;
                }
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
