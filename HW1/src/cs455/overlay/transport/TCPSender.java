package cs455.overlay.transport;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class TCPSender extends Thread {

    private DataOutputStream dout;
    public BlockingQueue<byte[]> outQueue = new LinkedBlockingQueue<>();

    public TCPSender(Socket socket) throws IOException {
        dout = new DataOutputStream(socket.getOutputStream());
    }

    private void sendData(byte[] dataToSend) throws IOException {
        int dataLength = dataToSend.length;
        dout.writeInt(dataLength);
        dout.write(dataToSend, 0, dataLength);
        dout.flush();
    }

    public void run() {
        while (true) {
            try {
                sendData(outQueue.take());
            } catch (InterruptedException | IOException e) {
                System.out.println(e.getMessage());
                break;
            }
        }
    }
}
