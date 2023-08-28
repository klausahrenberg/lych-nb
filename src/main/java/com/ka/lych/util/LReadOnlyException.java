package com.ka.lych.util;

/**
 *
 * @author klausahrenberg
 */
public class LReadOnlyException extends Exception {

    public LReadOnlyException(String message, Throwable cause) {
        super(message, cause);
    }

    public LReadOnlyException(String message) {
        super(message);
    }

}
