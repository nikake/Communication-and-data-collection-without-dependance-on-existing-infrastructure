package main.java.network;

import main.java.util.CommandExecutor;
import main.java.util.Device;

public class BluetoothScanner {

    private CommandExecutor commandExecutor = new CommandExecutor();
    private static final String CMD = "python bluetooth_rssi.py";

    public double scanDevice(Device device){
        // Get 10 rssi values
        double[] rssiValues = new double[10];
        for(double d : rssiValues){
            d = Double.parseDouble(commandExecutor.execute(CMD + " " + device.btAddress.split("\\s+"))[0]);
        }
        // Get the average of the 10 rssi values
        double sum = 0;
        for(double d : rssiValues){
            sum += d;
        }

        return sum / rssiValues.length;
    }

}
