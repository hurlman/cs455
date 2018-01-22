package cs455.overlay.routing;

import java.net.Socket;
import java.util.List;

public class RoutingEntry {
    public Socket Socket;
    public byte[] IPAddress;
    public int Port;
    public List<Integer> RoutingTable;
}
