package com.ka.lych.util;

/**
 *
 * @author klausahrenberg
 */
public class LValueNotFoundException extends Exception {

    public LValueNotFoundException(String message) {
        super(message);
    }
    
    public LValueNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
