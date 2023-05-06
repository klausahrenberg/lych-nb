package com.ka.lych.repo;

import com.ka.lych.util.LException;

/**
 *
 * @author klausahrenberg
 */
public class LDataException extends LException {

    public LDataException(Object sender, String message, Throwable cause) {
        super(sender, message, cause);
    }

    public LDataException(Object sender, String message) {
        super(sender, message);
    }

}
