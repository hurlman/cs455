package cs455.overlay.node;

import cs455.overlay.transport.TCPConnection;
import cs455.overlay.util.InteractiveCommandParser.Command;
import cs455.overlay.wireformats.Event;

import java.net.InetAddress;

public interface Node {
    void onEvent(Event message, TCPConnection origin);
    void onCommand(Command command, int arg);
}
