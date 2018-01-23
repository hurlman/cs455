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
import cs455.overlay.transport.TCPConnectionCache;
import cs455.overlay.util.InteractiveCommandParser;
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
    private TCPConnectionCache tcpCache;

    public static void main(String[] args) {
        new Registry().doMain(args);
    }

    private void doMain(String[] args) {
        try {
            Port = Integer.parseInt(args[0]);
            System.out.println("Starting registry.");

            serverSocket = new ServerSocket(Port);
            tcpCache = new TCPConnectionCache(serverSocket);

            EF = EventFactory.getInstance();
            EF.subscribe(this);

            new InteractiveCommandParser(this).start();

            System.out.println(String.format("Listening at %s on port %s.", InetAddress.getLocalHost(), Port));
            System.out.println("Awaiting messaging node registration.");
        } catch (IOException e) {
            System.out.println(String.format("Unable to open server socket on port %s. %s", Port, e.getMessage()));
            System.exit(0);
        } catch (NumberFormatException e) {
            System.out.println(String.format("%s is not a valid integer.", args[0]));
            System.exit(0);
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
            default:
                break;
        }
    }

    private void DeregisterNode(OverlayNodeSendsDeregistration dereg, InetAddress origin) {

    }

    private void RegisterNode(OverlayNodeSendsRegistration reg, InetAddress origin) {
        System.out.println(String.format("Registration from %s.  Listening on port %s", origin.getHostAddress(), reg.Port));
    }
}
