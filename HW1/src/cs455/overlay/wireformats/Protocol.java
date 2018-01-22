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
    
    // ParseData to always be called by constructor, when initialized with data.
    // For use when receiving this message.
    abstract void ParseData(byte[] rawData);
    
    // For use when generating this message to build the byte array to send.
    abstract byte[] BuildOutput();
    
    public static int ByteToInt(byte[] bytes) {
        int i = 0;
        i |= bytes[0] & 0xFF;
        i <<= 8;
        i |= bytes[1] & 0xFF;
        i <<= 8;
        i |= bytes[2] & 0xFF;
        return i;
    }
    
    public static byte[] IntToByte(int i){
        byte[] bytes = new byte[3];
        bytes[0] = (byte) ((i >> 16) & 0xFF);
        bytes[1] = (byte) ((i >> 8) & 0xFF);
        bytes[2] = (byte) (i  & 0xFF);
        return bytes;
    }

    public static final int BUFFER_SIZE = 1024;
    
}
