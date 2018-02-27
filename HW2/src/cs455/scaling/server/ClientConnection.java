package cs455.scaling.server;

import java.nio.ByteBuffer;

import static cs455.scaling.util.Util.SERVER_BUFFER_SIZE;

public class ClientConnection {

    private int sentCount = 0;

    public ByteBuffer channelBuffer = ByteBuffer.allocate(SERVER_BUFFER_SIZE);

    public synchronized void incrementSentCount(int count) {
        sentCount += count;
    }

    public synchronized int getAndResetSentCount() {
        int temp = sentCount;
        sentCount = 0;
        return temp;
    }
}
