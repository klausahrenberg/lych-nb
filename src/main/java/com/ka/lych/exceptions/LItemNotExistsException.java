package com.ka.lych.exceptions;

import com.ka.lych.util.LException;

/**
 *
 * @author klausahrenberg
 */
public class LItemNotExistsException extends LException {

    public LItemNotExistsException(Object sender, String message, Throwable cause) {
        super(sender, message, cause);
    }

    public LItemNotExistsException(Object sender, String message) {
        super(sender, message);
    }

}
