package com.ka.lych.graphics;

import java.util.EnumSet;
import java.util.Objects;
import com.ka.lych.observable.LObservable;
import com.ka.lych.util.ILConstants;
import com.ka.lych.util.LLog;
import com.ka.lych.annotation.Xml;

/**
 *
 * @author klausahrenberg
 */
public class LPaint extends LAbstractPaint {

    @Xml
    protected LObservable<LColor> color;

    public LPaint() {
    }
    
    public LPaint(LColor color) {
        setColor(color);
    }
    
    public LPaint(LPaintStyle style, LColor color) {
        setStyle(EnumSet.of(style));
        setColor(color);
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
    public Object clone() {
        try {
            LPaint p = (LPaint) super.clone();
            LObservable.clone(color);
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
        return Objects.hash(getStyle(), getColor());
    }

}
