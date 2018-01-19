/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.overlay.wireformats;
import cs455.overlay.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 *
 * @author Mike
 */
public class OverlayNodeSendsRegistration extends Protocol {

    public byte[] IPAddress;
    public int Port;
    
    public OverlayNodeSendsRegistration (byte[] rawData){
        ParseData(rawData);
    }
    
    public OverlayNodeSendsRegistration(){}
    
    @Override
    void ParseData(byte[] rawData) {
        MessageType = MessageType.GetMessageType(rawData[0]);
        int len = rawData[1];
                
        IPAddress = Arrays.copyOfRange(rawData, 2, len + 2);
        ByteBuffer wrapped = ByteBuffer.wrap(rawData, len + 2, 4);
        Port = wrapped.getInt();
    }

    @Override
    byte[] BuildOutput() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
