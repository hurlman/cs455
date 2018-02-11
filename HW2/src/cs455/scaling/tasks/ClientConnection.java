package cs455.scaling.tasks;

import cs455.scaling.server.Server;
import cs455.scaling.util.Util;

import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class ClientConnection implements ClientTask {

    private Server server;
    private byte[] dataIn;
    private String response;
    private SocketChannel socket;
    private int processed = 0;

    public ClientConnection(SocketChannel socket) {
        this.socket = socket;
    }

    public void setNewTask(byte[] newData, int numRead){
        dataIn = new byte[numRead];
        dataIn = Arrays.copyOfRange(newData, 0, numRead);
    }
    @Override
    public void runClientTask() throws InterruptedException {
        response = Util.SHA1FromBytes(dataIn);
        server.queueSend(socket, response);
        incrementProcessed();
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
