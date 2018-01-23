package cs455.overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class TCPSender extends Thread {

	private DataOutputStream dout;
	public List<byte[]> outQueue = new ArrayList<>();
	
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
		while(true) {
			if(!outQueue.isEmpty()) {
				try {
					sendData(outQueue.get(0));
				} 
				catch (SocketException se) {
					System.out.println(se.getMessage());
					break;
				}
				catch(IOException ioe) {
					System.out.println(ioe.getMessage());
					break;
				}
				outQueue.remove(0);
			}
		}
	}
}
