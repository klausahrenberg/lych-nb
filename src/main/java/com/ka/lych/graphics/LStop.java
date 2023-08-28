package com.ka.lych.graphics;

import com.ka.lych.annotation.Json;
import java.util.Objects;
import com.ka.lych.observable.LDouble;
import com.ka.lych.observable.LObject;

/**
 *
 * @author klausahrenberg
 */
public class LStop {
    
    @Json
    protected LDouble offset;
    @Json
    protected LObject<LColor> color;
    
    public LStop() {
        
    }
    
    public LStop(double offset, LColor color) {
        setOffset(offset);
        setColor(color);
    }
    
    public final LDouble offset() {
        if (offset == null) {
            offset = new LDouble();            
        }
        return offset;
    }
    
    public final double getOffset() {
        return (offset != null ? offset.get() : 0.0);
    }

    public final void setOffset(double offset) {
        offset().set(offset);
    }
    
    public final LObject<LColor> color() {
        if (color == null) {
            color = new LObject<>();
        }
        return color;
    }

    public final void setColor(LColor color) {
        color().set(color);
    }

    public final LColor getColor() {
        return (color != null ? color.get() : null);
    }    

    @Override
    public boolean equals(Object st) {
        return ((st != null) && (st instanceof LStop) ? this.hashCode() == st.hashCode() : false);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOffset(), getColor());
    }
    
}
