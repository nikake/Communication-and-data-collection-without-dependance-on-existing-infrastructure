package main.java.network;

import main.java.Application;
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
                // Check if left or right is available in the other device
                if(!me.getKey().hasLeft() || !me.getKey().hasRight()) {
                    if (me.getValue().getRssi() >= closestRssi) {
                        closest = me.getValue();
                        closestRssi = me.getValue().getRssi();
                    }
                }
            }
            if(!closest.device.hasLeft()) {
                right = closest;
                Application.getLocalDevice().setRight(true);
            } else {
                left = closest;
                Application.getLocalDevice().setLeft(true);
            }

            System.out.println("Closest RSSI: " + closestRssi);
            if(right != null)
                System.out.println("Right: " + right.device);
            else if (left != null)
                System.out.println("Left: " + left.device);
        }
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
