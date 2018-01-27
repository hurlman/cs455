package cs455.overlay.routing;

import java.util.*;

public class Overlay {
    private Map<Integer, RoutingEntry> nodes;
    private int RoutingTableSize;
    private Map<Integer, List<RoutingEntry>> routes = new HashMap<>();

    public Overlay(Map<Integer, RoutingEntry> registeredNodes, int routingTableSize) {
        nodes = registeredNodes;
        RoutingTableSize = routingTableSize;
        setupOverlay();
    }

    private void setupOverlay() {
        int[] nodeList = nodes.keySet().stream().mapToInt(Number::intValue).toArray();
        Arrays.sort(nodeList);
        for (int i = 0; i < nodeList.length; i++) {
            routes.put(nodeList[i], new ArrayList<>());
            for (int j = 1; j <= RoutingTableSize; j++) {
                int hops = i + (int) Math.pow(2, j - 1);
                int newDest = hops >= nodeList.length ? hops - nodeList.length : hops;
                List<RoutingEntry> currentRoute = routes.get(nodeList[i]);
                currentRoute.add(nodes.get(nodeList[newDest]));
                routes.put(nodeList[i], currentRoute);
            }
        }
    }

    public List<RoutingEntry> getRoutingTable(int ID) {
        return routes.get(ID);
    }

    public boolean connectionsComplete() {
        for (RoutingEntry node : nodes.values()) {
            if (!node.OverlayConnectionsMade) return false;
        }
        return true;
    }
}
