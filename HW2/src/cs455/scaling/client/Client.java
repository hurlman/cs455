package cs455.scaling.client;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import javax.jws.soap.SOAPBinding;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;

import static cs455.scaling.util.Util.BUFFER_SIZE;
import static cs455.scaling.util.Util.SHA1FromBytes;
import static cs455.scaling.util.Util.randBytes;

public class Client implements Runnable {

    private InetAddress serverIP;
    private int port;
    private int rate;
    private Selector selector;
    private ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private final LinkedList<ByteBuffer> dataToSend = new LinkedList<>();
    private final LinkedList<String> hashes = new LinkedList<>();
    private SocketChannel socket;

    public static void main(String[] args) {
        try {
            InetAddress serverIP = InetAddress.getByName(args[0]);
            int port = Integer.parseInt(args[1]);
            int rate = Integer.parseInt(args[2]);
            new Thread(new Client(serverIP, port, rate)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Client(InetAddress serverIP, int port, int rate) throws IOException {
        selector = Selector.open();
        this.serverIP = serverIP;
        this.port = port;
        this.rate = rate;
    }

    public void run() {
        try {

            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.connect(new InetSocketAddress(serverIP, port));
            sc.register(selector, SelectionKey.OP_CONNECT);

            while (true) {

                if(!dataToSend.isEmpty()){
                    SelectionKey key = socket.keyFor(selector);
                    key.interestOps(SelectionKey.OP_WRITE);
                }

                selector.select();
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {

                    SelectionKey key = it.next();
                    it.remove();
                    try {
                        if (!key.isValid()) {
                            continue;
                        }
                        if (key.isConnectable()) {
                            connect(key);
                        } else if (key.isWritable()) {
                            write(key);
                        } else if (key.isReadable()) {
                            read(key);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connect(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();

        if (sc.finishConnect()) {
            key.interestOps(SelectionKey.OP_WRITE);
            System.out.println("Connected to server!");
            sendRandomData();
        } else
            key.cancel();
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        readBuffer.clear();
        int numRead = sc.read(readBuffer);
        if (numRead < 0) {
            key.channel().close();
            key.cancel();
            throw new IOException("Socket was closed");
        }
        handleResponse(readBuffer.array(), numRead);
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();

        synchronized (dataToSend) {
            while (!dataToSend.isEmpty()) {
                sc.write(dataToSend.poll());
                System.out.println("Sent.");
            }
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private void handleResponse(byte[] data, int count) {
        byte[] dataCopy = Arrays.copyOfRange(data, 0, count);
        String hash = new String(dataCopy);
        synchronized (hashes) {
            if (hashes.remove(hash)) {
                System.out.println("Valid hash received! " + hash.substring(0, 8));
            } else {
                System.out.println("WARNING! Unknown hash received. " + hash.substring(0, 8));
            }
        }
    }

    private void sendRandomData() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                byte[] randomData = randBytes(BUFFER_SIZE);
                String hash = SHA1FromBytes(randomData);
                synchronized (dataToSend) {
                    dataToSend.add(ByteBuffer.wrap(randomData));
                }
                synchronized (hashes) {
                    hashes.add(hash);
                }
                System.out.println("Generating new data: " + hash.substring(0, 8));
            }
        }, 1000 / rate, 1000 / rate);
    }
}
