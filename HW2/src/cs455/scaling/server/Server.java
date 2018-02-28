package cs455.scaling.server;

import cs455.scaling.tasks.ChannelWorker;
import cs455.scaling.thread.ThreadPoolManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.sql.Timestamp;
import java.util.*;

import static cs455.scaling.util.Util.REPORT_INTERVAL;

/**
 * Server class.  Serves clients using a thread pool.
 */
public class Server implements Runnable {

    private Selector selector;
    private ThreadPoolManager pool;

    // Collection of client socket channels and ClientConnection objects.
    // Contains state information, read buffers and provides locking object.
    private final Map<SocketChannel, ClientConnection> clients = new HashMap<>();

    /**
     * Main initializes thread pool and passes it to server class.
     * Command line arguments are listening port and number of threads. Starts server thread.
     */
    public static void main(String args[]) {
        try {
            ThreadPoolManager pool = new ThreadPoolManager(Integer.parseInt(args[1]));
            pool.initialize();
            new Thread(new Server(Integer.parseInt(args[0]), pool)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor provides server with thread pool, port.  Initializes server socket channel
     * and selector, begins reporting task timer.
     */
    private Server(int port, ThreadPoolManager pool) throws IOException {
        this.pool = pool;
        selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println(String.format("Server open, listening on %s:%s",
                InetAddress.getLocalHost(), port));

        reportThroughput();
    }

    /**
     * Server selector loop.
     */
    public void run() {

        //noinspection InfiniteLoopStatement
        while (true) {
            try {

                selector.select();
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {

                    SelectionKey key = it.next();
                    it.remove();
                    try {
                        if (!key.isValid()) {
                            continue;
                        }
                        // While loop only contains accept and read.  Write was determined to be
                        // unnecessary in testing.
                        if (key.isAcceptable()) {
                            accept(key);
                        } else if (key.isReadable()) {
                            read(key);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Accepts socket connection, creates and registers client channel with selector.
     * Adds socket channel to clients data structure.
     */
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel csc = ssc.accept();
        csc.configureBlocking(false);
        csc.register(key.selector(), SelectionKey.OP_READ);

        // Locks data structure when adding new clients.
        synchronized (clients) {
            clients.put(csc, new ClientConnection());
        }
        System.out.println("Client connected. " + csc.getRemoteAddress());
    }

    /**
     * Creates task and sends it to the thread pool for processing when a client socket channel
     * is readable.  Interest Ops are removed from this key so that another thread does not try
     * to read from it in the next loop until task is complete.
     */
    private void read(SelectionKey key) {
        SocketChannel sc = (SocketChannel) key.channel();
        // Locks data structure to retrieve ClientConnection object for channel.
        synchronized (clients) {
            // Create new task.
            ChannelWorker cw = new ChannelWorker(key, clients.get(sc), this);
            key.interestOps(0);
            pool.execute(cw);   // Execute task with thread pool.
        }
    }

    /**
     * Provides a way for a thread to remove a client socket channel from the clients collection
     * when the socket disconnects.
     */
    public void removeClient(SocketChannel sc) {
        // Locks data structure to remove clients.
        synchronized (clients) {
            clients.remove(sc);
        }
    }

    /**
     * Creates reporting task to be run every REPORT_INTERVAL seconds.
     */
    private void reportThroughput() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                calculateAndPrintThroughput();
            }
        }, 1000 * REPORT_INTERVAL, 1000 * REPORT_INTERVAL);
    }

    /**
     * Called by reportThroughput to perform calculations and print to screen.
     * clients data structure is used to determine number of connected clients and obtain their
     * counts since the last call.
     */
    private void calculateAndPrintThroughput() {
        int N;
        List<Double> throughputs = new ArrayList<>();
        // Locks data structure to get current size and iterate over each client to get and
        // reset message counts.
        synchronized (clients) {
            N = clients.size();
            for (ClientConnection client : clients.values()) {
                throughputs.add((double) client.getAndResetSentCount() / REPORT_INTERVAL);
            }
        }
        if (N > 0) {
            double sum = 0;
            for (double tp : throughputs) {
                sum += tp;
            }
            double mean = sum / N;
            double variance = 0;
            for (double tp : throughputs) {
                variance += Math.pow(tp - mean, 2);
            }
            double stD = Math.sqrt(variance / N);

            System.out.printf("[%s] Server Throughput: %.2f messages/s, " +
                            "Active Client Connections: %s, " +
                            "Mean Per-client Throughput: %.2f messages/s, " +
                            "Std. Dev. Of Per-client Throughput: %.2f messages/s\n",
                    new Timestamp(System.currentTimeMillis()), sum, N, mean, stD);
        } else { // Needed to avoid divide by 0 situation.
            System.out.printf("[%s] Server Throughput: 0.0 messages/s, " +
                            "Active Client Connections: 0, " +
                            "Mean Per-client Throughput: 0.0 messages/s, " +
                            "Std. Dev. Of Per-client Throughput: 0.0 messages/s\n",
                    new Timestamp(System.currentTimeMillis()));
        }
    }
}