package com.ka.lych.graphics;

import com.ka.lych.annotation.Json;
import java.util.EnumSet;
import java.util.Objects;
import com.ka.lych.util.LLog;

/**
 *
 * @author klausahrenberg
 */
public class LPaint extends LAbstractPaint<LPaint> {

    @Json
    LColor _color;

    public LPaint() {
    }
    
    public LPaint(LColor color) {
        color(color);
    }
    
    public LPaint(LStyle style, LColor color) {
        style(EnumSet.of(style));
        color(color);
    }

    public final LPaint color(LColor color) {
        _color = color;
        return this;
    }

    public final LColor color() {
        return _color;
    }

    @Override
    public Object clone() {
        try {
            LPaint p = (LPaint) super.clone();
            p._color = _color;            
            return p;
        } catch (Exception e) {
            LLog.error(this, "clone failed", e);
            throw new InternalError();
        }
    }
    
    @Override
    public boolean equals(Object st) {
        return ((st != null) && (st instanceof LPaint) ? this.hashCode() == st.hashCode() : false);
    }

    @Override
    public int hashCode() {       
        return Objects.hash(style(), color());
    }
    
    @Override
    public String toString() {
        return this.getClass().getName() + " [" + _style + ", " + _color + "]";
    }
    
    public static LPaint create() {
        return new LPaint();
    }

}
