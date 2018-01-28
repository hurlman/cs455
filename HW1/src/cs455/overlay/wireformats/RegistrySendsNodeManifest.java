package cs455.overlay.wireformats;

import cs455.overlay.routing.RoutingEntry;

import java.io.*;
import java.util.*;

public class RegistrySendsNodeManifest implements Event {

    private int type;
    public List<RoutingEntry> NodeRoutingTable;
    public int[] orderedNodeList;

    public RegistrySendsNodeManifest() {
        type = Protocol.MessageType.REGISTRY_SENDS_NODE_MANIFEST.GetIntValue();
    }

    public RegistrySendsNodeManifest(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din =
                new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readByte();

        int tableSize = din.readByte();
        for (int i = 0; i < tableSize; i++) {
            int nodeID = din.readByte();
            int ipAddrLen = din.readByte();
            byte[] ipAddr = new byte[ipAddrLen];
            din.readFully(ipAddr);
            int port = din.readInt();
            NodeRoutingTable.add(new RoutingEntry(ipAddr, port, nodeID));
        }
        int nodeCount = din.readByte();
        orderedNodeList = new int[nodeCount];
        byte[] bNodes = new byte[nodeCount];
        din.readFully(bNodes);
        for (int i = 0; i < nodeCount; i++) {
            orderedNodeList[i] = bNodes[i];
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
        dout.writeByte(NodeRoutingTable.size());
        for (RoutingEntry re : NodeRoutingTable) {
            dout.writeByte(re.ID);
            dout.writeByte(re.IPAddress.length);
            dout.write(re.IPAddress);
            dout.writeInt(re.Port);
        }
        int nodeCount = orderedNodeList.length;
        byte[] bNodes = new byte[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            bNodes[i] = (byte) orderedNodeList[i];
        }
        dout.writeByte(nodeCount);
        dout.write(bNodes);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();
        return marshalledBytes;
    }
}
