package com.ka.lych.exception;

import com.ka.lych.LBase;
import com.ka.lych.annotation.Json;
import com.ka.lych.list.LList;
import com.ka.lych.observable.LString;
import com.ka.lych.util.ILConsumer;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 * @author klausahrenberg
 */
public class LException extends Exception {
    
    @Json
    protected final String _key;
    @Json
    protected final LList<Object> _arguments;
    
    public LException(final String key, final LList<Object> arguments) {
        super(key != null ? LString.format(key, arguments) : "");
        _key = key;
        _arguments = arguments;
    }
    
    public LException(final String key, final Object... arguments) {
        super(key != null ? LString.format(key, arguments) : "");
        _key = key;
        _arguments = (arguments != null ? LList.of(arguments) : null);
    }
    
    public LException(final Throwable cause) {
        super(cause != null ? cause.getMessage() : "");
        _key = cause.getMessage();
        _arguments = null;
    }
    
    public LException(final Throwable cause, final String key, final Object... arguments) {       
        super((key != null ? LString.format(key, arguments) : (cause != null ? cause.getMessage() : "")), cause);            
        _key = (key != null ? key : (cause != null ? cause.getMessage() : ""));
        _arguments = (arguments != null ? LList.of(arguments) : null);
    }   

    public String key() { return _key; }
    
    public LList<Object> arguments() { return _arguments; }
    
    @Override
    public String getMessage() {
        if (_key != null) {
            return LBase.getResources().localize(this, _key, _arguments);            
        } else {
            return super.getMessage();
        }
    }

    public static <T> Consumer<T> throwing(ILConsumer<T> throwingConsumer) {
        return ILConsumer.accept(throwingConsumer);
    }

    public static LException of(Map<String, Object> map) {
        String key = (String) map.get("key");
        LList<Map> arguments = (LList<Map>) map.get("arguments");
        return new LException(key, arguments);
    }
    
}
