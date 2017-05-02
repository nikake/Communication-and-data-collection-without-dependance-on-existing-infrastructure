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

    private void searchForNeighbours() {
        while(left == null && right == null) {
            CopyOnWriteArrayList<Device> devices = new CopyOnWriteArrayList<>();
            devices.addAll(InformationHolder.getDevices());

            HashMap<BluetoothScanner, Thread> rssiValues = new HashMap<>();
            for(Device d : devices){
                BluetoothScanner bs = new BluetoothScanner(d);
                Thread btScanner = new Thread(bs);
                btScanner.start();
                rssiValues.put(bs, btScanner);
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {

            }
            BluetoothScanner closest = null;
            int closestRssi = -100;
            // Pair with closest device.
            for(Map.Entry<BluetoothScanner, Thread> me : rssiValues.entrySet()) {
                // Check if left or right is available in the other device.
                if(me.getKey().getRssi() >= closestRssi) {
                    closest = me.getKey();
                    closestRssi = me.getKey().getRssi();
                }
                if(me.getValue().isAlive())
                    me.getValue().interrupt();
            }
            if(closest != null) {
                RemoteClient remoteClient = InformationHolder.remoteClients.get(closest.device.ipAddress);
                try {
                    remoteClient.sendMessage(new DataPacket(Application.getLocalDevice(), remoteClient.getHostDevice(), Message.SET_LEFT_NEIGHBOUR, null, null));
                    pendingLeft = closest;
                    remoteClient.sendMessage(new DataPacket(Application.getLocalDevice(), remoteClient.getHostDevice(), Message.SET_RIGHT_NEIGHBOUR, null, null));
                    pendingRight = closest;

                } catch (Exception e){

                }
            }
            System.out.println("Closest RSSI: " + closestRssi);
        }
    }

    private void scanDistanceToNeighbours() {
        int leftStrength, rightStrength;
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

    @Override
    public void run() {
        while(true) {
            if (left == null && right == null) {
                searchForNeighbours();
            } else {
                scanDistanceToNeighbours();
            }
        }
    }
}
