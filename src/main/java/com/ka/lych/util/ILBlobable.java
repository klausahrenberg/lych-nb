package com.ka.lych.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author klausahrenberg
 */
public interface ILBlobable {
    
    public void read(InputStream is) throws IOException;

    public void write(OutputStream os) throws IOException;
    
}
