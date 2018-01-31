package cs455.overlay.wireformats;

import java.io.*;

public class RegistryRequestsTrafficSummary implements Event {

    private int type;

    public RegistryRequestsTrafficSummary(){
        type = Protocol.MessageType.REGISTRY_REQUESTS_TRAFFIC_SUMMARY.GetIntValue();
    }

    public RegistryRequestsTrafficSummary(byte[] marshalledBytes) throws IOException{
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din =
                new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readByte();
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

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();
        return marshalledBytes;
    }
}
