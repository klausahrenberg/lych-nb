package com.ka.lych.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author klausahrenberg
 */
public interface ILBlobable {
    
    public void read(ObjectInputStream is) throws IOException;

    public void write(ObjectOutputStream os) throws IOException;
    
}
