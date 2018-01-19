/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.overlay.wireformats;

/**
 *
 * @author Mike
 */
public class OverlayNodeSendsDeregistration extends Protocol{
    
    public byte[] IPAddress;
    public int Port;
    
    public OverlayNodeSendsDeregistration (){}
    
    public OverlayNodeSendsDeregistration(byte[] rawData){
        ParseData(rawData);
    }

    @Override
    void ParseData(byte[] rawData) {
        MessageType = MessageType.GetMessageType(rawData[0]);
    }

    @Override
    byte[] BuildOutput() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
