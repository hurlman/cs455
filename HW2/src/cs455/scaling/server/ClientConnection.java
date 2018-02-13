package cs455.scaling.server;

import cs455.scaling.tasks.Sha1Calculator;
import cs455.scaling.thread.ThreadPoolManager;
import cs455.scaling.util.Util;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.LinkedList;

import static cs455.scaling.util.Util.DATA_SIZE;

public class ClientConnection {

    private Server server;
    private ThreadPoolManager pool;
    private final LinkedList<ByteBuffer> responses = new LinkedList<>();
    private SocketChannel socket;
    private int processed = 0;

    byte[] dataIn = new byte[DATA_SIZE];
    int index = 0;

    ClientConnection(SocketChannel socket, Server server, ThreadPoolManager pool) {
        this.socket = socket;
        this.server = server;
        this.pool = pool;
    }

    public void handleData(byte[] newData, int numRead) {
        try {
            System.arraycopy(newData, 0, dataIn, index, numRead);
        }catch (IndexOutOfBoundsException e){
            System.out.println("Data corrupted, discarding.");
            index = 0;
        }
        index += numRead;
        if(index == DATA_SIZE) {
            Sha1Calculator task = new Sha1Calculator(this, dataIn);
            pool.execute(task);
            index = 0;
        }
    }

    public void handleResponse(ByteBuffer response) {
        synchronized (responses) {
            responses.add(response);
        }
        incrementProcessed();
        server.queueSend(socket);
    }

    public LinkedList<ByteBuffer> getResponses() {
        LinkedList<ByteBuffer> out = new LinkedList<>();
        synchronized (responses) {
            while (!responses.isEmpty()) {
                out.add(responses.poll());
            }
        }
        return out;
    }

    private synchronized void incrementProcessed() {
        processed++;
    }

    public synchronized int getTasksProcessed() {
        int temp = processed;
        processed = 0;
        return temp;
    }
}
