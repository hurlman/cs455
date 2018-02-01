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
import cs455.overlay.util.StatisticsCollectorAndDisplay;
import cs455.overlay.wireformats.*;

import java.net.*;
import java.io.*;
import java.util.concurrent.ThreadLocalRandom;


/**
 * @author hurleym
 */
public class MessagingNode implements Node {

    private int Port;
    private byte[] IPAddr;
    private TCPConnectionCache tcpCache;
    private int ID;
    private RoutingTable routingTable;
    private StatisticsCollectorAndDisplay stats = new StatisticsCollectorAndDisplay();

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
            int registryPort = Integer.parseInt(args[1]);
            InetAddress registryIP = InetAddress.getByName(args[0]);

            // Subscribe to event factory.
            EventFactory.getInstance().subscribe(this);


            // Set up connection handler.
            ServerSocket serverSocket = new ServerSocket(0);
            Port = serverSocket.getLocalPort();
            tcpCache = new TCPConnectionCache(serverSocket);
            System.out.println(String.format("Server socket open: %s:%s",
                    InetAddress.getLocalHost(), Port));

            // Register immediately.
            OverlayNodeSendsRegistration myReg = new OverlayNodeSendsRegistration();
            IPAddr = InetAddress.getLocalHost().getAddress();
            myReg.IPAddress = IPAddr;
            myReg.Port = Port;
            Socket clientSocket = new Socket(registryIP, registryPort);
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
                HandleRegistrationStatus((RegistryReportsRegistrationStatus) message);
                break;
            case REGISTRY_REPORTS_DEREGISTRATION_STATUS:
                HandleDeregistrationStatus((RegistryReportsDeregistrationStatus) message);
                break;
            case REGISTRY_SENDS_NODE_MANIFEST:
                CreateRoutingTable((RegistrySendsNodeManifest) message);
                break;
            case REGISTRY_REQUESTS_TASK_INITIATE:
                GenerateNewData((RegistryRequestsTaskInitiate) message);
                break;
            case OVERLAY_NODE_SENDS_DATA:
                ReceivedData((OverlayNodeSendsData) message);
                break;
            case REGISTRY_REQUESTS_TRAFFIC_SUMMARY:
                ReportTrafficSummary();
                break;
            default:
                // Do nothing.
                break;
        }
    }

    /**
     * Received data from another node.  Add to totals or relay
     */
    private void ReceivedData(OverlayNodeSendsData message) {
//  System.out.println(String.format("Got something. Src: %s, Dst: %s, Payload: %s, Hops: %s",
//      message.SourceID, message.DestinationID, message.Payload, message.DisseminationTrace.size()));

        if (message.DestinationID == ID) {
            // We are the sink.
            stats.dataReceived(message.Payload);
        } else {
            // Relay
            message.DisseminationTrace.add(ID);
            stats.dataRelayed();
            SendDataMessage(message);
        }
    }

    /**
     * Generates and sends off packets. This will run on the main thread.
     * Sends task complete after loop has finished.
     */
    private void GenerateNewData(RegistryRequestsTaskInitiate message) {
        System.out.println(String.format("Beginning sending %s messages.", message.NumberOfPackets));

        OverlayNodeSendsData newDataMsg = new OverlayNodeSendsData();
        for (int i = 0; i < message.NumberOfPackets; i++) {

            int payload = ThreadLocalRandom.current().nextInt();
            newDataMsg.DestinationID = routingTable.getRandomDest();
            newDataMsg.SourceID = ID;
            newDataMsg.Payload = payload;
            newDataMsg.DisseminationTrace.clear();

            SendDataMessage(newDataMsg);
            stats.dataSent(payload);

            if ((i + 1) % 1000 == 0) {
                System.out.println((i + 1) + " messages sent.");
            }
        }

        OverlayNodeReportsTaskFinished completeMessage = new OverlayNodeReportsTaskFinished();
        completeMessage.NodeID = ID;
        completeMessage.Port = Port;
        completeMessage.IPAddress = IPAddr;
        try {
            tcpCache.sendToRegistry(completeMessage.getBytes());
        } catch (IOException e) {
            System.out.println("Error informing registry task complete.");
            e.printStackTrace();
        }
    }

    /**
     * Forwards a message towards its destination according to routing table.
     */
    private void SendDataMessage(OverlayNodeSendsData dataToSend) {
        try {
            routingTable.getDest(dataToSend.DestinationID).sendData(dataToSend.getBytes());
        } catch (IOException e) {
            System.out.println("Error sending message. " + e.getMessage());
            System.out.print(String.format("Src: %s, Dst: %s, Hops: ",
                    dataToSend.SourceID, dataToSend.DestinationID));
            for (int hop : dataToSend.DisseminationTrace) {
                System.out.print(hop + ", ");
            }
            System.out.println("\b\b  ");
        }
    }

    /**
     * Receives manifest from registry, initializes routing table, and establishes
     * connections to nodes.  Finally, notifies registry of completion of this task,
     * successful or otherwise.
     */
    private void CreateRoutingTable(RegistrySendsNodeManifest message) {

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

    /**
     * Just prints registry response of a deregistration request.
     */
    private void HandleDeregistrationStatus(RegistryReportsDeregistrationStatus message) {
        System.out.println(message.Message);
        //TODO Exit here??
    }

    /**
     * On registration response, sets ID if one is returned. Prints registry message.
     */
    private void HandleRegistrationStatus(RegistryReportsRegistrationStatus message) {
        if (message.SuccessStatus > -1) {
            ID = message.SuccessStatus;
            System.out.println(message.Message);
            System.out.println("My ID is " + ID);
        } else {
            System.out.println(message.Message);
            //TODO Exit here??
        }
    }

    /**
     * Handler for incoming keyboard commands.
     */
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

    /**
     * Prints counters, sends to registry, resets counters.
     */
    private void ReportTrafficSummary() {

        try {
            OverlayNodeReportsTrafficSummary summaryMsg = new OverlayNodeReportsTrafficSummary();
            summaryMsg.NodeID = ID;
            summaryMsg.Sent = stats.getSendTracker();
            summaryMsg.Relayed = stats.getRelayTracker();
            summaryMsg.SentSum = stats.getSendSummation();
            summaryMsg.Received = stats.getReceiveTracker();
            summaryMsg.ReceivedSum = stats.getReceiveSummation();

            tcpCache.sendToRegistry(summaryMsg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        stats.resetCounters();
    }

    /**
     * Prints counters to screen.
     */
    private void PrintDiags() {
        stats.NodeDisplay();
    }

    /**
     * Sends deregistration request to registry.
     */
    private void Deregister() {
        try {
            OverlayNodeSendsDeregistration myDereg = new OverlayNodeSendsDeregistration();
            myDereg.NodeID = ID;
            myDereg.Port = Port;
            myDereg.IPAddress = InetAddress.getLocalHost().getAddress();

            tcpCache.sendToRegistry(myDereg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
