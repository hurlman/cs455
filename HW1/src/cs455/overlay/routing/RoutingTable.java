package cs455.overlay.routing;

import cs455.overlay.transport.TCPConnection;

import java.net.*;
import java.util.*;
import java.io.*;

public class RoutingTable {
    private Map<Integer, RoutingEntry> routingTable = new HashMap<>();
    private int[] orderedNodes;

    public RoutingTable(List<RoutingEntry> routingEntries, int[] orderedNodes){
        this.orderedNodes = orderedNodes;
        for(RoutingEntry re : routingEntries){
            routingTable.put(re.ID, re);
        }
    }

    /** Creates TCP connections for all entries in the routing table. Run first. */
    public void setupConnections() throws IOException{
        for(Map.Entry<Integer, RoutingEntry> rme : routingTable.entrySet()){
            RoutingEntry re = rme.getValue();
            InetAddress nodeAddr = InetAddress.getByAddress(re.IPAddress);
            Socket clientSocket = new Socket(nodeAddr, re.ID);
            TCPConnection reConnection = new TCPConnection(clientSocket);
            re.tcpConnection = reConnection;

            System.out.println(String.format("Connection made to node %s.  %s:%s",
                    re.ID, nodeAddr.getHostAddress(), re.Port));
        }
    }

    public void Relay(int destination, long data){
        //TODO Stuff here.
    }
}
