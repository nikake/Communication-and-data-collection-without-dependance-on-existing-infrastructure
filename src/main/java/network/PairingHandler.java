package main.java.network;

import main.java.util.Device;
import main.java.util.InformationHolder;

import java.util.concurrent.Semaphore;

public class PairingHandler implements Runnable {

    private static PairingHandler instance = new PairingHandler();
    private BluetoothScanner btScanner = new BluetoothScanner();
    private Device left;
    private Device right;
    private Semaphore permits;

    public static PairingHandler getInstance(){
        return instance;
    }

    private void pairDevice(){

    }

    @Override
    public void run() {
        while(InformationHolder.getDevices() == null){
            try {
                Thread.sleep(1000);
            } catch (Exception e){

            }
        }

    }
}
