/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.overlay.wireformats;
import cs455.overlay.*;
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
        int index = 0;
        MessageType = MessageType.GetMessageType(rawData[index++]);
        
        PackedData ipAddr = new PackedData(index, rawData);
        IPAddress = ipAddr.Data;
        index = ipAddr.NewIndex;
        
        byte[] port = Arrays.copyOfRange(rawData, index, index + 3);
        Port = Constants.ByteToInt(port);
    }

    @Override
    byte[] BuildOutput() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
