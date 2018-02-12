package cs455.scaling.client;

import java.io.IOException;
import java.net.InetAddress;

public class MultiClient {
    public static void main(String[] args) {
        try {
            InetAddress serverIP = InetAddress.getByName(args[0]);
            int port = Integer.parseInt(args[1]);
            int rate = Integer.parseInt(args[2]);
            int numClients = Integer.parseInt(args[3]);
            for (int i = 0; i < numClients; i++) {
                new Thread(new Client(serverIP, port, rate)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
