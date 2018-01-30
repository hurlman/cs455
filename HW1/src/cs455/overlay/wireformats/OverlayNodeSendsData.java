package cs455.overlay.wireformats;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class OverlayNodeSendsData implements Event {

    private int type;

    public int DestinationID;
    public int SourceID;
    public int Payload;
    public List<Integer> DisseminationTrace = new ArrayList<>();

    public OverlayNodeSendsData() {
        type = Protocol.MessageType.OVERLAY_NODE_SENDS_DATA.GetIntValue();
    }

    public OverlayNodeSendsData(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din =
                new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readByte();
        DestinationID = din.readInt();
        SourceID = din.readInt();
        Payload = din.readInt();

        int traceLen = din.readInt();
        for (int i = 0; i < traceLen; i++) {
            DisseminationTrace.add(din.readInt());
        }
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
        dout.writeInt(DestinationID);
        dout.writeInt(SourceID);
        dout.writeInt(Payload);

        dout.writeInt(DisseminationTrace.size());
        for (int i : DisseminationTrace) {
            dout.writeInt(i);
        }

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();
        return marshalledBytes;
    }
}
