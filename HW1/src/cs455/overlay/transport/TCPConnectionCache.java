package cs455.overlay.transport;

import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * Creates collection of TCP connections that are kept open indefinitely.  Server socket listens for new
 * connections and spins up a new TCP connection with its own send and receive threads when a connection is accepted.
 */
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

    public void setRegistryConnection(Socket clientSocket) throws IOException {
        RegistryConnection = new TCPConnection(clientSocket);
    }

    /**
     * Simple way for a messaging node to refer and send messages to the registry.
     */
    public void sendToRegistry(byte[] msgData) {
        RegistryConnection.sendData(msgData);
    }

    /**
     * Runs on its own thread. Creates new TCP connections.
     */
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
