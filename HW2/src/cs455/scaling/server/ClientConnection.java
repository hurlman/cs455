package cs455.scaling.server;

import cs455.scaling.tasks.Sha1Calculator;
import cs455.scaling.thread.ThreadPoolManager;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import static cs455.scaling.util.Util.DATA_SIZE;

public class ClientConnection {

    private Server server;
    private ThreadPoolManager pool;
    private final LinkedList<ByteBuffer> responses = new LinkedList<>();
    private SocketChannel socket;
    private int sentCount = 0;

    private byte[] dataIn = new byte[DATA_SIZE];
    private int index = 0;

    ClientConnection(SocketChannel socket, Server server, ThreadPoolManager pool) {
        this.socket = socket;
        this.server = server;
        this.pool = pool;
    }

    public void handleData(byte[] newData, int numRead) {
        try {
            System.arraycopy(newData, 0, dataIn, index, numRead);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Data corrupted, discarding.");
            index = 0;
        }
        index += numRead;
        if (index == DATA_SIZE) {
            Sha1Calculator task = new Sha1Calculator(this, dataIn);
            pool.execute(task);
            index = 0;
        }
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
