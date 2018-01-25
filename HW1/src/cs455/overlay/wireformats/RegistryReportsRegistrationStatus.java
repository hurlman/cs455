package cs455.overlay.wireformats;

import java.io.*;

public class RegistryReportsRegistrationStatus implements Event {

    public int SuccessStatus;
    public String Message;

    private int type;

    public RegistryReportsRegistrationStatus() {
        type = Protocol.MessageType.REGISTRY_REPORTS_REGISTRATION_STATUS.GetIntValue();
    }

    public RegistryReportsRegistrationStatus(byte[] marshalledBytes) throws IOException{
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din =
                new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readByte();
        SuccessStatus = din.readInt();

        int messageLen = din.readByte();
        byte[] messageBytes = new byte[messageLen];
        din.readFully(messageBytes);
        Message = new String(messageBytes, "US-ASCII");
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
        dout.writeInt(SuccessStatus);
        byte[] messageBytes = Message.getBytes("US-ASCII");
        dout.writeByte(messageBytes.length);
        dout.write(messageBytes);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();
        return marshalledBytes;
    }
}
