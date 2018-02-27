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

public class ChannelWorker implements ClientTask {


    private final SelectionKey key;
    private final ClientConnection client;
    private Server server;

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
        List<ByteBuffer> responses = new ArrayList<>();
        synchronized (client) {

            try {
                numRead = sc.read(client.channelBuffer);
            } catch (IOException e) {
                sc.close();
                key.cancel();
                server.removeClient(sc);
                return;
            }
            if (numRead == -1) {
                sc.close();
                key.cancel();
                server.removeClient(sc);
                return;
            }
            while (client.channelBuffer.position() >= DATA_SIZE) {
                client.channelBuffer.flip();
                client.channelBuffer.get(dataToHash);
                client.channelBuffer.compact();
                String hash = Util.SHA1FromBytes(dataToHash);
                ByteBuffer buf = ByteBuffer.wrap(hash.getBytes());
                responses.add(buf);
            }
            ByteBuffer[] responseBufs = new ByteBuffer[responses.size()];
            responses.toArray(responseBufs);
            try {
                sc.write(responseBufs);
            } catch (IOException e) {
                sc.close();
                key.cancel();
                server.removeClient(sc);
                return;
            }
            client.incrementSentCount(responseBufs.length);

            key.interestOps(SelectionKey.OP_READ);
        }
    }
}
