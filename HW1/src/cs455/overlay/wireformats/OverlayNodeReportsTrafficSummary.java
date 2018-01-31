package cs455.overlay.wireformats;

import java.io.*;

public class OverlayNodeReportsTrafficSummary implements Event{

    public int NodeID;
    public int Sent;
    public int Relayed;
    public long SentSum;
    public int Received;
    public long ReceivedSum;

    private int type;

    public OverlayNodeReportsTrafficSummary(){
        type = Protocol.MessageType.OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY.GetIntValue();
    }

    public OverlayNodeReportsTrafficSummary(byte[] marshalledBytes) throws IOException{
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din =
                new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readByte();

        NodeID = din.readInt();
        Sent = din.readInt();
        Relayed = din.readInt();
        SentSum = din.readLong();
        Received = din.readInt();
        ReceivedSum = din.readLong();
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
        dout.writeInt(NodeID);
        dout.writeInt(Sent);
        dout.writeInt(Relayed);
        dout.writeLong(SentSum);
        dout.writeInt(Received);
        dout.writeLong(ReceivedSum);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();
        return marshalledBytes;
    }
}
