/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.overlay.node;

import java.net.*;
import java.io.*;
import cs455.overlay.*;
/**
 *
 * @author hurleym
 */
public class Registry {

    private static int Port;

    public static void main(String[] args) {
        try {
            Port = Integer.parseInt(args[0]);
            System.out.println(String.format("Registry started.  Listening at %s on port %s.", InetAddress.getLocalHost(), Port));

            StartListener();
        } catch (NumberFormatException e) {
            System.out.println(String.format("%s is not a valid integer.", args[0]));
            System.exit(0);
        } catch (IllegalArgumentException e) {
            System.out.println(String.format("Port %s out of range.  Must be between 0 and 65535.", Port));
            System.exit(0);
        } catch (Exception e) {
            System.out.println(String.format("Unable to open socket on port %s.", Port));
            System.exit(0);
        }
    }

    private static void StartListener() throws Exception {
        System.out.println("Awaiting messaging node registration.  Press 'L' to list registered nodes.");
        System.out.println("Press 'L' to list registered nodes.");
        System.out.println("Press 'S' to stop accepting new nodes.");

        ServerSocket listener = new ServerSocket(Port);

        while (true) {
            Socket nodeSocket = listener.accept();
            new Thread(() -> HandleConnection(nodeSocket)).start();
        }
    }

    private static void HandleConnection(Socket messageNodeSocket) {
        try {
            Thread currentThread = Thread.currentThread();
            DataInputStream inFromNode = new DataInputStream(messageNodeSocket.getInputStream());
            DataOutputStream outToNode = new DataOutputStream(messageNodeSocket.getOutputStream());

            byte[] buffer = new byte[1024];
            int bytesread;
            while (true) {
                bytesread = inFromNode.read(buffer);

                byte[] out = String.format("%s bytes read. Thread %s", bytesread, currentThread.getId()).getBytes();
                outToNode.write(out);
            }

        } catch (IOException e) {
            System.out.println(String.format("An error occurred getting stream. %s", e.getMessage()));
        }

    }
}
