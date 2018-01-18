/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.overlay.node;

import java.net.Socket;
import java.util.List;

/**
 *
 * @author Mike
 */
public class RegisteredNode {
    public Socket Socket;
    public byte[] IPAddress;
    public int Port;
    public List<Integer> RoutingTable;
}
