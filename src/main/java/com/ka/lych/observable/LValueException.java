package com.ka.lych.observable;

import com.ka.lych.util.LException;

/**
 *
 * @author klausahrenberg
 */
public class LValueException extends LException {

    public LValueException(Object sender, String exceptionCode) {
        super(sender, exceptionCode);
    }

    public LValueException(Object sender, String exceptionCode, Throwable cause) {
        super(sender, exceptionCode, cause);
    }
    
}
