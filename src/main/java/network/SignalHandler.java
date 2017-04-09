package main.java.network;

import main.java.util.MachineID;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SignalHandler {

    private final static String SCAN_COMMAND = "iwlist wlan0 scan | egrep 'Address|ESSID|Quality|Signal'";
    private final MachineID device;
    private final String iNetAddress;

    public SignalHandler(MachineID device, String iNetAddress) {
        this.device = device;
        this.iNetAddress = iNetAddress;
    }

    private String scan() {
        StringBuilder data = new StringBuilder();
        try {
            Process p = Runtime.getRuntime().exec(SCAN_COMMAND);
            p.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while((line = br.readLine()) != null) {
                data.append(line + "\n");
            }
        } catch(Exception e) {
            System.out.println(e);
        }
        return data.toString();
    }
}
