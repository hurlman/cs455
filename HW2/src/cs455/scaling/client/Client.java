package cs455.scaling.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.*;

import static cs455.scaling.util.Util.*;

public class Client implements Runnable {

    private InetAddress serverIP;
    private int port;
    private int rate;
    private int thread = -1;
    private Selector selector;
    private ByteBuffer readBuffer = ByteBuffer.allocate(CLIENT_BUFFER_SIZE);
    private final LinkedList<ByteBuffer> dataToSend = new LinkedList<>();
    private final LinkedList<String> hashes = new LinkedList<>();
    private SocketChannel socketChannel;
    private int sentCount = 0;
    private int receivedCount = 0;

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

    private Client(InetAddress serverIP, int port, int rate) throws IOException {
        selector = Selector.open();
        this.serverIP = serverIP;
        this.port = port;
        this.rate = rate;
    }

    Client(InetAddress serverIP, int port, int rate, int thread) throws IOException {
        selector = Selector.open();
        this.serverIP = serverIP;
        this.port = port;
        this.rate = rate;
        this.thread = thread;
    }

    public void run() {
        try {

            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(serverIP, port));
            socketChannel.register(selector, SelectionKey.OP_CONNECT);

            //noinspection InfiniteLoopStatement
            while (true) {


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
            log("Connected to server!");
            sendRandomData();
            reportCounts();
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
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();

        synchronized (dataToSend) {
            if (!dataToSend.isEmpty()) {
                for (ByteBuffer buf : dataToSend) {
                    sc.write(buf);
                    incrementDataSent();
                }
                dataToSend.clear();
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    private void handleResponse(byte[] data, int count) {
        byte[] dataCopy = Arrays.copyOfRange(data, 0, count);
        String hash = new String(dataCopy);
        synchronized (hashes) {
            if (hashes.remove(hash)) {
                incrementDataReceived();
            } else {
                log("WARNING! Unknown hash received. " + hash.substring(0, 8));
            }
        }
    }

    private synchronized void incrementDataSent() {
        sentCount++;
    }

    private synchronized int getAndResetSentCount() {
        int temp = sentCount;
        sentCount = 0;
        return temp;
    }

    private synchronized void incrementDataReceived() {
        receivedCount++;
    }

    private synchronized int getAndResetReceivedCount() {
        int temp = receivedCount;
        receivedCount = 0;
        return temp;
    }

    private void sendRandomData() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                byte[] randomData = randBytes(DATA_SIZE);
                String hash = SHA1FromBytes(randomData);
                synchronized (dataToSend) {
                    dataToSend.add(ByteBuffer.wrap(randomData));
                }
                synchronized (hashes) {
                    hashes.add(hash);
                }
                wakeUpSelector();
            }
        }, 1000 / rate, 1000 / rate);
    }

    private void reportCounts() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                log(String.format("Total Sent Count: %s, Total Received Count: %s",
                        getAndResetSentCount(), getAndResetReceivedCount()));
            }
        }, 1000 * REPORT_INTERVAL, 1000 * REPORT_INTERVAL);
    }

    private void wakeUpSelector() {

        SelectionKey key = this.socketChannel.keyFor(selector);
        key.interestOps(SelectionKey.OP_WRITE);
        selector.wakeup();
    }

    private void log(String message) {
        if (thread < 0) {
            System.out.println(String.format("[%s] %s",
                    new Timestamp(System.currentTimeMillis()), message));
        } else {
            System.out.println(String.format("[%s] Thread %s: %s",
                    new Timestamp(System.currentTimeMillis()), thread, message));
        }
    }
}
