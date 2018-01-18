/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.overlay.wireformats;
import cs455.overlay.*;

/**
 *
 * @author Mike
 */
public class OverlayNodeSendsRegistration extends Protocol {

    OverlayNodeSendsRegistration (byte[] rawData){
        ParseData(rawData);
    }
    
    OverlayNodeSendsRegistration(){}
    
    @Override
    void ParseData(byte[] rawData) {
        MessageType = MessageType.GetMessageType(rawData[0]);
        if(MessageType == null)
            throw new IllegalArgumentException(String.format("Bad message data.  Message type unknown: %s.", rawData[0]));
    }

    @Override
    byte[] BuildOutput() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
