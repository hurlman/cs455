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
public interface Protocol {
    public byte[] RAWDATA = new byte[Constants.BUFFER_SIZE];
    public void ParseData();
    public byte[] BuildOutput();
}
