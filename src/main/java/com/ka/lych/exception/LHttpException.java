package com.ka.lych.exception;

/**
 *
 * @author klausahrenberg
 */
public class LHttpException extends LDataException {

    public LHttpException(String key, Object... arguments) {
        super(key, arguments);
    }

    public LHttpException(Throwable cause) {
        super(cause);
    }    

}
