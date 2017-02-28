package main.java;

import java.net.InetAddress;
import java.net.NetworkInterface;

public class MachineID {

    private static MachineID instance = null;
    private byte[] mID;

    private MachineID() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(ip);
            mID = ni.getHardwareAddress();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MachineID getInstance() {
        if(instance == null)
            instance = new MachineID();
        return instance;
    }

    public byte[] getMachineID() {
        return mID;
    }
}
