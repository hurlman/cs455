package cs455.overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class TCPReceiver extends Thread {

	private Socket socket;
	private DataInputStream din;

	public TCPReceiver(Socket socket) throws IOException {
		this.socket = socket;
		din = new DataInputStream(socket.getInputStream());
	}

	public void run() {

		int dataLength;
		while (socket != null) {
			try {
				dataLength = din.readInt();
				
				byte[] data = new byte[dataLength];
				din.readFully(data, 0, dataLength);
				//TODO Event factory
			}
			catch (SocketException se) {
				System.out.println(se.getMessage());
				break;
			}
			catch(IOException ioe) {
				System.out.println(ioe.getMessage());
				break;
			}
		}
	}

}
