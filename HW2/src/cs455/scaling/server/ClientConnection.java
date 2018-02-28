package cs455.scaling.server;

import java.nio.ByteBuffer;

import static cs455.scaling.util.Util.SERVER_BUFFER_SIZE;

/**
 * Class that maintains read buffer and sent count for each client socket channel.
 * This object is also used for locking to prevent multiple threads from acting on the same
 * socket channel.
 */
public class ClientConnection {

    private int sentCount = 0;

    /**
     * Buffer that is read into from the client channel.  This buffer will hold remainders of
     * incomplete messages that were read so that it is available for the next time the
     * channel is read from.
     */
    public ByteBuffer channelBuffer = ByteBuffer.allocate(SERVER_BUFFER_SIZE);

    /**
     * Method for thread to call to track responses sent on this channel.
     */
    public synchronized void incrementSentCount() {
        sentCount++;
    }

    /**
     * Method for server to call to get the current sent count, and resets it.
     */
    public synchronized int getAndResetSentCount() {
        int temp = sentCount;
        sentCount = 0;
        return temp;
    }
}
