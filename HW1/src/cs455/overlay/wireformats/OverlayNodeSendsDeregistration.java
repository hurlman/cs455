/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.overlay.wireformats;

import cs455.overlay.wireformats.Protocol.*;

import java.io.*;

/**
 * @author Mike
 */
public class OverlayNodeSendsDeregistration implements Event {

    public byte[] IPAddress;
    public int Port;
    public int NodeID;

    private int type;

    public OverlayNodeSendsDeregistration() {
        type = MessageType.OVERLAY_NODE_SENDS_DEREGISTRATION.GetIntValue();
    }

    public OverlayNodeSendsDeregistration(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din =
                new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readByte();

        int ipAddrLen = din.readByte();
        IPAddress = new byte[ipAddrLen];
        din.readFully(IPAddress);

        Port = din.readInt();
        NodeID = din.readInt();
    }

    @Override
    public Protocol.MessageType getType() {
        return MessageType.getMessageType(type);
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout =
                new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeByte(type);
        dout.writeByte(IPAddress.length);
        dout.write(IPAddress);
        dout.writeInt(Port);
        dout.writeInt(NodeID);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshalledBytes;
    }

}
