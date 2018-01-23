/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.overlay.node;

import java.net.*;
import java.io.*;
import java.util.*;

import cs455.overlay.transport.TCPConnection;
import cs455.overlay.wireformats.*;
import cs455.overlay.routing.*;


/**
 * @author hurleym
 */
public class Registry implements Node {

    private int Port;
    private int ID = 0;
    private Map<Integer, RoutingEntry> RegisteredNodes;
    private Scanner keyboard;
    private ServerSocket serverSocket;
    private EventFactory EF;
    private List<TCPConnection> TCPConnectionCache = new ArrayList<>();

    public static void main(String[] args) {
        new Registry().doMain(args);
    }

    public void doMain(String[] args) {
        try {
            Port = Integer.parseInt(args[0]);
            System.out.println("Starting registry.");

            serverSocket = new ServerSocket(Port);

            System.out.println(String.format("Listening at %s on port %s.", InetAddress.getLocalHost(), Port));
            System.out.println("Awaiting messaging node registration.");
        } catch (IOException e) {
            System.out.println(String.format("Unable to open server socket on port %s. %s", Port, e.getMessage()));
            System.exit(0);
        } catch (NumberFormatException e) {
            System.out.println(String.format("%s is not a valid integer.", args[0]));
            System.exit(0);
        }

        EF = EventFactory.getInstance();
        EF.subscribe(this);

        new Thread(this::AcceptConnections).start();

        System.out.println("'list-messaging-nodes' or 'l'");
        System.out.println("'setup-overlay #' or 's #'  (e.g. s 3)");
        keyboard = new Scanner(System.in);
        while (true) {
            String input = keyboard.nextLine();
            if (input.equals("l") || input.equals("list-messaging-nodes")) {
                ListNodes();
            } else if (input.startsWith("s ") || input.startsWith("setup-overlay ")) {
                int routingTableSize;
                try {
                    routingTableSize = Integer.parseInt(input.substring(input.lastIndexOf(" ") + 1));
                } catch (NumberFormatException e) {
                    System.out.println(String.format("%s is not a valid integer.", input.substring(input.lastIndexOf(" ") + 1)));
                    continue;
                }
                // Stop registering nodes and create the overlay.
                SetupOverlay(routingTableSize);
                break;
            } else {
                System.out.println(String.format("Unknown command: %s", input));
            }
        }

        // Overlay is now setup.  Begin taking new commands.
    }

    private void AcceptConnections() {
        try {
            while (true) {
                Socket newSocket = serverSocket.accept();
                TCPConnectionCache.add(new TCPConnection(newSocket));
                System.out.println("Client has connected.");
            }
        } catch (IOException e) {
            System.out.println(String.format("Error on client connection. %s.", e.getMessage()));
        }
    }

    private int GetID() {
        ID++;
        return ID;
    }

    private void ListNodes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void SetupOverlay(int routingTableSize) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onEvent(Event message, InetAddress origin) {
        switch (message.getType()) {
            case OVERLAY_NODE_SENDS_REGISTRATION:
                RegisterNode((OverlayNodeSendsRegistration) message, origin);
                break;
            case REGISTRY_REPORTS_REGISTRATION_STATUS:
                break;
            case OVERLAY_NODE_SENDS_DEREGISTRATION:
                DeregisterNode((OverlayNodeSendsDeregistration) message, origin);
            case REGISTRY_REPORTS_DEREGISTRATION_STATUS:
                break;
            case REGISTRY_SENDS_NODE_MANIFEST:
                break;
            case NODE_REPORTS_OVERLAY_SETUP_STATUS:
                break;
            case REGISTRY_REQUESTS_TASK_INITIATE:
                break;
            case OVERLAY_NODE_SENDS_DATA:
                break;
            case OVERLAY_NODE_REPORTS_TASK_FINISHED:
                break;
            case REGISTRY_REQUESTS_TRAFFIC_SUMMARY:
                break;
            case OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY:
                break;
        }

    }

    private void DeregisterNode(OverlayNodeSendsDeregistration dereg, InetAddress origin) {

    }

    private void RegisterNode(OverlayNodeSendsRegistration reg, InetAddress origin) {
        System.out.println(String.format("Registration from %s.  Listening on port %s", origin.getHostAddress(), reg.Port));
    }
}
