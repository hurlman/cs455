package cs455.overlay.routing;

import cs455.overlay.transport.TCPConnection;

import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Abstraction representing a messaging node. Used by both Registry and messaging nodes
 * to hold information about other nodes.
 */
public class RoutingEntry {
    public TCPConnection tcpConnection;
    public byte[] IPAddress;
    public int Port;
    public int ID;

    // Flags to track each node completing various tasks.
    public boolean OverlayConnectionsMade = false;
    public boolean TaskFinished = false;
    public boolean ReportReceived = false;

    public RoutingEntry(TCPConnection tcpConnection, byte[] IPAddress, int port) {
        this.tcpConnection = tcpConnection;
        this.IPAddress = IPAddress;
        Port = port;
    }

    public RoutingEntry(byte[] IPAddress, int port, int id) {
        this.IPAddress = IPAddress;
        Port = port;
        ID = id;
    }

    // Overriding equality comparison to determine IP/Port registration
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoutingEntry that = (RoutingEntry) o;
        return Port == that.Port &&
                Arrays.equals(IPAddress, that.IPAddress);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(Port);
        result = 31 * result + Arrays.hashCode(IPAddress);
        return result;
    }
}
