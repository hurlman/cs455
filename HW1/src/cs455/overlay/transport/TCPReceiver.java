package cs455.overlay.transport;

import cs455.overlay.wireformats.EventFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPReceiver extends Thread {

    private Socket socket;
    private DataInputStream din;
    private TCPConnection tcpConnection;

    public TCPReceiver(Socket socket, TCPConnection tcpConnection) throws IOException {
        this.socket = socket;
        this.tcpConnection = tcpConnection;
        din = new DataInputStream(socket.getInputStream());
    }

    public void run() {

        int dataLength;
        while (socket != null) {
            try {
                dataLength = din.readInt();

                byte[] data = new byte[dataLength];
                din.readFully(data, 0, dataLength);

                EventFactory.getInstance().newMessage(data, tcpConnection);
            } catch (IOException ioe) {
                System.out.println("Socket closed " + ioe.getMessage());
                break;
            }
        }
    }

}
