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

public class PairingHandler implements Runnable {

    private static PairingHandler instance = new PairingHandler();
    private Device pendingLeft;
    private Device pendingRight;
    private BluetoothScanner left = null;
    private BluetoothScanner right = null;
    private final Object pairingLock = new Object();

    public PairingHandler() {
        left = null;
        right = null;
        pendingLeft = null;
        pendingRight = null;

    }

    public static PairingHandler getInstance(){
        return instance;
    }

    public BluetoothScanner getLeft() {
        return left;
    }

    public BluetoothScanner getRight() {
        return right;
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            Logger.error("PairingHandler - Error while sleeping.");
        }
    }

    private BluetoothScanner startNewNeighbour(Device device) {
        BluetoothScanner bs = new BluetoothScanner(device);
        Thread btScanner = new Thread(bs);
        btScanner.start();
        return bs;
    }

    private boolean setLeft(Device device) {
        if (pendingLeft.equals(device)) {
            if(left == null && (right == null || !right.device.equals(device))) {
                left = startNewNeighbour(device);
                pendingLeft = null;
                return true;
            }
        }
        return false;
    }

    private boolean setPendingLeft(Device device) {
        if(pendingLeft == null && left == null) {
            if(right == null || !right.device.equals(device)) {
                pendingLeft = device;
                return true;
            }
        }
        return false;
    }

    private boolean setRight(Device device) {
        if (pendingRight.equals(device)) {
            if(right == null && (left == null || !left.device.equals(device))) {
                right = startNewNeighbour(device);
                pendingRight = null;
                return true;
            }
        }
        return false;
    }

    private boolean setPendingRight(Device device) {
        if(pendingRight == null && right == null) {
            if(left == null || !left.device.equals(device)) {
                pendingRight = device;
                return true;
            }
        }
        return false;
    }

    private void printTestMessage() {
        System.out.println("PairingHandler Neighbours");
        if(pendingLeft != null)
            System.out.println("\t\tPendingLeft: " + pendingLeft.ipAddress);
        else
            System.out.println("\t\tPendingLeft: null");
        if(left != null)
            System.out.println("\t\tLeft: " + left.device.ipAddress);
        else
            System.out.println("\t\tLeft: null");
        if(pendingRight != null)
            System.out.println("\t\tPendingRight: " + pendingRight.ipAddress);
        else
            System.out.println("\t\tPendingRight: null");
        if(right != null)
            System.out.println("\t\tRight: " + right.device.ipAddress);
        else
            System.out.println("\t\tRight: null");
    }

    public boolean setNeighbour(Device device, Message message) {
        synchronized (pairingLock) {
            Logger.info("PairingHandler - Processing message from device " + device.ipAddress + ": " + message.name());
            printTestMessage();
            switch (message) {
                case SET_RIGHT_NEIGHBOUR:
                    return setPendingLeft(device);
                case SET_RIGHT_NEIGHBOUR_OK:
                    return setRight(device);
                case SET_RIGHT_NEIGHBOUR_DENIED:
                case SET_RIGHT_NEIGHBOUR_FAILURE:
                    if(device.equals(pendingLeft))
                        pendingLeft = null;
                    break;
                case SET_RIGHT_NEIGHBOUR_SUCCESS:
                    return setLeft(device);
                case SET_LEFT_NEIGHBOUR:
                    return setPendingRight(device);
                case SET_LEFT_NEIGHBOUR_OK:
                    return setLeft(device);
                case SET_LEFT_NEIGHBOUR_DENIED:
                case SET_LEFT_NEIGHBOUR_FAILURE:
                    if(device.equals(pendingRight))
                        pendingRight = null;
                    break;
                case SET_LEFT_NEIGHBOUR_SUCCESS:
                    return setRight(device);
                default:
                    Logger.error("PairingHandler - No case for message in setNeighbour: " + message.name());
                    break;
            }
        }
        return false;
    }

    private void updateRoutingTable(){
        Device[] value = new Device[2];
        if(left == null)
            value[0] = null;
        else
            value[0] = left.device;
        if(right == null)
            value[1] = null;
        else
            value[1] = right.device;

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

    /*
        Suspekt att detta failar pga trådning
     */
    private void waitForResponsesOnPairingRequest() {
        int maxTries = 20, attempt = 0;
        while ((pendingLeft != null || pendingRight != null) && attempt < maxTries) {
            sleep(100);
            attempt++;
        }
        pendingLeft = null;
        pendingRight = null;
    }

    private boolean connectedToNeighbour(HashMap<BluetoothScanner, Thread> scanners) throws IOException {
        BluetoothScanner closestScanner = getClosestDeviceBluetoothScanner(scanners);
        if (closestScanner != null) {
            sendPairingRequestToClosestNeighbour(closestScanner);
            waitForResponsesOnPairingRequest();
        } else {
            checkedDevices.clear();
        }
        return left != null || right != null;
    }



    private void closeUnusedScanners(HashMap<BluetoothScanner, Thread> scanners) {
        for(Map.Entry<BluetoothScanner, Thread> me : scanners.entrySet()) {
            me.getKey().close();
            if(me.getValue().isAlive())
                me.getValue().interrupt();
        }
    }

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
    private boolean leftInitiated = false, rightInitiated = false;

    private void scanDistanceToNeighbours() {
        if (left != null) {
            leftStrength = left.getRssi();
            if(leftStrength <= -100 && leftInitiated) {
                leftFailures++;
            } else {
                leftInitiated = true;
                leftFailures = 0;
            }
            System.out.println("Left [IP: " + left.device.ipAddress + "] rssi: " + leftStrength);
            if(leftFailures == 10) {
                DataPacket dataPacket = new DataPacket(Application.getLocalDevice(), left.device, Message.SET_LEFT_NEIGHBOUR_FAILURE, null, null);
                try {
                    InformationHolder.remoteClients.get(left.device.ipAddress).sendMessage(dataPacket);
                } catch (Exception e) {

                }
                left = null;
                leftFailures = 0;
                leftInitiated = false;
            }
        }
        if (right != null) {
            rightStrength = right.getRssi();
            if(rightStrength <= -100 && rightInitiated) {
                rightFailures++;
            } else {
                rightInitiated = true;
                rightFailures = 0;
            }
            System.out.println("Right [IP: " + right.device.ipAddress + "] rssi: " + rightStrength);
            if(rightFailures == 10) {
                DataPacket dataPacket = new DataPacket(Application.getLocalDevice(), right.device, Message.SET_RIGHT_NEIGHBOUR_FAILURE, null, null);
                try {
                    InformationHolder.remoteClients.get(right.device.ipAddress).sendMessage(dataPacket);
                } catch (Exception e) {

                }
                right = null;
                rightFailures = 0;
                rightInitiated = false;
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
            if (left == null && right == null) {
                searchForNeighbours();
            } else {
                scanDistanceToNeighbours();
            }
        }
    }
}
