package cs455.overlay.wireformats;

import java.io.*;

public class OverlayNodeReportsTaskFinished implements Event {

    public byte[] IPAddress;
    public int Port;
    public int NodeID;

    private int type;

    public OverlayNodeReportsTaskFinished() {
        type = Protocol.MessageType.OVERLAY_NODE_REPORTS_TASK_FINISHED.GetIntValue();
    }

    public OverlayNodeReportsTaskFinished(byte[] marshalledBytes) throws IOException {
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
        return Protocol.MessageType.getMessageType(type);
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
