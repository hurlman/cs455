package cs455.overlay.routing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutingTable {
    private Map<Integer, RoutingEntry> routingTable = new HashMap<>();
    private int[] orderedNodes;

    public RoutingTable(List<RoutingEntry> routingEntries, int[] orderedNodes){
        this.orderedNodes = orderedNodes;
        for(RoutingEntry re : routingEntries){
            routingTable.put(re.ID, re);
        }
    }
}
