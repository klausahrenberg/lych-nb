package com.ka.lych.list;

import com.ka.lych.util.LException;

/**
 *
 * @author klausahrenberg
 */
public class LDoubleHashKeyException extends LException {

    public LDoubleHashKeyException(Object sender, String message, Throwable cause) {
        super(sender, message, cause);
    }

    public LDoubleHashKeyException(Object sender, String message) {
        super(sender, message);
    }

}
