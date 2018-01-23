package cs455.overlay.node;

import cs455.overlay.wireformats.Event;

import java.net.InetAddress;

public interface Node {
    void onEvent(Event message, InetAddress origin);
}
