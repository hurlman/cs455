package cs455.scaling.server;

import cs455.scaling.tasks.Sha1Calculator;
import cs455.scaling.thread.ThreadPoolManager;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import static cs455.scaling.util.Util.DATA_SIZE;
import static cs455.scaling.util.Util.SERVER_BUFFER_SIZE;

public class ClientConnection {

    private Server server;
    private ThreadPoolManager pool;
    private final LinkedList<ByteBuffer> responses = new LinkedList<>();
    private SocketChannel socket;
    private int sentCount = 0;

    private ByteBuffer channelBuffer = ByteBuffer.allocateDirect(SERVER_BUFFER_SIZE);
    private byte[] dataToHash = new byte[DATA_SIZE];

    ClientConnection(SocketChannel socket, Server server, ThreadPoolManager pool) {
        this.socket = socket;
        this.server = server;
        this.pool = pool;
    }

    public void handleData(byte[] newData, int numRead) {
        channelBuffer.put(newData, 0, numRead);
        while (channelBuffer.position() > DATA_SIZE){
            channelBuffer.flip();
            channelBuffer.get(dataToHash);
            hashIt();
            channelBuffer.compact();
        }
    }

    private void hashIt(){
        Sha1Calculator task = new Sha1Calculator(this, dataToHash);
        task.runClientTask();
    }

    public void handleResponse(ByteBuffer response) {
        synchronized (responses) {
            responses.add(response);
        }
        server.queueSend(socket);
    }

    public LinkedList<ByteBuffer> getResponses() {
        LinkedList<ByteBuffer> out = new LinkedList<>();
        synchronized (responses) {
            while (!responses.isEmpty()) {
                out.add(responses.poll());
                incrementSentCount();
            }
        }
        return out;
    }

    private synchronized void incrementSentCount() {
        sentCount++;
    }

    public synchronized int getAndResetSentCount() {
        int temp = sentCount;
        sentCount = 0;
        return temp;
    }
}
