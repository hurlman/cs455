/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.overlay.node;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import cs455.overlay.transport.*;
import cs455.overlay.util.*;
import cs455.overlay.wireformats.*;
import cs455.overlay.routing.*;

import static cs455.overlay.wireformats.Protocol.RELAY_WAIT;


/**
 * @author hurleym
 */
public class Registry implements Node {

    private int Port;
    private Set<Integer> NodeIDs = new HashSet<>();
    private Map<Integer, RoutingEntry> RegisteredNodes = new HashMap<>();
    private cs455.overlay.routing.Overlay overlay;
    private boolean ReadyToStart = false;
    private StatisticsCollectorAndDisplay stats = new StatisticsCollectorAndDisplay();

    public static void main(String[] args) {
        new Registry().doMain(args);
    }

    private void doMain(String[] args) {
        try {
            // Store command line input.
            Port = Integer.parseInt(args[0]);
            System.out.println("Starting registry.");

            // Subscribe to event factory.
            EventFactory.getInstance().subscribe(this);

            // Set up connection handler.
            new TCPConnectionCache(new ServerSocket(Port));
            System.out.println(String.format("Server socket open: %s:%s", InetAddress.getLocalHost(), Port));
            System.out.println("Awaiting messaging node registration.");

            // Begin accepting keyboard input.
            new InteractiveCommandParser(this).start();

        } catch (IOException e) {
            System.out.println(String.format("Unable to open server socket on port " +
                    "%s. %s", Port, e.getMessage()));
            System.exit(0);
        } catch (NumberFormatException e) {
            System.out.println(String.format("%s is not a valid integer.", args[0]));
            System.exit(0);
        }
    }

    /**
     * Generates unique ID between 0 and 127.  Returns false NodeIDs >= 128.
     */
    private synchronized int GetID() {
        if (NodeIDs.size() >= 128) {
            System.out.println("Too many nodes have registered. Max is 128.");
            return -1;
        }

        int newID;
        do {
            newID = ThreadLocalRandom.current().nextInt(0, 128);
        } while (!NodeIDs.add(newID));

        return newID;
    }

    private void ListNodes() {
        System.out.println("Currently registered nodes: " + RegisteredNodes.size());
        for (Map.Entry<Integer, RoutingEntry> re : RegisteredNodes.entrySet()) {
            System.out.println(String.format("ID: %s, IPAddr: %s, Port %s",
                    re.getKey(),
                    re.getValue().tcpConnection.getRemoteIP().getHostAddress(),
                    re.getValue().Port));
        }
    }

    private void ListRoutes() {
        System.out.print("Routing Tables:\n\n");
        for (Map.Entry<Integer, RoutingEntry> re : RegisteredNodes.entrySet()) {
            System.out.println(String.format("ID: %s, IPAddr: %s, Port %s",
                    re.getKey(),
                    re.getValue().tcpConnection.getRemoteIP().getHostAddress(),
                    re.getValue().Port));
            System.out.print("Table: ");
            for (RoutingEntry e : overlay.getRoutingTable(re.getKey())) {
                System.out.print(e.ID + ", ");
            }
            System.out.print("\b\b \n\n\n");
        }
    }

    /**
     * Creates overlay with specified routing table size. Ensures appropriate routing
     * table size for total number of nodes and that overlay has not already been set up.
     * Sends node manifest to each node.
     */
    private void SetupOverlay(int routingTableSize) {
        if (routingTableSize < 0) {
            System.out.println("Routing table size must be specified.");
        } else if (!(RegisteredNodes.size() > (2 * routingTableSize))) {
            System.out.println(String.format("%s is an invalid routing table size for %s nodes.",
                    routingTableSize, RegisteredNodes.size()));
        } else if (ReadyToStart) {
            System.out.println("Overlay already set up and nodes connected.");
        } else {
            System.out.println("Registration ended. Setting up overlay. RT size: " + routingTableSize);
            overlay = new Overlay(RegisteredNodes, routingTableSize);

            System.out.println("Overlay setup complete.  Sending node manifests.");
            for (Map.Entry<Integer, RoutingEntry> re : RegisteredNodes.entrySet()) {
                SendNodeManifest(re.getValue(),
                        overlay.getRoutingTable(re.getKey()),
                        overlay.getOrderedNodeList());
            }
        }
    }

    /**
     * Sends manifest to a node containing that node's routing table and full node ID list.
     */
    private void SendNodeManifest(RoutingEntry re, List<RoutingEntry> routingTable, int[] orderedNodeList) {
        RegistrySendsNodeManifest manifestMessage = new RegistrySendsNodeManifest();
        manifestMessage.NodeRoutingTable = routingTable;
        manifestMessage.orderedNodeList = orderedNodeList;

        try {
            System.out.println(String.format("Sending manifest to node %s.", re.ID));
            re.tcpConnection.sendData(manifestMessage.getBytes());
        } catch (IOException e) {
            System.out.println(String.format("Error sending manifest to node %s. %s", re.ID, e.getMessage()));
        }
    }

    /**
     * Handles incoming messages. Initiates appropriate action based on message type.
     */
    @Override
    public void onEvent(Event message, TCPConnection origin) {
        switch (message.getType()) {
            case OVERLAY_NODE_SENDS_REGISTRATION:
                RegisterNode((OverlayNodeSendsRegistration) message, origin);
                break;
            case OVERLAY_NODE_SENDS_DEREGISTRATION:
                DeregisterNode((OverlayNodeSendsDeregistration) message, origin);
                break;
            case NODE_REPORTS_OVERLAY_SETUP_STATUS:
                TrackReadyNodes((NodeReportsOverlaySetupStatus) message);
                break;
            case OVERLAY_NODE_REPORTS_TASK_FINISHED:
                TrackFinishedNodes((OverlayNodeReportsTaskFinished) message, origin);
                break;
            case OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY:
                TrackReceivedSummaries((OverlayNodeReportsTrafficSummary) message);
                break;
            default:
                // Do nothing.
                break;
        }
    }

    /**
     * Handles keyboard input.  Initiates appropriate action.
     */
    @Override
    public void onCommand(InteractiveCommandParser.Command command, int arg) {
        switch (command) {
            case LIST_MESSAGING_NODES:
                ListNodes();
                break;
            case SETUP_OVERLAY:
                SetupOverlay(arg);
                break;
            case LIST_ROUTING_TABLES:
                ListRoutes();
                break;
            case START:
                TaskInitiate(arg);
                break;
            default:
                System.out.println("Invalid command for Registry.");
                break;
        }
    }

    private void TrackReadyNodes(NodeReportsOverlaySetupStatus message) {
        if (message.SuccessStatus > -1) {
            System.out.println(message.Message);
            if (overlay.connectionsComplete(message.SuccessStatus)) {
                ReadyToStart = true;
                System.out.println("All nodes are now ready to start sending messages.");
            }
        } else {
            System.out.println(message.Message);
            //TODO Exit here??
        }
    }

    private void TrackFinishedNodes(OverlayNodeReportsTaskFinished message, TCPConnection origin) {
        if (!Arrays.equals(message.IPAddress, origin.getRemoteIP().getAddress())) {
            System.out.println(String.format("ERROR. Task complete message from invalid source IP. Node: %s",
                    message.NodeID));
        } else {
            System.out.println(String.format("Node %s has reported task complete.", message.NodeID));
            if (overlay.taskFinished(message.NodeID)) {
                System.out.println("All nodes have reported task complete.");
                RequestTrafficSummary();
            }
        }
    }

    private void TrackReceivedSummaries(OverlayNodeReportsTrafficSummary message) {

    }

    private void RequestTrafficSummary() {
        //TODO maybe change this?
        try {
            System.out.println(String.format("Waiting %s second(s) for relays to finish.", RELAY_WAIT));
            Thread.sleep(RELAY_WAIT * 1000); // Wait for relays to complete

            for(Map.Entry<Integer, RoutingEntry> re : RegisteredNodes.entrySet()){
                re.getValue().tcpConnection.sendData(new RegistryRequestsTrafficSummary().getBytes());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error requesting traffic summary.");
            e.printStackTrace();
        }
    }

    private void TaskInitiate(int numberOfPackets) {
        if (ReadyToStart) {
            if (numberOfPackets > 0) {
                System.out.println("Sending TASK_INITIATE to all nodes!");
                RegistryRequestsTaskInitiate startMsg = new RegistryRequestsTaskInitiate();
                startMsg.NumberOfPackets = numberOfPackets;

                try {
                    for (Map.Entry<Integer, RoutingEntry> re : RegisteredNodes.entrySet()) {
                        re.getValue().tcpConnection.sendData(startMsg.getBytes());
                    }
                } catch (IOException e) {
                    System.out.println("Error sending task initiate.");
                    e.printStackTrace();
                }
            } else
                System.out.println("You must enter number of messages to send.");
        } else {
            System.out.println("All nodes have not yet reported that they are ready.");
        }
    }

    /**
     * Deregisters a node.  Ensures IP is valid and node was registered.
     */
    private synchronized void DeregisterNode(OverlayNodeSendsDeregistration dereg, TCPConnection origin) {

        String message;
        int id = -1;

        // Ensure source IP matches message IP.
        if (!Arrays.equals(dereg.IPAddress, origin.getRemoteIP().getAddress())) {
            message = "IP does not match source.";
            System.out.println("ERROR. Registration attempt failed. Message IP does not match source.");
        } else {

            // Ensure node is registered.
            if (RegisteredNodes.containsKey(dereg.NodeID)) {
                RegisteredNodes.remove(dereg.NodeID);
                NodeIDs.remove(dereg.NodeID); // Free up ID.
                message = String.format("Deregistration request successful.  The number of messaging " +
                        "nodes currently constituting the overlay is (%s).", RegisteredNodes.size());
                id = dereg.NodeID;
                System.out.println(String.format("Node deregistered. ID: %s, IPAddr: %s, Port: %s",
                        dereg.NodeID, origin.getRemoteIP().getHostAddress(), dereg.Port));
            } else {
                message = "Deregistration failed. Node was not registered.";
                System.out.println(String.format("ERROR. Node deregistration failed. ID %s was not in the " +
                        "registry.", dereg.NodeID));
            }
        }
        SendDeregistrationStatus(origin, message, id);

    }

    /**
     * Registers a node.  Checks to ensure node IP is valid and node has not already registered
     * from this IP and Port.  Checks to ensure no more than 128 nodes have registered.
     */
    private synchronized void RegisterNode(OverlayNodeSendsRegistration reg, TCPConnection origin) {

        String message;
        int id = -1;

        // Ensure source IP matches message IP.
        if (!Arrays.equals(reg.IPAddress, origin.getRemoteIP().getAddress())) {
            message = "IP does not match source.";
            System.out.println("ERROR. Registration attempt failed. Message IP does not match source.");
        } else {

            // Ensure node has not already registered. Otherwise register.
            RoutingEntry re = new RoutingEntry(origin, reg.IPAddress, reg.Port);
            if (RegisteredNodes.containsValue(re)) {
                message = "Node has previously registered.";
                System.out.println(String.format("ERROR. Node is attempting to reregister. IPAddr: %s, Port: %s",
                        origin.getRemoteIP().getHostAddress(), reg.Port));
            } else {
                id = GetID();
                if (id < 0) {
                    message = "Unable to get ID.  Perhaps too many nodes have registered.";
                } else {
                    re.ID = id;
                    RegisteredNodes.put(id, re);
                    message = String.format("Registration request successful. The number of " +
                            "messaging nodes currently constituting the overlay is (%s).", RegisteredNodes.size());
                    System.out.println(String.format("Node registered. ID: %s, IPAddr: %s, Port: %s",
                            id, origin.getRemoteIP().getHostAddress(), reg.Port));
                }
            }
        }
        SendRegistrationStatus(origin, message, id);
    }

    private void SendRegistrationStatus(TCPConnection dest, String message, int success) {
        RegistryReportsRegistrationStatus regStatus = new RegistryReportsRegistrationStatus();
        regStatus.SuccessStatus = success;
        regStatus.Message = message;
        try {
            dest.sendData(regStatus.getBytes());
        } catch (IOException e) {
            System.out.println("Unable to send registration reply. " + e.getMessage());
            if (RegisteredNodes.containsKey(success)) {
                System.out.println(String.format("Removing node %s from registry.", success));
                RegisteredNodes.remove(success);
            }
        }
    }

    private void SendDeregistrationStatus(TCPConnection dest, String message, int success) {
        RegistryReportsDeregistrationStatus deregStatus = new RegistryReportsDeregistrationStatus();
        deregStatus.SuccessStatus = success;
        deregStatus.Message = message;
        try {
            dest.sendData(deregStatus.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
