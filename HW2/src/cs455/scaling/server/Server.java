package cs455.scaling.server;

//import cs455.scaling.tasks.TestClientTask;
import cs455.scaling.thread.ThreadPoolManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Iterator;

import static cs455.scaling.util.Util.BUFFER_SIZE;

public class Server {

    Selector something;
    private static HashMap<String, Integer> tracker = new HashMap<>();

    public static void main(String args[]) {
        try {
            ThreadPoolManager pool = new ThreadPoolManager(Integer.parseInt(args[1]));
            ClientCache clients = new ClientCache();

            Selector selector = Selector.open();
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(Integer.parseInt(args[0])));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
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
                            clients.addClient(key);
                        } else if (key.isReadable()) {
                            clients.readFromClient(key);
                        } else if (key.isWritable()) {
                            clients.writeToClient(key);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    clients.removeClients();
                }
            }


        } catch (NumberFormatException nfe) {
            System.out.println("Invalid argument. " + nfe.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
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