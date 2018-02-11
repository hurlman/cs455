package cs455.scaling.server;

//import cs455.scaling.tasks.TestClientTask;

import cs455.scaling.tasks.ClientConnection;
import cs455.scaling.thread.ThreadPoolManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

import static cs455.scaling.util.Util.BUFFER_SIZE;

public class Server implements Runnable {

    private Selector selector;
    private ThreadPoolManager pool;
    private ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    private final Map<SocketChannel, ClientConnection> clients = new HashMap<>();
    private final LinkedList<SocketChannel> socketsToWrite = new LinkedList<>();

    public static void main(String args[]) {
        try {
            ThreadPoolManager pool = new ThreadPoolManager(Integer.parseInt(args[1]));
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
    }

    public void run() {


        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                synchronized (socketsToWrite){
                    for(SocketChannel s : socketsToWrite){
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

        clients.put(csc, new ClientConnection(csc, this));
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        readBuffer.clear();
        int numRead;
        try {
            numRead = sc.read(readBuffer);
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

        clients.get(sc).setNewTask(readBuffer.array(), numRead);
        pool.execute(clients.get(sc));
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();

        LinkedList<ByteBuffer> queue = clients.get(sc).getResponses();
        while(!queue.isEmpty()){
            sc.write(queue.poll());
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