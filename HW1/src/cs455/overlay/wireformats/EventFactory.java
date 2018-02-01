package cs455.overlay.wireformats;

import java.net.InetAddress;
import java.util.Observable;

import cs455.overlay.node.Node;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.wireformats.Protocol.*;

import java.io.*;

/**
 * Raises events to Registry and Messaging Node when data arrives on a TCP connection via the newMessage method.
 * Singleton pattern to handle access from multiple classes and threads.  Raises events to subscriber.
 */
public final class EventFactory {

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

    /**
     * Called by TCPReceiver threads to raise events to Registry and Messaging Nodes that data has arrived.
     */
    public void newMessage(byte[] messageData, TCPConnection origin) throws IOException {

        int type = messageData[0];

        switch (MessageType.getMessageType(type)) {
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
            case REGISTRY_SENDS_NODE_MANIFEST:
                Subscriber.onEvent(new RegistrySendsNodeManifest(messageData), origin);
                break;
            case NODE_REPORTS_OVERLAY_SETUP_STATUS:
                Subscriber.onEvent(new NodeReportsOverlaySetupStatus(messageData), origin);
                break;
            case REGISTRY_REQUESTS_TASK_INITIATE:
                Subscriber.onEvent(new RegistryRequestsTaskInitiate(messageData), origin);
                break;
            case OVERLAY_NODE_SENDS_DATA:
                Subscriber.onEvent(new OverlayNodeSendsData(messageData), origin);
                break;
            case OVERLAY_NODE_REPORTS_TASK_FINISHED:
                Subscriber.onEvent(new OverlayNodeReportsTaskFinished(messageData), origin);
                break;
            case REGISTRY_REQUESTS_TRAFFIC_SUMMARY:
                Subscriber.onEvent(new RegistryRequestsTrafficSummary(messageData), origin);
                break;
            case OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY:
                Subscriber.onEvent(new OverlayNodeReportsTrafficSummary(messageData), origin);
                break;
        }
    }

}
