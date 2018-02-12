package cs455.scaling.tasks;

import cs455.scaling.server.ClientConnection;
import cs455.scaling.util.Util;

import java.nio.ByteBuffer;

public class Sha1Calculator implements ClientTask {

    private ClientConnection sender;
    private byte[] data;

    public Sha1Calculator(ClientConnection sender, byte[] data) {
        this.sender = sender;
        this.data = data;
    }

    @Override
    public void runClientTask(){
        String response = Util.SHA1FromBytes(data);
        ByteBuffer buf = ByteBuffer.wrap(response.getBytes());
        sender.handleResponse(buf);
    }
}
