package main.java.network;

import main.java.log.Logger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DeviceServer implements Runnable {

    private final static int PORT = 8000;
    private final static int MAX_CLIENTS = 256;
    private final static Executor POOL = Executors.newFixedThreadPool(MAX_CLIENTS);

    private static DeviceServer instance = new DeviceServer();

    public DeviceServer getInstance(){
        return instance;
    }

    @Override
    public void run() {
        ServerSocket host = null;
        Socket client = null;
        String localAddress = "";
        try{
                Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

                while(e.hasMoreElements()){
                    NetworkInterface ni = e.nextElement();
                    Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                    if(ni.getName().equals("wlan0")){
                        while(inetAddresses.hasMoreElements()){
                            InetAddress ia = inetAddresses.nextElement();
                            if(!ia.isLinkLocalAddress()){
                                localAddress = ia.getHostAddress();
                            }
                        }
                    }
                }
            host = new ServerSocket(PORT, 50, InetAddress.getByName(localAddress));

            while(true){
                client = host.accept();
                Logger.info("Accepted socket from new device: IP:" + client.getLocalSocketAddress() + ".");
                POOL.execute(new LocalClient(client));
            }
        } catch(Exception e){
            Logger.error("Error while running DeviceServer.\n\n" + e.getMessage());
        } finally {
            try{
                if(host != null)
                    host.close();
            }catch(Exception e){
                Logger.error("Could not close server socket.\n\n" + e.getMessage());
            }
        }
    }
}
