package com.ka.lych.util;

/**
 *
 * @author klausahrenberg
 */
public class LReadOnlyException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1197712207092567127L;

	public LReadOnlyException(String message, Throwable cause) {
        super(message, cause);
    }

    public LReadOnlyException(String message) {
        super(message);
    }

}
