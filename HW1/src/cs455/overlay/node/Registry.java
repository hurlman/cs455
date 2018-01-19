/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.overlay.node;

import java.net.*;
import java.io.*;
import cs455.overlay.*;
import cs455.overlay.wireformats.*;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author hurleym
 */
public class Registry {

    private static int Port;
    private static int ID = 0;
    private static Map<Integer, RegisteredNode> RegisteredNodes;

    public static void main(String[] args) {
        try {
            Port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println(String.format("%s is not a valid integer.", args[0]));
            System.exit(0);
        }
        System.out.println("Starting registry.");
        // Register nodes in separate thread to keep console alive.
        Thread registerThread = new Thread(() -> RegisterNodes());
        registerThread.start();

        try {
            // Short wait for listener to start before displaying console instructions.
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println("'list-messaging-nodes' or 'l'");
        System.out.println("'setup-overlay #' or 's #'  (e.g. s 3)");
        Scanner keyboard = new Scanner(System.in);
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
                registerThread.interrupt();
                SetupOverlay(routingTableSize);
                break;
            } else {
                System.out.println(String.format("Unknown command: %s", input));
            }
        }
        
        // Overlay is now setup.  Begin taking new commands.
    }

    private static void RegisterNodes() {
        try {
            ServerSocket listener = new ServerSocket(Port);
            System.out.println(String.format("Listening at %s on port %s.", InetAddress.getLocalHost(), Port));
            System.out.println("Awaiting messaging node registration.");

            while (!Thread.currentThread().isInterrupted()) {
                Socket nodeSocket = listener.accept();
                new Thread(() -> HandleNodeRegistration(nodeSocket)).start();
            }
        } catch (IOException e) {
            System.out.println(String.format("Unable to open server socket on port %s. %s", Port, e.getMessage()));
            System.exit(0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        System.out.println("Node registration complete.");
    }

    private static void HandleNodeRegistration(Socket messageNodeSocket) {
        try {
            System.out.println("Client has connected.");
            Thread currentThread = Thread.currentThread();
            DataInputStream inFromNode = new DataInputStream(messageNodeSocket.getInputStream());
            DataOutputStream outToNode = new DataOutputStream(messageNodeSocket.getOutputStream());

            byte[] buffer = new byte[Constants.BUFFER_SIZE];
            int bytesread;
            while (true) {
                bytesread = inFromNode.read(buffer);
                Constants.MessageType msgType = Constants.MessageType.GetMessageType(buffer[0]);
                switch(msgType){
                    case OVERLAY_NODE_SENDS_REGISTRATION:
                        OverlayNodeSendsRegistration nodeReg = new OverlayNodeSendsRegistration(buffer);
                        System.out.println(String.format("IP: %s, Port %s", nodeReg.IPAddress, nodeReg.Port));
                        break;
                    case OVERLAY_NODE_SENDS_DEREGISTRATION:
                        OverlayNodeSendsDeregistration nodeDereg = new OverlayNodeSendsDeregistration(buffer);
                        break;
                    default:
                        System.out.println("Invalid message type received.");
                }
                
                
            }

        } catch (IOException e) {
            System.out.println(String.format("Client has disconnected."));
        }

    }
    
    private static int GetID(){
        ID++;
        return ID;
    }

    private static void ListNodes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static void SetupOverlay(int routingTableSize) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
