package com.ka.lych.graphics.anim;

import com.ka.lych.annotation.Json;
import com.ka.lych.observable.LBoolean;
import com.ka.lych.observable.LInteger;
import com.ka.lych.util.ILConstants;
import com.ka.lych.xml.LXmlUtils;

/**
 *
 * @author klausahrenbegr
 */
public abstract class LAnimation 
        implements ILAnimation, ILConstants {
    
    @Json
    int _delay = 0;
    @Json
    int _duration = 0;
    @Json
    boolean _infinite = false;  
    
    public LAnimation() {        
    }

    @Override
    public int delay() {
        return _delay;
    }    

    @Override
    public int duration() {
        return _duration;
    }

    public void duration(int duration) {
        _duration = duration;
    }
    
    public boolean isInfinite(){
        return _infinite;
    }

    public void setInfinite(boolean infinite) {
        _infinite = infinite;
    }

    @Override
    public String toString() {
        return LXmlUtils.classToString(this);
    }
    
}
