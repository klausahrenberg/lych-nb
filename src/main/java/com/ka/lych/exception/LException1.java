package com.ka.lych.exception;

import com.ka.lych.LBase;
import com.ka.lych.annotation.Json;
import com.ka.lych.list.LList;
import com.ka.lych.observable.LString;
import com.ka.lych.util.ILConsumer;
import java.util.function.Consumer;

/**
 *
 * @author klausahrenberg
 */
public class LException1 extends Exception {
    
    public static int UNSPECIFIED = 0;
    
    @Json
    protected final String _key;
    @Json
    protected final LList<Object> _arguments;
    
    public LException1() {
        super();
        _key = null;
        _arguments = null;
    }    

    public String key() { return _key; }
    
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

}
