package main.java.network;

import main.java.util.Device;
import main.java.util.InformationHolder;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

public class PairingHandler implements Runnable {

    private static PairingHandler instance = new PairingHandler();
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
        while(InformationHolder.getDevices().isEmpty()){
            try{
                Thread.sleep(1000);
            } catch (Exception e){

            }
        }
        CopyOnWriteArrayList<Device> devices = InformationHolder.getDevices();
        for(Device d : devices){
            BluetoothScanner bs = new BluetoothScanner(d);
            Thread btScanner = new Thread(bs);
            btScanner.start();
        }

    }
}
