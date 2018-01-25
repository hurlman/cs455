package cs455.overlay.transport;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class TCPConnectionCache {
    private ServerSocket serverSocket;
    private List<TCPConnection> InboundConnections = new ArrayList<>();

    // Used by overlay nodes only.
    private TCPConnection RegistryConnection;

    // Immediately begin accepting connections.
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
            }
        } catch (IOException e) {
            System.out.println(String.format("Error on client connection. %s.", e.getMessage()));
        }
    }
}
