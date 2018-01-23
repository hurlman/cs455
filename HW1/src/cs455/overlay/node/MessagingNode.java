/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.overlay.node;

import cs455.overlay.transport.TCPConnection;
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
        } catch (IOException e) {
            System.out.println(String.format("Unable to open server socket. %s", e.getMessage()));
            System.exit(0);
        } catch (NumberFormatException e) {
            System.out.println(String.format("%s is not a valid integer.", args[1]));
            System.exit(0);
        }

        try {
            System.out.println(String.format("Server socket open: %s:%s", InetAddress.getLocalHost(), Port));
            OverlayNodeSendsRegistration myReg = new OverlayNodeSendsRegistration();
            myReg.IPAddress = InetAddress.getLocalHost().getAddress();
            myReg.Port = Port;
            Socket clientSocket = new Socket(RegistryIP, RegistryPort);

            TCPConnection tcp = new TCPConnection(clientSocket);
            tcp.Sender.outQueue.add(myReg.getBytes());
        } catch (UnknownHostException e) {
            System.out.println("Unknown host. " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Unable to connect to registry. " + e.getMessage());
        }


    }

    @Override
    public void onEvent(Event message, InetAddress origin) {
        // TODO Auto-generated method stub

    }
}
