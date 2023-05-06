package com.ka.lych.graphics;

import java.util.Objects;
import com.ka.lych.observable.LDouble;
import com.ka.lych.observable.LObservable;
import com.ka.lych.util.ILConstants;
import com.ka.lych.annotation.Xml;

/**
 *
 * @author klausahrenberg
 */
public class LStop {
    
    @Xml
    protected LDouble offset;
    @Xml
    protected LObservable<LColor> color;
    
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
    
    public final LObservable<LColor> color() {
        if (color == null) {
            color = new LObservable<>();
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
