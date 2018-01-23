package cs455.overlay.transport;

import java.io.IOException;
import java.net.Socket;

public class TCPConnection {

    private TCPSender Sender;
    private TCPReceiver Receiver;

    public TCPConnection(Socket socket) throws IOException {

        Sender = new TCPSender(socket);
        Receiver = new TCPReceiver(socket);
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
