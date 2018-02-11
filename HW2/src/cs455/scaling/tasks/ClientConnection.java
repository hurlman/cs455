package cs455.scaling.tasks;

import cs455.scaling.server.Server;
import cs455.scaling.util.Util;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.LinkedList;

public class ClientConnection implements ClientTask {

    private Server server;
    private byte[] dataIn;
    private final LinkedList<ByteBuffer> responses = new LinkedList<>();
    private SocketChannel socket;
    private int processed = 0;

    public ClientConnection(SocketChannel socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void setNewTask(byte[] newData, int numRead){
        dataIn = new byte[numRead];
        dataIn = Arrays.copyOfRange(newData, 0, numRead);
    }

    @Override
    public void runClientTask() throws InterruptedException {
        String response = Util.SHA1FromBytes(dataIn);
        synchronized (responses){
            responses.add(ByteBuffer.wrap(response.getBytes()));
        }
        incrementProcessed();
        server.queueSend(socket);
    }

    public LinkedList<ByteBuffer> getResponses(){
        LinkedList<ByteBuffer> out;
        synchronized (responses){
            // deep copy?
            out = (LinkedList<ByteBuffer>) responses.clone();
            responses.clear();
        }
        return out;
    }

    private synchronized void incrementProcessed(){
        processed++;
    }

    public synchronized int getTasksProcessed(){
        int temp = processed;
        processed = 0;
        return temp;
    }
}
