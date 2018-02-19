package cs455.scaling.server;

//import cs455.scaling.tasks.TestClientTask;

import cs455.scaling.thread.ThreadPoolManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.*;

import static cs455.scaling.util.Util.REPORT_INTERVAL;
import static cs455.scaling.util.Util.SERVER_BUFFER_SIZE;

public class Server implements Runnable {

    private Selector selector;
    private ThreadPoolManager pool;

    private final Map<SocketChannel, ClientConnection> clients = new HashMap<>();
    private final Set<SocketChannel> socketsToWrite = new HashSet<>();

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
                synchronized (socketsToWrite) {
                    for (SocketChannel s : socketsToWrite) {
                        SelectionKey key = s.keyFor(selector);
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                    socketsToWrite.clear();
                }

                selector.select();  //blocking
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

        clients.put(csc, new ClientConnection(csc, this, pool));
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
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();

        LinkedList<ByteBuffer> queue = clients.get(sc).getResponses();
        for (ByteBuffer buf : queue) {
            sc.write(buf);
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    public void queueSend(SocketChannel socket) {
        synchronized (socketsToWrite) {
            socketsToWrite.add(socket);
        }
        selector.wakeup();
    }

    private void removeClients() {
        clients.keySet().removeIf(socket -> !socket.isOpen());
    }

    public void reportThroughput() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                calculateAndPrintThroughput();
            }
        }, 1000 * REPORT_INTERVAL, 1000 * REPORT_INTERVAL);
    }

    public void calculateAndPrintThroughput(){
        int N = clients.size();
        if(N > 0) {
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
        }
        else{
            System.out.printf("[%s] Server Throughput: 0.0 messages/s, " +
                            "Active Client Connections: 0, " +
                            "Mean Per-client Throughput: 0.0 messages/s, " +
                            "Std. Dev. Of Per-client Throughput: 0.0 messages/s\n",
                    new Timestamp(System.currentTimeMillis()));
        }
    }
}


//    private static void reportThreadUsage() {
//
//        for (Map.Entry e : tracker.entrySet()) {
//            System.out.println(e.getKey() + ": " + e.getValue());
//        }
//    }
//
//    public synchronized static void countThreadUsage(String threadName) {
//        tracker.merge(threadName, 1, (a, b) -> a + b);
//    }

//            for (int i = 0; i < 100; i++) {
//                pool.execute(new TestClientTask());
//
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            try {
//                Thread.sleep(102);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            reportThreadUsage();