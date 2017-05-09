package main.java.network;

import main.java.Application;
import main.java.log.Logger;
import main.java.messaging.DataPacket;
import main.java.messaging.Message;
import main.java.util.Device;
import main.java.util.InformationHolder;

import java.io.IOException;
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

    private Device pendingLeft;
    private Device pendingRight;

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
            Logger.error("PairingHandler - Error while sleeping.");
        }
    }

    private boolean startLeft(Device device) {
        Logger.info("PairingHandler - Attempting to set device " + device.ipAddress + " as new left neighbour.");
        BluetoothScanner bs = new BluetoothScanner(device);
        Thread btScanner = new Thread(bs);
        btScanner.start();
        sleep(1000);
        synchronized (pairingLock){
            if(leftRef.get() == null && (rightRef.get() == null || !rightRef.get().device.equals(device)) && leftRef.compareAndSet(nullBS, bs)) {
                Logger.info("PairingHandler - New left neighbour: [" + leftRef.get().device + "]");
                return true;
            }
        }
        Logger.error("PairingHandler - Failed to set new left neighbour!\n\nLeft: " + leftRef.get() + "\n\nBs: " + bs);
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
        if(message == Message.SET_LEFT_NEIGHBOUR_OK) {
            if (leftRef.get() == null && (rightRef.get() == null || !rightRef.get().device.equals(device))) {
                boolean returnValue = startLeft(device);
                pendingLeft = null;
                return returnValue;
            }
        } else if (message == Message.SET_LEFT_NEIGHBOUR_FAILURE) {
            leftRef.set(null);
        }
        pendingLeft = null;
        return false;
    }

    private boolean startRight(Device device) {
        Logger.info("PairingHandler - Attempting to set device " + device.ipAddress + " as new right neighbour.");
        BluetoothScanner bs = new BluetoothScanner(device);
        Thread btScanner = new Thread(bs);
        btScanner.start();
        sleep(1000);
        synchronized (pairingLock){
            if(rightRef.get() == null && (leftRef.get() == null || !leftRef.get().device.equals(device)) && rightRef.compareAndSet(nullBS, bs)) {
                Logger.info("PairingHandler - New right neighbour: [" + rightRef.get().device + "]");
                return true;
            }
        }
        Logger.error("PairingHandler - Failed to set new right neighbour!\n\nRight: " + right + "\n\nbs: " + bs);
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
        if (message == Message.SET_RIGHT_NEIGHBOUR_OK) {
            if (rightRef.get() == null && (leftRef.get() == null || !leftRef.get().device.equals(device))) {
                boolean returnValue = startRight(device);
                pendingRight = null;
                return returnValue;
            }
        } else if (message == Message.SET_RIGHT_NEIGHBOUR_FAILURE) {
            rightRef.set(null);
        }
        pendingRight = null;
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

    private CopyOnWriteArrayList<Device> getFoundDevices() {
        CopyOnWriteArrayList<Device> devices;
        while((devices = InformationHolder.getDevices()).isEmpty()) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {

            }
        }
        return devices;
    }

    /*

     */

    private ArrayList<Device> checkedDevices;

    private HashMap<BluetoothScanner, Thread> startScannersForFoundDevices(CopyOnWriteArrayList<Device> devices) {
        HashMap<BluetoothScanner, Thread> scanners = new HashMap<>();
        for(Device d : devices){
            BluetoothScanner bs = new BluetoothScanner(d);
            Thread btScanner = new Thread(bs);
            btScanner.start();
            scanners.put(bs, btScanner);
        }
        return scanners;
    }

    private BluetoothScanner getClosestDeviceBluetoothScanner(HashMap<BluetoothScanner, Thread> scanners) {
        int closestRssi = -100;
        BluetoothScanner closestScanner = null;
        for(Map.Entry<BluetoothScanner, Thread> me : scanners.entrySet()) {
            // Check if left or right is available in the other device.
            if(!checkedDevices.contains(me.getKey().device) && me.getKey().getRssi() >= closestRssi) {
                closestScanner = me.getKey();
                closestRssi = me.getKey().getRssi();
                checkedDevices.add(closestScanner.device);
            }
        }
        return closestScanner;
    }

    private void sendPairingRequestToClosestNeighbour(BluetoothScanner closestScanner) throws IOException {
        RemoteClient remoteClient = InformationHolder.remoteClients.get(closestScanner.device.ipAddress);
        pendingLeft = remoteClient.getHostDevice();
        pendingRight = remoteClient.getHostDevice();
        remoteClient.sendMessage(new DataPacket(Application.getLocalDevice(), remoteClient.getHostDevice(), Message.SET_RIGHT_NEIGHBOUR, null, null));
        remoteClient.sendMessage(new DataPacket(Application.getLocalDevice(), remoteClient.getHostDevice(), Message.SET_LEFT_NEIGHBOUR, null, null));
    }

    private void waitForResponsesOnPairingRequest() {
        int maxTries = 20, attempt = 0;
        while ((pendingLeft != null || pendingRight != null) && attempt < maxTries) {
            sleep(100);
            attempt++;
        }
    }

    // Find closest device
    /*
        1. Find closestScanner
        2. If closestScanner != null then continue, else return.
        3. Attempt to connect to closestScanner.device
        4.
    */

    private boolean connectedToNeighbour(HashMap<BluetoothScanner, Thread> scanners) throws IOException {
        BluetoothScanner closestScanner = getClosestDeviceBluetoothScanner(scanners);
        if (closestScanner != null) {
            sendPairingRequestToClosestNeighbour(closestScanner);
            waitForResponsesOnPairingRequest();
        } else {
            checkedDevices.clear();
        }
        return leftRef.get() != null || rightRef.get() != null;
    }



    private void closeUnusedScanners(HashMap<BluetoothScanner, Thread> scanners) {
        for(Map.Entry<BluetoothScanner, Thread> me : scanners.entrySet()) {
            // Check if left or right is available in the other device.
            if(me.getValue().isAlive())
                me.getValue().interrupt();
        }
    }

        // Get devices, wait for at least one device

        // Start bluetooth scanner to each found device. Sleep 1000ms to wait for results.

        /* while left == null && right == null
                attempt to connect to closest device on right or left
                wait for responses
                wait 1000ms
        */

        // interrupt every scanner other than left/right

    private void searchForNeighbours() {
        checkedDevices = new ArrayList<>();
        CopyOnWriteArrayList<Device> devices = getFoundDevices();
        HashMap<BluetoothScanner, Thread> scanners = startScannersForFoundDevices(devices);
        sleep(1000);
        try {
            while (!connectedToNeighbour(scanners)) ;
        } catch (Exception e) {
            Logger.error("PairingHandler - Error while trying to connect to neighbour.\n\n" + e.getMessage());
        }
        closeUnusedScanners(scanners);
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
                DataPacket dataPacket = new DataPacket(Application.getLocalDevice(), leftRef.get().device, Message.SET_LEFT_NEIGHBOUR_FAILURE, null, null);
                try {
                    InformationHolder.remoteClients.get(leftRef.get().device.ipAddress).sendMessage(dataPacket);
                } catch (Exception e) {

                }
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
                DataPacket dataPacket = new DataPacket(Application.getLocalDevice(), rightRef.get().device, Message.SET_RIGHT_NEIGHBOUR_FAILURE, null, null);
                try {
                    InformationHolder.remoteClients.get(rightRef.get().device.ipAddress).sendMessage(dataPacket);
                } catch (Exception e) {

                }
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
