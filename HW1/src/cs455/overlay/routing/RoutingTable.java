package cs455.overlay.routing;

import cs455.overlay.transport.TCPConnection;

import java.net.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.*;

/**
 * Used by overlay node to store information on nodes in its routing table.  Contains public functions to
 * set up TCP connections, get appropriate destinations for data according the sink ID, and get random destinations
 * for new messages.
 */
public class RoutingTable {
    private Map<Integer, RoutingEntry> routingTable = new ConcurrentHashMap<>();
    private int[] orderedNodes;
    private int ID;
    private int[] clockwiseRoute;

    /**
     * Initialized with a list of routing entries and list of nodes, along with the owning node ID.
     */
    public RoutingTable(List<RoutingEntry> routingEntries, int[] orderedNodes, int id) {
        this.orderedNodes = orderedNodes;
        ID = id;
        for (RoutingEntry re : routingEntries) {
            routingTable.put(re.ID, re);
        }
        setClockwiseRoute();
    }

    /**
     * Creates TCP connections for all entries in the routing table. Run first.
     */
    public String setupConnections() throws IOException {
        StringBuilder msg = new StringBuilder("Connection made to nodes: ");
        for (Map.Entry<Integer, RoutingEntry> rme : routingTable.entrySet()) {
            RoutingEntry re = rme.getValue();
            InetAddress nodeAddr = InetAddress.getByAddress(re.IPAddress);
            Socket clientSocket = new Socket(nodeAddr, re.Port);
            re.tcpConnection = new TCPConnection(clientSocket);

            msg.append(re.ID).append(", ");
            System.out.println(String.format("Connection made to node %s.  %s:%s",
                    re.ID, nodeAddr.getHostAddress(), re.Port));
        }
        return msg + "\b\b  ";
    }

    /**
     * Returns TCPConnection of next node along routing table towards sink
     */
    public TCPConnection getDest(int sink) {
        int dest = 0;
        for (int i = 0; i < clockwiseRoute.length; i++) {
            dest = clockwiseRoute[i];
            if (dest == sink) break;
            if (i > 1) {
                if (clockwiseRoute[i] < clockwiseRoute[i - 1]) {  // We looped around.
                    if (sink > clockwiseRoute[i - 1] || sink < clockwiseRoute[i]) {
                        dest = clockwiseRoute[i - 1];             // And sink is between this hop and last.
                        break;
                    }
                } else if (clockwiseRoute[i] > sink && sink > clockwiseRoute[i - 1]) { // Didn't loop around.
                    dest = clockwiseRoute[i - 1];
                    break;
                }
            }
        }
        return routingTable.get(dest).tcpConnection;
    }

    /**
     * Returns a random NodeID destination that is not itself.
     */
    public int getRandomDest() {
        int randomNum;
        do {
            randomNum = ThreadLocalRandom.current().nextInt(0, orderedNodes.length);
        } while (orderedNodes[randomNum] == ID);
        return orderedNodes[randomNum];
    }

    /**
     * Creates an int array with the clockwise route of the routing table nodes.
     * Used for determining next destination of a message.
     */
    private void setClockwiseRoute() {
        clockwiseRoute = new int[routingTable.size()];

        // Crawl trough nodes clockwise starting at current node ID.
        int pos = indexOf(ID, orderedNodes);
        for (int i = 0; i < routingTable.size(); i++) {
            for (int j : orderedNodes) {     // Just safety for endless loop.
                pos++;
                if (pos == orderedNodes.length) {
                    pos = 0;
                }
                if (routingTable.keySet().contains(orderedNodes[pos])) {
                    clockwiseRoute[i] = orderedNodes[pos];
                    break;
                }
            }
        }
    }

    /**
     * Helper function to return the index of a given int within an int array.
     */
    private int indexOf(int value, int[] list) {
        int index = -1;

        for (int i = 0; (i < list.length) && (index == -1); i++) {
            if (list[i] == value) {
                index = i;
            }
        }
        return index;
    }
}
