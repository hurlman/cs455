/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.overlay.node;

import cs455.overlay.routing.RoutingTable;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPConnectionCache;
import cs455.overlay.util.InteractiveCommandParser;
import cs455.overlay.wireformats.*;

import java.net.*;
import java.io.*;
import java.util.concurrent.ThreadLocalRandom;


/**
 * @author hurleym
 */
public class MessagingNode implements Node {

    private int RegistryPort;
    private InetAddress RegistryIP;
    private ServerSocket serverSocket;
    private int Port;
    private TCPConnectionCache tcpCache;
    private int ID;
    private RoutingTable routingTable;

    public static void main(String[] args) {
        new MessagingNode().doMain(args);
    }

    private void doMain(String[] args) {
        System.out.println("Messaging Node.");
        if (args.length != 2) {
            System.out.println("Invalid number of arguments.  Must contain <RegistryIP> <RegistryPort>.");
        }
        try {
            // Store command line input.
            RegistryPort = Integer.parseInt(args[1]);
            RegistryIP = InetAddress.getByName(args[0]);

            // Subscribe to event factory.
            EventFactory.getInstance().subscribe(this);

            // Set up connection handler.
            serverSocket = new ServerSocket(0);
            Port = serverSocket.getLocalPort();
            tcpCache = new TCPConnectionCache(serverSocket);
            System.out.println(String.format("Server socket open: %s:%s", InetAddress.getLocalHost(), Port));

            // Register immediately.
            OverlayNodeSendsRegistration myReg = new OverlayNodeSendsRegistration();
            myReg.IPAddress = InetAddress.getLocalHost().getAddress();
            myReg.Port = Port;
            Socket clientSocket = new Socket(RegistryIP, RegistryPort);
            tcpCache.setRegistryConnection(clientSocket);
            tcpCache.sendToRegistry(myReg.getBytes());

            // Begin accepting keyboard input.
            new InteractiveCommandParser(this).start();

        } catch (UnknownHostException e) {
            System.out.println("Unknown host. " + e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println(String.format("%s is not a valid integer.", args[1]));
            System.exit(0);
        }

        new InteractiveCommandParser(this).start();
    }


    @Override
    public void onEvent(Event message, TCPConnection origin) {
        switch (message.getType()) {
            case REGISTRY_REPORTS_REGISTRATION_STATUS:
                HandleRegistrationStatus((RegistryReportsRegistrationStatus) message, origin);
                break;
            case REGISTRY_REPORTS_DEREGISTRATION_STATUS:
                HandleDeregistrationStatus((RegistryReportsDeregistrationStatus) message, origin);
                break;
            case REGISTRY_SENDS_NODE_MANIFEST:
                CreateRoutingTable((RegistrySendsNodeManifest) message, origin);
                break;
            case REGISTRY_REQUESTS_TASK_INITIATE:
                GenerateNewData((RegistryRequestsTaskInitiate) message, origin);
                break;
            case OVERLAY_NODE_SENDS_DATA:
                //TODO Relay
                break;
            case REGISTRY_REQUESTS_TRAFFIC_SUMMARY:
                break;
            default:
                // Do nothing.
                break;
        }
    }

    /**
     * Generates and sends off packets. This will run on the main thread.
     */
    private void GenerateNewData(RegistryRequestsTaskInitiate message, TCPConnection origin) {
        System.out.println(String.format("Beginning sending %s messages.", message.NumberOfPackets));

        OverlayNodeSendsData newDataMsg = new OverlayNodeSendsData();
        for (int i = 0; i < message.NumberOfPackets; i++) {
            int sink = routingTable.getRandomDest();
            newDataMsg.DestinationID = sink;
            newDataMsg.SourceID = ID;
            newDataMsg.Payload = ThreadLocalRandom.current().nextInt();
            newDataMsg.DisseminationTrace.clear();

            TCPConnection nextDest = routingTable.getDest(sink);

            SendDataMessage(newDataMsg, nextDest);

            if (i % 1000 == 0) System.out.println(i + " messages sent.");
        }

        //TODO Send task complete
    }

    private void SendDataMessage(OverlayNodeSendsData dataToSend, TCPConnection nextDest) {
        try {
            nextDest.sendData(dataToSend.getBytes());
        } catch (IOException e) {
            System.out.println("Error sending message. " + e.getMessage());
            System.out.print(String.format("Src: %, Dst: %, Hops: ",
                    dataToSend.SourceID, dataToSend.DestinationID));
            for (int hop : dataToSend.DisseminationTrace) {
                System.out.print(hop + ", ");
            }
            System.out.println("\b\b  ");
        }
    }

    private void CreateRoutingTable(RegistrySendsNodeManifest message, TCPConnection origin) {

        routingTable = new RoutingTable(message.NodeRoutingTable, message.orderedNodeList, ID);
        int success = ID;
        String msg = "Overlay setup response from node " + ID + ". ";
        try {
            msg += routingTable.setupConnections();

        } catch (IOException e) {
            String err = "Error creating all routing table connections. " + e.getMessage();
            System.out.println(err);
            msg += err;
            success = -1;
        }

        NodeReportsOverlaySetupStatus statusMsg = new NodeReportsOverlaySetupStatus();
        statusMsg.Message = msg;
        statusMsg.SuccessStatus = success;
        try {
            tcpCache.sendToRegistry(statusMsg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void HandleDeregistrationStatus(RegistryReportsDeregistrationStatus message, TCPConnection origin) {
        System.out.println(message.Message);
        //TODO Exit here??
    }

    private void HandleRegistrationStatus(RegistryReportsRegistrationStatus message, TCPConnection origin) {
        if (message.SuccessStatus > -1) {
            ID = message.SuccessStatus;
            System.out.println(message.Message);
            System.out.println("My ID is " + ID);
        } else {
            System.out.println(message.Message);
            //TODO Exit here??
        }
    }

    @Override
    public void onCommand(InteractiveCommandParser.Command command, int arg) {
        switch (command) {
            case PRINT_COUNTERS_AND_DIAGNOSTICS:
                PrintDiags();
                break;
            case EXIT_OVERLAY:
                Deregister();
                break;
            default:
                System.out.println("Invalid command for Registry.");
                break;
        }
    }

    private void PrintDiags() {
        // Doing reregister for testing.
        try {
            OverlayNodeSendsRegistration myReg = new OverlayNodeSendsRegistration();
            myReg.IPAddress = InetAddress.getLocalHost().getAddress();
            myReg.Port = Port;

            tcpCache.sendToRegistry(myReg.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Deregister() {
        try {
            OverlayNodeSendsDeregistration myDereg = new OverlayNodeSendsDeregistration();
            myDereg.NodeID = ID;
            myDereg.Port = Port;
            myDereg.IPAddress = InetAddress.getLocalHost().getAddress();

            tcpCache.sendToRegistry(myDereg.getBytes());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
