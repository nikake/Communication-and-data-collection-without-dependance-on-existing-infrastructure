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
import java.util.concurrent.atomic.AtomicReference;

public class PairingHandler implements Runnable {

    private static PairingHandler instance = new PairingHandler();
    private BluetoothScanner left = null;
    private BluetoothScanner right = null;
    private BluetoothScanner nullBS = null;
    private AtomicReference<BluetoothScanner> leftRef;
    private AtomicReference<BluetoothScanner> rightRef;
    private Semaphore permits;
    private final Object pairingLock = new Object();

    public PairingHandler() {
        left = null;
        right = null;
        leftRef = new AtomicReference<>(left);
        rightRef = new AtomicReference<>(right);
    }

    public static PairingHandler getInstance(){
        return instance;
    }

    private boolean pendingLeft;
    private boolean pendingRight;

    public BluetoothScanner getLeft() {
        return left;
    }

    public BluetoothScanner getRight() {
        return right;
    }

    private boolean startLeft(Device device) {
        BluetoothScanner bs = new BluetoothScanner(device);
        if(leftRef.compareAndSet(nullBS, bs)) {
            Thread btScanner = new Thread(bs);
            btScanner.start();
            return true;
        }
        return false;
    }

    public boolean setLeft(Device device) {
        //om den kan sätta, sätt device till left
        if (left == null && (right == null || !right.device.equals(device))) {
            return startLeft(device);
        }
        return false;
    }

    public boolean setLeft(Device device, Message message) {
        //om den kan sätta, sätt device till left
        if(message == Message.OK) {
            if (left == null && (right == null || !right.device.equals(device))) {
                boolean returnValue = startLeft(device);
                pendingLeft = false;
                return returnValue;
            }
        }
        pendingLeft = false;
        return false;
    }

    private boolean startRight(Device device) {
        BluetoothScanner bs = new BluetoothScanner(device);
        if(rightRef.compareAndSet(nullBS, bs)) {
            Thread btScanner = new Thread(bs);
            btScanner.start();
            return true;
        }
        return false;
    }

    public boolean setRight(Device device) {
        //om den kan sätta, sätt device till right
        if (right == null && (left == null || !left.device.equals(device))) {
            return startRight(device);
        }
        return false;
    }

    public boolean setRight(Device device, Message message) {
        //om den kan sätta, sätt device till right
        if (message == Message.OK) {
            if (right == null && (left == null || !left.device.equals(device))) {
                boolean returnValue = startRight(device);
                pendingRight = false;
                return returnValue;
            }
        }
        pendingRight = false;
        return false;
    }

    private void pairDevice(){

    }

    private void searchForNeighbours() {
        HashMap<BluetoothScanner, Thread> rssiValues = new HashMap<>();
        CopyOnWriteArrayList<Device> devices;

        while((devices = InformationHolder.getDevices()).isEmpty()) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {

            }
        }
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
        while(left == null && right == null) {
            BluetoothScanner closest = null;
            int closestRssi = -100;
            // Pair with closest device.
            for(Map.Entry<BluetoothScanner, Thread> me : rssiValues.entrySet()) {
                // Check if left or right is available in the other device.
                if(me.getKey().getRssi() >= closestRssi) {
                    closest = me.getKey();
                    closestRssi = me.getKey().getRssi();
                }
            }
            if(closest != null) {
                RemoteClient remoteClient = InformationHolder.remoteClients.get(closest.device.ipAddress);
                try {
                    pendingLeft = true;
                    pendingRight = true;
                    remoteClient.sendMessage(new DataPacket(Application.getLocalDevice(), remoteClient.getHostDevice(), Message.SET_RIGHT_NEIGHBOUR, null, null));
                    remoteClient.sendMessage(new DataPacket(Application.getLocalDevice(), remoteClient.getHostDevice(), Message.SET_LEFT_NEIGHBOUR, null, null));
                } catch (Exception e){

                }
            }
            while (pendingLeft || pendingRight) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {

                }
            }
            System.out.println("Closest RSSI: " + closestRssi);
        }
        for(Map.Entry<BluetoothScanner, Thread> me : rssiValues.entrySet()) {
            // Check if left or right is available in the other device.
            if(me.getValue().isAlive())
                me.getValue().interrupt();
        }
    }

    private void scanDistanceToNeighbours() {
        int leftStrength, rightStrength;
        if (left != null) {
            leftStrength = left.getRssi();
            System.out.println("Left [IP: " + left.device.ipAddress + "] rssi: " + leftStrength);
        }
        if (right != null) {
            rightStrength = right.getRssi();
            System.out.println("Right [IP: " + right.device.ipAddress + "] rssi: " + rightStrength);
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
