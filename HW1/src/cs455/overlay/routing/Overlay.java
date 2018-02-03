package cs455.overlay.routing;

import java.util.*;

/**
 * Contains functions for Registry to set up the overlay as well as track node state.
 */
public class Overlay {
    private Map<Integer, RoutingEntry> nodes;
    private int RoutingTableSize;
    private Map<Integer, List<RoutingEntry>> routes = new HashMap<>();
    private int[] nodeList;

    /**
     * Constructor
     *
     * @param registeredNodes  Map of routing entries, node ID as key.
     * @param routingTableSize Size of desired routing table size.
     */
    public Overlay(Map<Integer, RoutingEntry> registeredNodes, int routingTableSize) {
        nodes = registeredNodes;
        RoutingTableSize = routingTableSize;
        setupOverlay();
    }

    /**
     * Creates the overlay with the specified routing table size.
     * Creates both the ordered list of node ID's, as well as a list of routing
     * table entries for each node.
     */
    private void setupOverlay() {
        nodeList = nodes.keySet().stream().mapToInt(Number::intValue).toArray();
        List<RoutingEntry> currentRoute;
        Arrays.sort(nodeList);
        for (int i = 0; i < nodeList.length; i++) {
            routes.put(nodeList[i], new ArrayList<>());  // New list for each node.
            for (int j = 1; j <= RoutingTableSize; j++) {
                int hops = i + (int) Math.pow(2, j - 1);  // Calculate hops.
                int newDest = hops >= nodeList.length ? hops - nodeList.length : hops;  // Loop around.
                currentRoute = routes.get(nodeList[i]);                 // Get existing list,
                currentRoute.add(nodes.get(nodeList[newDest]));         // Add new node
                routes.put(nodeList[i], currentRoute);                  // Update map.
            }
        }
    }

    /**
     * Returns the list of routing entries that have been calculated for a node.
     */
    public List<RoutingEntry> getRoutingTable(int ID) {
        return routes.get(ID);
    }

    /**
     * Gets the list of all registered NodeID's in ascending order.
     */
    public int[] getOrderedNodeList() {
        return nodeList;
    }

    /**
     * Updates node as ready, returns true if all nodes are now ready.
     */
    public synchronized boolean connectionsComplete(int ID) {
        nodes.get(ID).OverlayConnectionsMade = true;

        for (RoutingEntry node : nodes.values()) {
            if (!node.OverlayConnectionsMade) return false;
        }
        return true;
    }

    /**
     * Updates node as task complete.  Returns true if all nodes complete.
     */
    public synchronized boolean taskFinished(int ID) {
        nodes.get(ID).TaskFinished = true;

        for (RoutingEntry node : nodes.values()) {
            if (!node.TaskFinished) return false;
        }

        // If we got this far, all nodes are finished.
        // Reset flags for next run before returning true.
        for (RoutingEntry node : nodes.values()) {
            node.TaskFinished = false;
        }
        return true;
    }

    /**
     * Updates node as summary is received.  Returns true if all nodes have reported
     * their summaries.
     */
    public synchronized boolean summaryReceived(int ID) {
        nodes.get(ID).ReportReceived = true;

        for (RoutingEntry node : nodes.values()) {
            if (!node.ReportReceived) return false;
        }

        // If we got this far, all nodes are finished.
        // Reset flags for next run before returning true.
        for (RoutingEntry node : nodes.values()) {
            node.ReportReceived = false;
        }
        return true;
    }
}
