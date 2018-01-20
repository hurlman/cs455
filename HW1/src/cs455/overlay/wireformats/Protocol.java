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
public abstract class Protocol {
    public Constants.MessageType MessageType;
    
    // ParseData to always be called by constructor, when initialized with data.
    // For use when receiving this message.
    abstract void ParseData(byte[] rawData);
    
    // For use when generating this message to build the byte array to send.
    abstract byte[] BuildOutput();
    
}
