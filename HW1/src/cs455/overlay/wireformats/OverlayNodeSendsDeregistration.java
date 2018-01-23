/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.overlay.wireformats;

import cs455.overlay.wireformats.Protocol.*;
import java.io.*;

/**
 *
 * @author Mike
 */
public class OverlayNodeSendsDeregistration implements Event {

    public byte[] IPAddress;
    public int Port;

    private int type;

    public OverlayNodeSendsDeregistration() {
    }

    public OverlayNodeSendsDeregistration(byte[] marshalledBytes) throws IOException{

    }

    @Override
    public Protocol.MessageType getType() {
        return MessageType.getMessageType(type);
    }

    @Override
    public byte[] getBytes() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
