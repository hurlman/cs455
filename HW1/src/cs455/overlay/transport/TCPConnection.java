package cs455.overlay.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class TCPConnection {

    private TCPSender Sender;
    private TCPReceiver Receiver;
    private InetAddress remoteIP;

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
    //TODO Something causing null on disconnect.

    public void sendData(byte[] dataToWrite) {
        try {
            Sender.outQueue.put(dataToWrite);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
