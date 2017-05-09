package main.java.network;

import main.java.Application;
import main.java.log.Logger;
import main.java.messaging.DataPacket;
import main.java.messaging.Message;
import main.java.util.Device;
import main.java.util.InformationHolder;

import java.util.ArrayList;
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
        return leftRef.get();
    }

    public BluetoothScanner getRight() {
        return rightRef.get();
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            Logger.error("Error while sleeping.");
        }
    }

    private boolean startLeft(Device device) {
        Logger.info("Attempting to set device " + device.ipAddress + " as new left neighbour.");
        BluetoothScanner bs = new BluetoothScanner(device);
        Thread btScanner = new Thread(bs);
        btScanner.start();
        sleep(1000);
        synchronized (pairingLock){
            if((rightRef.get() == null || !rightRef.get().device.equals(device)) && leftRef.compareAndSet(nullBS, bs)) {
                Logger.info("New left neighbour: [" + leftRef.get().device + "]");
                return true;
            }
        }
        Logger.error("Failed to set new left neighbour!\n\nLeft: " + leftRef.get() + "\n\nBs: " + bs);
        btScanner.interrupt();
        return false;
    }

    public boolean setLeft(Device device) {
        //om den kan sätta, sätt device till left
        if (leftRef.get() == null && (rightRef.get() == null || !rightRef.get().device.equals(device))) {
            return startLeft(device);
        }
        return false;
    }

    public boolean setLeft(Device device, Message message) {
        //om den kan sätta, sätt device till left
        if(message == Message.OK) {
            if (leftRef.get() == null && (rightRef.get() == null || !rightRef.get().device.equals(device))) {
                boolean returnValue = startLeft(device);
                pendingLeft = false;
                return returnValue;
            }
        } else if (message == Message.SET_LEFT_NEIGHBOUR_FAILURE) {
            leftRef.set(nullBS);
        }
        pendingLeft = false;
        return false;
    }

    private boolean startRight(Device device) {
        Logger.info("Attempting to set device " + device.ipAddress + " as new right neighbour.");
        BluetoothScanner bs = new BluetoothScanner(device);
        Thread btScanner = new Thread(bs);
        btScanner.start();
        sleep(1000);
        synchronized (pairingLock){
            if((leftRef.get() == null || !leftRef.get().device.equals(device)) && rightRef.compareAndSet(nullBS, bs)) {
                Logger.info("New right neighbour: [" + rightRef.get().device + "]");
                return true;
            }
        }
        Logger.error("Failed to set new right neighbour!\n\nRight: " + right + "\n\nbs: " + bs);
        btScanner.interrupt();
        return false;
    }

    public boolean setRight(Device device) {
        //om den kan sätta, sätt device till right
        if (rightRef.get() == null && (leftRef.get() == null || !leftRef.get().device.equals(device))) {
            return startRight(device);
        }
        return false;
    }

    public boolean setRight(Device device, Message message) {
        //om den kan sätta, sätt device till right
        if (message == Message.OK) {
            if (rightRef.get() == null && (leftRef.get() == null || !leftRef.get().device.equals(device))) {
                boolean returnValue = startRight(device);
                pendingRight = false;
                return returnValue;
            }
        } else if (message == Message.SET_RIGHT_NEIGHBOUR_FAILURE) {
            rightRef.set(nullBS);
        }
        pendingRight = false;
        return false;
    }

    private void pairDevice(){

    }

    private void updateRoutingTable(){
        Device[] value = new Device[2];
        if(leftRef.get() == null)
            value[0] = null;
        else
            value[0] = leftRef.get().device;
        if(rightRef.get() == null)
            value[1] = null;
        else
            value[1] = rightRef.get().device;

        RoutingTable.add(Application.getLocalDevice(), value);
    }

    private void searchForNeighbours() {
        HashMap<BluetoothScanner, Thread> rssiValues = new HashMap<>();
        ArrayList<Device> checkedDevices = new ArrayList<>();
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

        while(leftRef.get() == null && rightRef.get() == null) {
            int closestRssi = -100;
            BluetoothScanner closest = null;
            boolean foundNoDevices = true;
            // Pair with closest device.
            for(Map.Entry<BluetoothScanner, Thread> me : rssiValues.entrySet()) {
                // Check if left or right is available in the other device.
                if(!checkedDevices.contains(me.getKey().device) && me.getKey().getRssi() >= closestRssi) {
                    closest = me.getKey();
                    closestRssi = me.getKey().getRssi();
                    foundNoDevices = false;
                }
            }
            if(closest != null) {
                RemoteClient remoteClient = InformationHolder.remoteClients.get(closest.device.ipAddress);
                try {
                    pendingLeft = true;
                    pendingRight = true;
                    remoteClient.sendMessage(new DataPacket(Application.getLocalDevice(), remoteClient.getHostDevice(), Message.SET_RIGHT_NEIGHBOUR, null, null));
                    remoteClient.sendMessage(new DataPacket(Application.getLocalDevice(), remoteClient.getHostDevice(), Message.SET_LEFT_NEIGHBOUR, null, null));
                    checkedDevices.add(closest.device);
                } catch (Exception e){

                }
            }
            int maxTries = 20, attempt = 0;
            while ((pendingLeft || pendingRight) && attempt < maxTries) {
                try {
                    Thread.sleep(100);
                    attempt++;
                } catch (Exception e) {

                }
            }
            if(leftRef.get() == null && rightRef.get() == null) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {

                }
            }
            if(foundNoDevices)
                checkedDevices.clear();
            System.out.println("Closest RSSI: " + closestRssi);
        }
        for(Map.Entry<BluetoothScanner, Thread> me : rssiValues.entrySet()) {
            // Check if left or right is available in the other device.
            if(me.getValue().isAlive())
                me.getValue().interrupt();
        }
    }

    private int leftStrength, rightStrength;
    private int leftFailures = 0, rightFailures = 0;

    private void scanDistanceToNeighbours() {
        if (leftRef.get() != null) {
            leftStrength = leftRef.get().getRssi();
            if(leftStrength <= -100)
                leftFailures++;
            else
                leftFailures = 0;
            System.out.println("Left [IP: " + leftRef.get().device.ipAddress + "] rssi: " + leftStrength);
            if(leftFailures == 10) {
                leftRef.set(null);
                leftFailures = 0;
            }
        }
        if (rightRef.get() != null) {
            rightStrength = rightRef.get().getRssi();
            if(rightStrength <= -100)
                rightFailures++;
            else
                rightFailures = 0;
            System.out.println("Right [IP: " + rightRef.get().device.ipAddress + "] rssi: " + rightStrength);
            if(rightFailures == 10) {
                rightRef.set(null);
                rightFailures = 0;
            }
        }
        try {
            Thread.sleep(300);
        } catch (Exception e) {

        }
    }

    @Override
    public void run() {
        while(true) {
            if (leftRef.get() == null && rightRef.get() == null) {
                searchForNeighbours();
            } else {
                scanDistanceToNeighbours();
            }
        }
    }
}
