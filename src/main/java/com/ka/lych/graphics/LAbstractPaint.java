package com.ka.lych.graphics;

import com.ka.lych.annotation.Json;
import java.util.EnumSet;
import java.util.Objects;
import com.ka.lych.observable.LObservable;
import com.ka.lych.util.ILCloneable;
import com.ka.lych.util.ILConstants;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LReflections;
import com.ka.lych.xml.*;

/**
 *
 * @author klausahrenberg
 */
public abstract class LAbstractPaint
        implements ILCanvasCommand, ILCloneable, ILConstants {

    protected static EnumSet<LPaintStyle> DEFAULT_STYLE = EnumSet.of(LPaintStyle.FILL);
    @Json
    protected LObservable<EnumSet<LPaintStyle>> style;

    public LAbstractPaint() {
    }

    public LObservable<EnumSet<LPaintStyle>> style() {
        if (style == null) {
            style = new LObservable<>(DEFAULT_STYLE);
        }
        return style;
    }

    public boolean isStyle(LPaintStyle style) {
        return getStyle().contains(style);
    }

    public EnumSet<LPaintStyle> getStyle() {
        return (style != null ? style.get() : DEFAULT_STYLE);
    }

    public void setStyle(EnumSet<LPaintStyle> style) {
        style().set(style);
    }

    public void addStyle(LPaintStyle style) {
        style().get().add(style);
    }

    @Override
    public void execute(LCanvasRenderer canvasRenderer, long now) {
        canvasRenderer.setPaint(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            LAbstractPaint p = (LAbstractPaint) LReflections.newInstance(getClass());
            p.style = LObservable.clone(style);
            return p;
        } catch (Exception e) {
            LLog.error(this, "clone failed", e);
            throw new InternalError();
        }
    }

    @Override
    public String toString() {
        return LXmlUtils.classToString(this);
    }
    
    @Override
    public boolean equals(Object st) {
        return ((st != null) && (st instanceof LAbstractPaint) ? this.hashCode() == st.hashCode() : false);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getStyle());
    }

}
