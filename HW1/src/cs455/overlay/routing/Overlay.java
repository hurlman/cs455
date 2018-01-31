package cs455.overlay.routing;

import java.util.*;

public class Overlay {
    private Map<Integer, RoutingEntry> nodes;
    private int RoutingTableSize;
    private Map<Integer, List<RoutingEntry>> routes = new HashMap<>();
    private int[] nodeList;

    public Overlay(Map<Integer, RoutingEntry> registeredNodes, int routingTableSize) {
        nodes = registeredNodes;
        RoutingTableSize = routingTableSize;
        setupOverlay();
    }

    private void setupOverlay() {
        nodeList = nodes.keySet().stream().mapToInt(Number::intValue).toArray();
        List<RoutingEntry> currentRoute;
        Arrays.sort(nodeList);
        for (int i = 0; i < nodeList.length; i++) {
            routes.put(nodeList[i], new ArrayList<>());
            for (int j = 1; j <= RoutingTableSize; j++) {
                int hops = i + (int) Math.pow(2, j - 1);
                int newDest = hops >= nodeList.length ? hops - nodeList.length : hops;
                currentRoute = routes.get(nodeList[i]);
                currentRoute.add(nodes.get(nodeList[newDest]));
                routes.put(nodeList[i], currentRoute);
            }
        }
    }

    public List<RoutingEntry> getRoutingTable(int ID) {
        return routes.get(ID);
    }

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
}
