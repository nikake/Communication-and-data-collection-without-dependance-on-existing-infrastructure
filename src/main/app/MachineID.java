package main.app;

import java.net.InetAddress;
import java.net.NetworkInterface;

public class MachineID {

    private byte[] mID;

    public MachineID() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(ip);
            mID = ni.getHardwareAddress();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] getMachineID() {
        return mID;
    }
}
