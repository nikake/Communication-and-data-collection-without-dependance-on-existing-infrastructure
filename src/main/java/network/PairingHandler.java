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
    private BluetoothScanner left;
    private BluetoothScanner right;
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

    private BluetoothScanner pendingLeft;
    private BluetoothScanner pendingRight;

    public BluetoothScanner getLeft() {
        return left;
    }

    public BluetoothScanner getRight() {
        return right;
    }

    private boolean startLeft(Device device) {
        BluetoothScanner bs = new BluetoothScanner(device);
        leftRef.compareAndSet(null, bs);
        if(left.equals(bs)) {
            Thread btScanner = new Thread(bs);
            btScanner.start();
            return true;
        }
        return false;
    }

    public boolean setLeft(Device device) {
        //om den kan sätta, sätt device till left
        if (left == null && (right == null || !right.device.equals(device))) {
            startLeft(device);
        }
        return false;
    }

    public boolean setLeft(Device device, Message message) {
        //om den kan sätta, sätt device till left
        if(message == Message.OK) {
            if (left == null && (right == null || !right.device.equals(device))) {
                startLeft(device);
            }
        }
        return false;
    }

    private boolean startRight(Device device) {
        BluetoothScanner bs = new BluetoothScanner(device);
        rightRef.compareAndSet(null, bs);
        if(right.equals(bs)) {
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
                return startRight(device);
            }
        }
        return false;
    }

    private void pairDevice(){

    }

    private HashMap<BluetoothScanner, Thread> rssiValues = new HashMap<>();


    private void searchForNeighbours() {
        while(left == null && right == null) {
            CopyOnWriteArrayList<Device> devices = new CopyOnWriteArrayList<>();
            devices.addAll(InformationHolder.getDevices());

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
                    remoteClient.sendMessage(new DataPacket(Application.getLocalDevice(), remoteClient.getHostDevice(), Message.SET_RIGHT_NEIGHBOUR, null, null));
                    pendingLeft = closest;
                    remoteClient.sendMessage(new DataPacket(Application.getLocalDevice(), remoteClient.getHostDevice(), Message.SET_LEFT_NEIGHBOUR, null, null));
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
