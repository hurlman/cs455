package cs455.overlay.transport;

import java.io.IOException;
import java.net.Socket;

public class TCPConnection {
	
	public TCPSender Sender;
	public TCPReceiver Receiver;
		
	public TCPConnection(Socket socket) throws IOException {
		
		Sender = new TCPSender(socket);
		Receiver = new TCPReceiver(socket);
		Receiver.start();
	}
	
	public void sendData(byte[] dataToWrite) throws IOException {
		Sender.outQueue.add(dataToWrite);
	}
}
