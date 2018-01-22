package cs455.overlay.wireformats;

import java.util.Observable;
import cs455.overlay.node.Node;
import cs455.overlay.wireformats.Protocol.*;
import java.io.*;

public final class EventFactory extends Observable {

    private static final EventFactory INSTANCE = new EventFactory();

    private EventFactory() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static EventFactory getInstance() {
        return INSTANCE;
    }
    private Node Subscriber;

    public void subscribe(Node subscriber) {
        Subscriber = subscriber;
    }
    
    public void newMessage(byte[] messageData) throws IOException{
        ByteArrayInputStream baInputStream = 
                new ByteArrayInputStream(messageData);
        DataInputStream din = 
                new DataInputStream(new BufferedInputStream(baInputStream));
        
        int type = din.readInt();
        
        switch(MessageType.getMessageType(type)){
            case OVERLAY_NODE_SENDS_REGISTRATION:
                Subscriber.onEvent(new OverlayNodeSendsRegistration(messageData));
        }
    }

}
