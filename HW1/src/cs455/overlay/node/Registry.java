/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.overlay.node;

import java.net.*;
import java.io.*;
import java.util.*;

import cs455.overlay.transport.*;
import cs455.overlay.util.InteractiveCommandParser;
import cs455.overlay.wireformats.*;
import cs455.overlay.routing.*;


/**
 * @author hurleym
 */
public class Registry implements Node {

    private int Port;
    private int ID = 0;
    private Map<Integer, RoutingEntry> RegisteredNodes = new HashMap<>();
    private ServerSocket serverSocket;
    private TCPConnectionCache tcpCache;
    private cs455.overlay.routing.Overlay overlay;

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
            serverSocket = new ServerSocket(Port);
            tcpCache = new TCPConnectionCache(serverSocket);
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

    private int GetID() {
        if (ID > 127) {
            System.out.println("Too many nodes have registered. Max is 128. Exiting.");
            System.exit(0);
        }
        return ID++;
    }

    private void ListNodes() {
        System.out.println("Currently registered nodes:");
        for (Map.Entry<Integer, RoutingEntry> re : RegisteredNodes.entrySet()) {
            System.out.println(String.format("ID: %s, IPAddr: %s, Port %s",
                    re.getKey(),
                    re.getValue().tcpConnection.getRemoteIP().getHostAddress(),
                    re.getValue().Port));
        }
    }

    private void ListRoutes(){
        System.out.print("Routing Tables:\n\n");
        for (Map.Entry<Integer, RoutingEntry> re : RegisteredNodes.entrySet()) {
            System.out.println(String.format("ID: %s, IPAddr: %s, Port %s",
                    re.getKey(),
                    re.getValue().tcpConnection.getRemoteIP().getHostAddress(),
                    re.getValue().Port));
            System.out.print("Table: ");
            for(RoutingEntry e : overlay.getRoutingTable(re.getKey())){
                System.out.print(e.ID + ", ");
            }
            System.out.print("\b\b \n\n\n");
        }
    }

    private void SetupOverlay(int routingTableSize) {
        if (routingTableSize < 0) {
            System.out.println("Routing table size must be specified.");
        } else if (routingTableSize < 2 || routingTableSize > 4) {
            System.out.println(routingTableSize + " is an invalid routing table size.");
        } else {
            System.out.println("Registration ended. Setting up overlay. RT size: " + routingTableSize);

            overlay = new Overlay(RegisteredNodes, routingTableSize);

        }
    }

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
                break;
            case OVERLAY_NODE_REPORTS_TASK_FINISHED:
                break;
            case OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY:
                break;
            default:
                // Do nothing.
                break;
        }
    }

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
            default:
                System.out.println("Invalid command for Registry.");
                break;
        }
    }

    private void DeregisterNode(OverlayNodeSendsDeregistration dereg, TCPConnection origin) {

        // Ensure source IP matches message IP.
        if (!Arrays.equals(dereg.IPAddress, origin.getRemoteIP().getAddress())) {
            SendDeregistrationStatus(origin, "IP does not match source.", -1);
            System.out.println("ERROR. Registration attempt failed. Message IP does not match source.");
        }

        // Ensure node is registered.
        if(RegisteredNodes.containsKey(dereg.NodeID)){
            RegisteredNodes.remove(dereg.NodeID);
            String message = String.format("Deregistration request successful.  The number of messaging " +
                    "nodes currently constituting the overlay is (%s).", RegisteredNodes.size());
            SendDeregistrationStatus(origin, message, dereg.NodeID);
            System.out.println(String.format("Node deregistered. ID: %s, IPAddr: %s, Port: %s",
                    dereg.NodeID, origin.getRemoteIP().getHostAddress(), dereg.Port));
        }else{
            String message = "Deregistration failed. Node was not registered.";
            SendDeregistrationStatus(origin, message, -1);
            System.out.println(String.format("ERROR. Node deregistration failed. ID %s was not in the " +
                            "registry.", dereg.NodeID));
        }

    }


    private void RegisterNode(OverlayNodeSendsRegistration reg, TCPConnection origin) {

        // Ensure source IP matches message IP.
        if (!Arrays.equals(reg.IPAddress, origin.getRemoteIP().getAddress())) {
            SendRegistrationStatus(origin, "IP does not match source.", -1);
            System.out.println("ERROR. Registration attempt failed. Message IP does not match source.");
        }

        // Ensure node has not already registered. Otherwise register.
        RoutingEntry re = new RoutingEntry(origin, reg.IPAddress, reg.Port);
        if (RegisteredNodes.containsValue(re)) {
            String message = "Registration failed.  Node has previsouly registered.";
            SendRegistrationStatus(origin, "Node has previously registered.", -1);
            System.out.println(String.format("ERROR. Node is attempting to reregister. IPAddr: %s, Port: %s",
                    origin.getRemoteIP().getHostAddress(), reg.Port));
        } else {
            int id = GetID();
            re.ID = id;
            RegisteredNodes.put(id, re);
            String message = String.format("Registration request successful. The number of " +
                    "messaging nodes currently constituting the overlay is (%s).", RegisteredNodes.size());
            SendRegistrationStatus(origin, message, id);
            System.out.println(String.format("Node registered. ID: %s, IPAddr: %s, Port: %s",
                    id, origin.getRemoteIP().getHostAddress(), reg.Port));
        }

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
        try{
            dest.sendData(deregStatus.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
