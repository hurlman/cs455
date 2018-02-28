package cs455.scaling.client;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.*;

import static cs455.scaling.util.Util.*;

/**
 * Main client class.  Connects to sever and sends random 8kB data at rate R.
 * Calculates hash of random data and waits for and compares it to server response.
 */
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

    /**
     * Begins a single client thread.
     */
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

    /**
     * Private constructor for single thread use.  Sets server info, rate, and initializes selector.
     */
    private Client(InetAddress serverIP, int port, int rate) throws IOException {
        selector = Selector.open();
        this.serverIP = serverIP;
        this.port = port;
        this.rate = rate;
    }

    /**
     * Constructor overload for MultiClient use.  Tracks client thread in logging.
     */
    Client(InetAddress serverIP, int port, int rate, int thread) throws IOException {
        selector = Selector.open();
        this.serverIP = serverIP;
        this.port = port;
        this.rate = rate;
        this.thread = thread;
    }

    public void run() {
        try {

            // Create socket channel, begin connection to server and register with selector.
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

    /**
     * Completes connection to server if channel is connectable.  Begins tasks of generating random
     * data and reporting sent/received counts.
     */
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

    /**
     * If channel is readable, reads into the readBuffer.  Pops hashes off the buffer and
     * compacts it.
     */
    private void read(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        byte[] data = new byte[20];
        int numRead = sc.read(readBuffer);
        if (numRead < 0) {
            key.channel().close();
            key.cancel();
            throw new IOException("Socket was closed");
        }
        while (readBuffer.position() >= 20) {
            readBuffer.flip();
            readBuffer.get(data);
            readBuffer.compact();
            handleResponse(data);
        }
    }

    /**
     * If channel is writeable, writes all ByteBuffers that are queued to send.
     * Increments sent counter.  Sets interestOps back to OP_READ for the key.
     */
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

    /**
     * Converts the 20 byte hash to a string to compare to the linked list of hashes sent.
     * removes from list if there is a match.  Logs if it's a mismatch.  Increments received count.
     */
    private void handleResponse(byte[] data) {

        BigInteger hashInt = new BigInteger(1, data);
        String hash = hashInt.toString(16);
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

    /**
     * Queues random 8kB of data to send to the server at rate/s interval.  Computes hash
     * of data and adds to linked list of hashes.  Notifies selector channel has data to write.
     */
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

    /**
     * Writes to screen number of messages sent and received last REPORT_INTERVAL seconds.
     * Resets counts.
     */
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

    /**
     * Sets interestOps to OP_WRITE for the key for this client's socket channel.
     * Wakes up selector if it was blocking.
     */
    private void wakeUpSelector() {

        SelectionKey key = this.socketChannel.keyFor(selector);
        key.interestOps(SelectionKey.OP_WRITE);
        selector.wakeup();
    }

    /**
     * Prepends thread number if running in MultiClient mode.
     */
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
