package cs455.overlay.routing;

import java.util.*;

public class Overlay {
    private Map<Integer, RoutingEntry> nodes;
    private int RoutingTableSize;
    private Map<Integer, List<RoutingEntry>> routes = new HashMap<>();
    private List<NodeConnection> nodeConnections = new ArrayList<>();

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
                nodeConnections.add(new NodeConnection(i, newDest));
            }
        }
    }

    public List<RoutingEntry> getRoutingTable(int ID) {
        return routes.get(ID);
    }

    public boolean connectionsComplete() {
        for (NodeConnection b : nodeConnections) {
            if (!b.established) return false;
        }
        return true;
    }

    public void connectionMade(int src, int dst) {
        NodeConnection newConnection = new NodeConnection(src, dst);
        for (NodeConnection nc : nodeConnections) {
            if (nc.equals(newConnection)) nc.established = true;
        }
    }

    class NodeConnection {
        public int src;
        public int dst;
        public boolean established;

        public NodeConnection(int src, int dst) {
            this.src = src;
            this.dst = dst;
            established = false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NodeConnection that = (NodeConnection) o;
            return src == that.src &&
                    dst == that.dst;
        }

        @Override
        public int hashCode() {

            return Objects.hash(src, dst);
        }
    }
}
