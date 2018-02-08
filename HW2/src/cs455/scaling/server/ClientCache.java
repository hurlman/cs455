package cs455.scaling.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static cs455.scaling.util.Util.BUFFER_SIZE;
import static cs455.scaling.util.Util.SHA1FromBytes;

public class ClientCache {

    private final Map<SocketChannel, ByteBuffer> clients = new HashMap<>();

    public void removeClients() {
        clients.keySet().removeIf(socket -> !socket.isOpen());
    }

    public void addClient(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel csc = ssc.accept();
        csc.configureBlocking(false);
        csc.register(key.selector(), SelectionKey.OP_READ);

        clients.put(csc, ByteBuffer.allocateDirect(BUFFER_SIZE));
    }

    public void readFromClient(SelectionKey key) throws IOException {
        SocketChannel csc = (SocketChannel) key.channel();
        ByteBuffer buffer = clients.get(csc);
        int data = csc.read(buffer);
        if (data == -1) {
            csc.close();
            clients.remove(csc);
        }
        byte[] bytes = new byte[data];
        buffer.get(bytes);
        String hash = SHA1FromBytes(bytes);
        buffer.flip();
        buffer.put(hash.getBytes());

        key.interestOps(SelectionKey.OP_WRITE);
    }

    public void writeToClient(SelectionKey key) throws IOException {
        SocketChannel csc = (SocketChannel) key.channel();
        ByteBuffer buffer = clients.get(csc);
        csc.write(buffer);
        if(!buffer.hasRemaining()){
            buffer.compact();
            key.interestOps(SelectionKey.OP_READ);
        }
    }
}
