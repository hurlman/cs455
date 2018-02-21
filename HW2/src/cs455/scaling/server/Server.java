package cs455.scaling.server;

import cs455.scaling.thread.ThreadPoolManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.sql.Timestamp;
import java.util.*;

import static cs455.scaling.util.Util.REPORT_INTERVAL;

public class Server implements Runnable {

    private Selector selector;
    private ThreadPoolManager pool;

    private final Map<SocketChannel, ClientConnection> clients = new HashMap<>();

    public static void main(String args[]) {
        try {
            ThreadPoolManager pool = new ThreadPoolManager(Integer.parseInt(args[1]));
            pool.initialize();
            new Thread(new Server(Integer.parseInt(args[0]), pool)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

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
                        if (key.isAcceptable()) {
                            accept(key);
                        } else if (key.isReadable()) {
                            read(key);
                        } else if (key.isWritable()) {
                            write(key);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    removeClients();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel csc = ssc.accept();
        csc.configureBlocking(false);
        csc.register(key.selector(), SelectionKey.OP_READ);

        clients.put(csc, new ClientConnection(pool));
        System.out.println("Client connected. " + csc.getRemoteAddress());
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        int numRead;
        try {
            numRead = sc.read(clients.get(sc).channelBuffer);
        } catch (IOException e) {
            sc.close();
            clients.remove(sc);
            key.cancel();
            return;
        }
        if (numRead == -1) {
            sc.close();
            clients.remove(sc);
            key.cancel();
            return;
        }
        clients.get(sc).handleData();
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();

        LinkedList<ByteBuffer> queue = clients.get(sc).getResponses();
        for (ByteBuffer buf : queue) {
            sc.write(buf);
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private void removeClients() {
        clients.keySet().removeIf(socket -> !socket.isOpen());
    }

    private void reportThroughput() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                calculateAndPrintThroughput();
            }
        }, 1000 * REPORT_INTERVAL, 1000 * REPORT_INTERVAL);
    }

    private void calculateAndPrintThroughput() {
        int N = clients.size();
        if (N > 0) {
            List<Double> throughputs = new ArrayList<>();
            synchronized (clients) {
                for (ClientConnection client : clients.values()) {
                    throughputs.add((double) client.getAndResetSentCount() / REPORT_INTERVAL);
                }
            }
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
                    new Timestamp(System.currentTimeMillis()), sum, N,
                    mean, stD);
        } else {
            System.out.printf("[%s] Server Throughput: 0.0 messages/s, " +
                            "Active Client Connections: 0, " +
                            "Mean Per-client Throughput: 0.0 messages/s, " +
                            "Std. Dev. Of Per-client Throughput: 0.0 messages/s\n",
                    new Timestamp(System.currentTimeMillis()));
        }
    }
}