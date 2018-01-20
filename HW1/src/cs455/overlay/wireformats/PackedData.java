/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.overlay.wireformats;

import java.util.Arrays;

/**
 *
 * @author Mike
 */
public class PackedData {

    public int NewIndex;
    public byte[] Data;

    // Gets length to read from data with index.  
    // Increments index by the length + 1. Sets to NewIndex property.
    // Sets Data to specified sub array.
    public PackedData(int index, byte[] data) {
        int length = data[index];
        NewIndex = length + index;
        Data = Arrays.copyOfRange(data, index + 1, NewIndex++);
    }

    // Utility function to return a byte array prepended by a byte containing
    // its length.
    public static byte[] Pack(byte[] data) {
        int len = data.length;
        byte blen = (byte) len;
        byte packedData[] = new byte[data.length + 1];
        packedData[0] = blen;
        System.arraycopy(data, 0, packedData, 1, data.length);
        return packedData;
    }

}
