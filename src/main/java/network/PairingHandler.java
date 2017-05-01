package main.java.network;

import main.java.Application;
import main.java.messaging.DataPacket;
import main.java.messaging.Message;
import main.java.util.Device;
import main.java.util.InformationHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

public class PairingHandler implements Runnable {

    private static PairingHandler instance = new PairingHandler();
    private BluetoothScanner left;
    private BluetoothScanner right;
    private Semaphore permits;
    private final Object pairingLock = new Object();

    public PairingHandler() {
        while(left == null && right == null) {
            // Get all known devices
            while(InformationHolder.getDevices().isEmpty()){
                try{
                    Thread.sleep(50);
                } catch (Exception e){

                }
            }
            CopyOnWriteArrayList<Device> devices = InformationHolder.getDevices();
            HashMap<Device, BluetoothScanner> rssiValues = new HashMap<>();
            // Go through all devices and check which device with a free spot is closest
            for(Device d : devices){
                BluetoothScanner bs = new BluetoothScanner(d);
                Thread btScanner = new Thread(bs);
                btScanner.start();
                rssiValues.put(d, bs);
            }
            BluetoothScanner closest = null;
            int closestRssi = -100;
            // Pair with closest device.
            for(Map.Entry<Device, BluetoothScanner> me : rssiValues.entrySet()) {
                // Check if left or right is available in the other device.
                if(me.getValue().getRssi() >= closestRssi) {
                    closest = me.getValue();
                    closestRssi = me.getValue().getRssi();
                }
            }

            RemoteClient remoteClient = InformationHolder.remoteClients.get(closest.device.ipAddress);

            try {
                remoteClient.sendMessage(new DataPacket(Application.getLocalDevice(), remoteClient.getHostDevice(), Message.SET_LEFT_NEIGHBOUR, null, null));
                pendingLeft = closest;
                remoteClient.sendMessage(new DataPacket(Application.getLocalDevice(), remoteClient.getHostDevice(), Message.SET_RIGHT_NEIGHBOUR, null, null));
                pendingRight = closest;

            }catch (Exception e){

            }



            System.out.println("Closest RSSI: " + closestRssi);
        }
    }

    public static PairingHandler getInstance(){
        return instance;
    }

    private BluetoothScanner pendingLeft;
    private BluetoothScanner pendingRight;

    public BluetoothScanner getLeft() {
        return left;
    }

    public BluetoothScanner getRight() {
        return right;
    }

    public boolean setLeft(Device device) {
        //om den kan sätta, sätt device till left
        synchronized (pairingLock) {
            if (left == null && (right == null || !right.device.equals(device))) {

            }
        }
        return false;
    }

    public boolean setLeft(Device device, Message message) {
        //om den kan sätta, sätt device till left
        synchronized (pairingLock) {
            if (left == null && (right == null || !right.device.equals(device))) {
                left = pendingLeft;
            }
        }
        return false;
    }

    public boolean setRight(Device device) {
        //om den kan sätta, sätt device till right
        synchronized (pairingLock) {
            if (right == null && (left == null || !left.device.equals(device))) {

            }
        }
        return false;
    }

    public boolean setRight(Device device, Message message) {
        //om den kan sätta, sätt device till right
        synchronized (pairingLock) {
            if (right == null && (left == null || !left.device.equals(device))) {
                right = pendingRight;
            }
        }
        return false;
    }

    private void pairDevice(){

    }

    @Override
    public void run() {
        int leftStrength, rightStrength;
        while(true) {
            if (left != null) {
                leftStrength = left.getRssi();
                System.out.println("Left rssi: " + leftStrength);
            }
            if (right != null) {
                rightStrength = right.getRssi();
                System.out.println("Right rssi: " + rightStrength);
            }
            try {
                Thread.sleep(100);
            } catch (Exception e) {

            }
        }
    }
}
