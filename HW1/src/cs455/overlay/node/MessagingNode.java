/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.overlay.node;

import cs455.overlay.transport.TCPConnectionCache;
import cs455.overlay.util.InteractiveCommandParser;
import cs455.overlay.wireformats.*;

import java.net.*;
import java.io.*;
import java.util.*;


/**
 * @author hurleym
 */
public class MessagingNode implements Node {

    private int RegistryPort;
    private InetAddress RegistryIP;
    private Scanner keyboard;
    private ServerSocket serverSocket;
    private int Port;
    private TCPConnectionCache tcpCache;

    public static void main(String[] args) {
        new MessagingNode().doMain(args);
    }

    private void doMain(String[] args) {
        System.out.println("Messaging Node.");
        if (args.length != 2) {
            System.out.println("Invalid number of arguments.  Must contain <RegistryIP> <RegistryPort>.");
        }
        try {
            RegistryPort = Integer.parseInt(args[1]);
            RegistryIP = InetAddress.getByName(args[0]);

            serverSocket = new ServerSocket(0);
            Port = serverSocket.getLocalPort();

            System.out.println(String.format("Server socket open: %s:%s", InetAddress.getLocalHost(), Port));

            tcpCache = new TCPConnectionCache(serverSocket);

            OverlayNodeSendsRegistration myReg = new OverlayNodeSendsRegistration();
            myReg.IPAddress = InetAddress.getLocalHost().getAddress();
            myReg.Port = Port;
            Socket clientSocket = new Socket(RegistryIP, RegistryPort);

            tcpCache.setRegistryConnection(clientSocket);
            tcpCache.sendToRegistry(myReg.getBytes());
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
    public void onEvent(Event message, InetAddress origin) {
        switch (message.getType()) {
            case OVERLAY_NODE_SENDS_REGISTRATION:
                break;
            case REGISTRY_REPORTS_REGISTRATION_STATUS:
                break;
            case OVERLAY_NODE_SENDS_DEREGISTRATION:
                break;
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

    @Override
    public void onCommand(InteractiveCommandParser.Command command, int arg) {

    }
}
