package cs455.scaling.tasks;

import cs455.scaling.server.ClientConnection;
import cs455.scaling.server.Server;
import cs455.scaling.util.Util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static cs455.scaling.util.Util.DATA_SIZE;

/**
 * Class that represents tasks created by server to handle each client request.
 */
public class ChannelWorker implements IClientTask {


    private final SelectionKey key;
    private final ClientConnection client;
    private Server server;

    /**
     * Constructor takes channel key, ClientConnection object and reference back to server.
     * ClientConnection object holds read buffer and method to increment counts for the channel,
     * and server reference allows for call to method to remove channel from clients data structure if
     * socket is closed.
     */
    public ChannelWorker(SelectionKey key, ClientConnection client, Server server) {

        this.key = key;
        this.client = client;
        this.server = server;
    }

    @Override
    public void runClientTask() throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        int numRead;
        byte[] dataToHash = new byte[DATA_SIZE];
        List<ByteBuffer> responses = new ArrayList<>();  // To store multiple responses if necessary.

        // Locks ClientConnection object for this socket channel.
        synchronized (client) {

            try {
                numRead = sc.read(client.channelBuffer); // Read from channel into ClientConnection buffer.
            } catch (IOException e) {
                sc.close();                              // If socket was closed, clean up everything
                key.cancel();                            // and remove references to client objects.
                server.removeClient(sc);
                return;
            }
            if (numRead == -1) {
                sc.close();
                key.cancel();
                server.removeClient(sc);
                return;
            }
            while (client.channelBuffer.position() >= DATA_SIZE) {  // Loop over every set of complete data
                client.channelBuffer.flip();                        // Flip to read mode.
                client.channelBuffer.get(dataToHash);               // Get client data from read buffer.
                client.channelBuffer.compact();                     // Compact read buffer.
                byte[] hash = Util.getSHA1(dataToHash);             // Perform hash.
                ByteBuffer buf = ByteBuffer.wrap(hash);             // Prepare hash for send.
                responses.add(buf);                                 // Add to send list.
            }
            try {
                for (ByteBuffer response : responses) {
                    sc.write(response);                             // Write responses back to channel.
                    client.incrementSentCount();                    // Inform ClientConnection of send.
                }
            } catch (IOException e) {
                sc.close();
                key.cancel();
                server.removeClient(sc);
                return;
            }
            key.interestOps(SelectionKey.OP_READ);                  // Make key readable again.
        }
    }
}
