package cs455.overlay.transport;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class TCPConnectionCache {
    private ServerSocket serverSocket;
    private List<TCPConnection> InboundConnections = new ArrayList<>();

    private TCPConnection RegistryConnection;

    public TCPConnectionCache(ServerSocket socket) {
        serverSocket = socket;
        new Thread(this::AcceptConnections).start();
    }

    public void setRegistryConnection(Socket clientSocket) throws IOException{
        RegistryConnection = new TCPConnection(clientSocket);
    }

    public void sendToRegistry(byte[] msgData){
        RegistryConnection.sendData(msgData);
    }

    private void AcceptConnections() {
        try {
            while (true) {
                Socket newSocket = serverSocket.accept();
                InboundConnections.add(new TCPConnection(newSocket));
                System.out.println("Client has connected.");
            }
        } catch (IOException e) {
            System.out.println(String.format("Error on client connection. %s.", e.getMessage()));
        }
    }
}
