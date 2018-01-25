package cs455.overlay.wireformats;

import java.net.InetAddress;
import java.util.Observable;
import cs455.overlay.node.Node;
import cs455.overlay.transport.TCPConnection;
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
    
    public void newMessage(byte[] messageData, TCPConnection origin) throws IOException{
        ByteArrayInputStream baInputStream = 
                new ByteArrayInputStream(messageData);
        DataInputStream din = 
                new DataInputStream(new BufferedInputStream(baInputStream));
        
        int type = din.readByte();
        
        switch(MessageType.getMessageType(type)){
            case OVERLAY_NODE_SENDS_REGISTRATION:
                Subscriber.onEvent(new OverlayNodeSendsRegistration(messageData), origin);
                break;
            case REGISTRY_REPORTS_REGISTRATION_STATUS:
                Subscriber.onEvent(new RegistryReportsRegistrationStatus(messageData), origin);
                break;
            case OVERLAY_NODE_SENDS_DEREGISTRATION:
                Subscriber.onEvent(new OverlayNodeSendsDeregistration(messageData), origin);
                break;
            case REGISTRY_REPORTS_DEREGISTRATION_STATUS:
                Subscriber.onEvent(new RegistryReportsDeregistrationStatus(messageData), origin);
                break;
        }
    }

}