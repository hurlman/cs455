package cs455.scaling.server;

import cs455.scaling.tasks.TestClientTask;
import cs455.scaling.thread.ThreadPoolManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import static cs455.scaling.util.Util.BUFFER_SIZE;

public class Server {

    Selector something;
    private static HashMap<String, Integer> tracker = new HashMap<>();

    public static void main(String args[]) {
        try {
            ThreadPoolManager pool = new ThreadPoolManager(Integer.parseInt(args[1]));

            Selector selector = Selector.open();
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(Integer.parseInt(args[0])));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                selector.select();

                for (SelectionKey key : selector.selectedKeys()) {

                    if (key.isAcceptable()) {
                        registerClient(selector, serverSocket);
                    } else if (key.isReadable()) {
                        // a channel is ready for reading

                    } else if (key.isWritable()) {
                        // a channel is ready for writing
                    }
                }
            }


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

        } catch (NumberFormatException nfe) {
            System.out.println("Invalid argument. " + nfe.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void registerClient(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    private static void reportThreadUsage() {

        for (Map.Entry e : tracker.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
    }

    public synchronized static void countThreadUsage(String threadName) {
        tracker.merge(threadName, 1, (a, b) -> a + b);
    }
}
