package com.ka.lych.graphics;

import com.ka.lych.annotation.Json;
import java.util.EnumSet;
import java.util.Objects;
import com.ka.lych.util.ILCloneable;
import com.ka.lych.util.ILConstants;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LReflections;
import com.ka.lych.xml.*;

/**
 *
 * @author klausahrenberg
 */
public abstract class LAbstractPaint<BC>
        implements ILCanvasCommand, ILCloneable, ILConstants {

    static EnumSet<LPaintStyle> DEFAULT_STYLE = EnumSet.of(LPaintStyle.FILL);
    @Json
    EnumSet<LPaintStyle> _style;

    public LAbstractPaint() {
    }

    public EnumSet<LPaintStyle> style() {
        return (_style != null ? _style : DEFAULT_STYLE);
    }
    
    public BC style(EnumSet<LPaintStyle> style) {
        _style = style;
        return (BC) this;
    }

    public boolean contains(LPaintStyle style) {
        return style().contains(style);
    }

    public void add(LPaintStyle style) {
        style().add(style);
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
        return LXmlUtils.classToString(this);
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
