package cs455.overlay.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Contains a sender and receiver thread.  Receiver raises events when data arrives.  Public
 * method to queue up data to send on sender thread.
 */
public class TCPConnection {

    private TCPSender Sender;
    private TCPReceiver Receiver;
    private InetAddress remoteIP;

    /**
     * Used by registry to verify source of data against message.
     */
    public InetAddress getRemoteIP() {
        return remoteIP;
    }

    public TCPConnection(Socket socket) throws IOException {

        remoteIP = socket.getInetAddress();
        Sender = new TCPSender(socket);
        Receiver = new TCPReceiver(socket, this);
        Sender.start();
        Receiver.start();
    }
    //TODO disconnect handling more gracefully?

    public void sendData(byte[] dataToWrite) {
        try {
            Sender.outQueue.put(dataToWrite);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
