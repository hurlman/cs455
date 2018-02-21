package cs455.scaling.server;

import cs455.scaling.tasks.Sha1Calculator;
import cs455.scaling.thread.ThreadPoolManager;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import static cs455.scaling.util.Util.DATA_SIZE;
import static cs455.scaling.util.Util.SERVER_BUFFER_SIZE;

public class ClientConnection {

    private ThreadPoolManager pool;
    private final LinkedList<ByteBuffer> responses = new LinkedList<>();
    private int sentCount = 0;

    public ByteBuffer channelBuffer = ByteBuffer.allocate(SERVER_BUFFER_SIZE);
    private byte[] dataToHash = new byte[DATA_SIZE];

    ClientConnection(ThreadPoolManager pool) {
        this.pool = pool;
    }

    public void handleData() {
        while (channelBuffer.position() >= DATA_SIZE){
            channelBuffer.flip();
            channelBuffer.get(dataToHash);
            hashIt();
            channelBuffer.compact();
        }
    }

    private void hashIt(){
        Sha1Calculator task = new Sha1Calculator(this, dataToHash);
        pool.execute(task);
    }

    public void handleResponse(ByteBuffer response) {
        synchronized (responses) {
            responses.add(response);
        }
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
