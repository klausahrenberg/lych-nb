package com.ka.lych.event;

/**
 *
 * @author klausahrenberg
 */
public class LNotificationEvent extends LEvent {
 
    private final String message;    
    private final Integer duration;    

    @SuppressWarnings("unchecked")
    public LNotificationEvent(Object source, String message, Integer duration) {
        super(source);        
        this.message = message;
        this.duration = duration;
    }
    
    public String getMessage() {
        return message;
    }

    public Integer getDuration() {
        return duration;
    }

    
    
}
