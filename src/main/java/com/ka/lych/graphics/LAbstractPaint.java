package com.ka.lych.graphics;

import com.ka.lych.annotation.Json;
import java.util.EnumSet;
import java.util.Objects;
import com.ka.lych.util.ILCloneable;
import com.ka.lych.util.ILConstants;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LReflections;

/**
 *
 * @author klausahrenberg
 * @param <BC>
 */
public abstract class LAbstractPaint<BC>
        implements ILCanvasCommand, ILCloneable, ILConstants {

    @Json
    EnumSet<LStyle> _style = EnumSet.noneOf(LStyle.class);

    public LAbstractPaint() {
    }

    public EnumSet<LStyle> style() {
        return _style;
    }
    
    public BC style(EnumSet<LStyle> style) {
        _style = style;
        return (BC) this;
    }

    public boolean contains(LStyle style) {
        return style().contains(style);
    }

    public BC style(LStyle style) {
        style().add(style);
        return (BC) this;
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
            p._style = _style;
            return p;
        } catch (Exception e) {
            LLog.error(this, "clone failed", e);
            throw new InternalError();
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " [" + _style + "]";
    }
    
    @Override
    public boolean equals(Object st) {
        return ((st != null) && (st instanceof LAbstractPaint) ? this.hashCode() == st.hashCode() : false);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(style());
    }

}
