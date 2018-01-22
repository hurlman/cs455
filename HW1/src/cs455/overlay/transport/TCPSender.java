package cs455.overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class TCPSender extends Thread {

	private Socket socket;
	private DataOutputStream dout;
	public List<byte[]> outQueue;
	
	public TCPSender(Socket socket) throws IOException {
		this.socket = socket;
		dout = new DataOutputStream(socket.getOutputStream());
		outQueue = new ArrayList<byte[]>();
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
