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
public class OverlayNodeSendsRegistration implements Event {

    public byte[] IPAddress;
    public int Port;
    
    private int type;
    
    public OverlayNodeSendsRegistration(){
        type = MessageType.OVERLAY_NODE_SENDS_REGISTRATION.GetIntValue();
    }
    
    public OverlayNodeSendsRegistration (byte[] marshalledBytes) throws IOException{
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = 
                new DataInputStream(new BufferedInputStream(baInputStream));
        
        type = din.readByte(); // redundant?
        
        int ipAddrLen = din.readByte();
        IPAddress = new byte[ipAddrLen];
        din.readFully(IPAddress);
        
        Port = din.readInt();
    }

    @Override
    public MessageType getType() {
        return MessageType.getMessageType(type);
    }

    @Override
    public byte[] getBytes()throws IOException{
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = 
                new DataOutputStream(new BufferedOutputStream(baOutputStream));
        
        dout.writeByte(type);
        dout.writeByte(IPAddress.length);
        dout.write(IPAddress);
        dout.writeInt(Port);
        
        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();
        
        baOutputStream.close();
        dout.close();
        return marshalledBytes;
    }
    
}
