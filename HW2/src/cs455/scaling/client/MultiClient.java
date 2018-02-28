package cs455.scaling.client;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Class used to simplify testing.  Spins up N number of clients in separate threads
 * with single command line. Command line arguments are as follows:
 * ServerIP, ServerPort, Rate, NumberOfClients
 */
public class MultiClient {
    public static void main(String[] args) {
        try {
            InetAddress serverIP = InetAddress.getByName(args[0]);
            int port = Integer.parseInt(args[1]);
            int rate = Integer.parseInt(args[2]);
            int numClients = Integer.parseInt(args[3]);
            for (int i = 0; i < numClients; i++) {
                new Thread(new Client(serverIP, port, rate, i)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
