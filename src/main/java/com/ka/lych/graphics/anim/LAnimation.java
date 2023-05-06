package com.ka.lych.graphics.anim;

import com.ka.lych.observable.LBoolean;
import com.ka.lych.observable.LInteger;
import com.ka.lych.util.ILConstants;
import com.ka.lych.xml.LXmlUtils;
import com.ka.lych.annotation.Xml;

/**
 *
 * @author klausahrenbegr
 */
public abstract class LAnimation 
        implements ILAnimation, ILConstants {

    private final Integer DEFAULT_DELAY = 0;
    private final Integer DEFAULT_DURATION = 0;
    private final Boolean DEFAULT_INFINITE = false;
    
    @Xml
    protected LInteger delay;
    @Xml
    protected LInteger duration;
    @Xml
    protected LBoolean infinite;  

    
    public LAnimation() {
        
    }

    public LInteger delay() {
        if (delay == null) {
            delay = new LInteger(DEFAULT_DELAY);
        }
        return delay;
    }

    @Override
    public Integer getDelay() {
        return (delay != null ? delay.get() : DEFAULT_DELAY);
    }    

    public void setDelay(Integer delay) {
        delay().set(delay);
    }
    
    public LInteger duration() {
        if (duration == null) {
            duration = new LInteger(DEFAULT_DURATION);
        }
        return duration;
    }

    @Override
    public Integer getDuration() {
        return (duration != null ? duration.get() : DEFAULT_DURATION);
    }

    public void setDuration(Integer duration) {
        duration().set(duration);
    }

    public LBoolean infinite() {
        if (infinite == null) {
            infinite = new LBoolean(DEFAULT_INFINITE);
        }
        return infinite;
    }
    
    @Override
    public Boolean isInfinite(){
        return infinite != null ? infinite.get() : DEFAULT_INFINITE;
    }

    public void setInfinite(Boolean infinite) {
        infinite().set(infinite);
    }

    @Override
    public String toString() {
        return LXmlUtils.classToString(this, Xml.class);
    }
    
}
