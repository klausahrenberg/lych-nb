package com.ka.lych.graphics;

import com.ka.lych.annotation.Json;
import java.util.Objects;
import com.ka.lych.observable.LDouble;
import com.ka.lych.observable.LObject;
import com.ka.lych.util.ILCloneable;
import com.ka.lych.util.ILConstants;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LParseException;
import com.ka.lych.xml.ILXmlSupport;
import com.ka.lych.util.LReflections;
import com.ka.lych.xml.LXmlUtils;
import com.ka.lych.xml.LXmlUtils.LXmlParseInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author klausahrenberg
 */
public class LStroke
        implements ILCloneable, ILXmlSupport, ILConstants, ILCanvasCommand {

    private final Double DEFAULT_WIDTH = null;
    private final LStrokeLineCap DEFAULT_CAP = null;
    private final LStrokeLineJoin DEFAULT_JOIN = null;
    private final Double[] DEFAULT_DASH = null;
    private final Double DEFAULT_MITER_LIMIT = null;
    private final Double DEFAULT_DASH_PHASE = null;

    @Json
    protected LDouble width;
    @Json
    protected LObject<LStrokeLineJoin> join;
    @Json
    protected LObject<LStrokeLineCap> cap;
    @Json
    protected LDouble miterLimit;
    @Json
    protected LObject<Double[]> dash;
    @Json
    protected LDouble dashPhase;

    public LStroke() {
        this(false);
    }

    public LStroke(boolean setDefaultValues) {
        if (setDefaultValues) {
            setWidth(1.0);
            setJoin(LStrokeLineJoin.MITER);
            setCap(LStrokeLineCap.SQUARE);
            setMiterLimit(10.0);
            setDash(null);
            setDashPhase(0.0);
        }
    }

    public LStroke(Double width, LStrokeLineJoin join, LStrokeLineCap cap, Double miterLimit, Double[] dash, Double dashPhase) {
        setWidth(width);
        setJoin(join);
        setCap(cap);
        setMiterLimit(miterLimit);
        setDash(dash);
        setDashPhase(dashPhase);
    }

    public LStroke(Node n, LXmlParseInfo xmlParseInfo) throws LParseException {
        this(true);
        parseXml(n, xmlParseInfo);
    }

    public LDouble width() {
        if (width == null) {
            width = new LDouble(DEFAULT_WIDTH);
        }
        return width;
    }

    public Double getWidth() {
        return width != null ? width.get() : DEFAULT_WIDTH;
    }

    public void setWidth(Double width) {
        width().set(width);
    }

    public LObject<LStrokeLineJoin> join() {
        if (join == null) {
            join = new LObject<>(DEFAULT_JOIN);
        }
        return join;
    }

    public LStrokeLineJoin getJoin() {
        return join != null ? join.get() : DEFAULT_JOIN;
    }

    public void setJoin(LStrokeLineJoin join) {
        join().set(join);
    }

    public LObject<LStrokeLineCap> cap() {
        if (cap == null) {
            cap = new LObject<>(DEFAULT_CAP);
        }
        return cap;
    }

    public LStrokeLineCap getCap() {
        return cap != null ? cap.get() : DEFAULT_CAP;
    }

    public void setCap(LStrokeLineCap cap) {
        cap().set(cap);
    }

    public LDouble miterLimit() {
        if (miterLimit == null) {
            miterLimit = new LDouble(DEFAULT_MITER_LIMIT);
        }
        return miterLimit;
    }

    public Double getMiterLimit() {
        return miterLimit != null ? miterLimit.get() : DEFAULT_MITER_LIMIT;
    }

    public void setMiterLimit(Double miterLimit) {
        miterLimit().set(miterLimit);
    }    

    public LObject<Double[]> dash() {
        if (dash == null) {
            dash = new LObject<>(DEFAULT_DASH);
        }
        return dash;
    }

    public Double[] getDash() {
        return dash != null ? dash.get() : DEFAULT_DASH;
    }

    public void setDash(Double[] dash) {
        dash().set(dash);
    }    

    public LDouble dashPhase() {
        if (dashPhase == null) {
            dashPhase = new LDouble(DEFAULT_DASH_PHASE);
        }
        return dashPhase;
    }

    public Double getDashPhase() {
        return dashPhase != null ? dashPhase.get() : DEFAULT_DASH_PHASE;
    }

    public void setDashPhase(Double dashPhase) {
        dashPhase().set(dashPhase);
    }   

    @Override
    public void parseXml(Node n, LXmlParseInfo xmlParseInfo) throws LParseException {        
        LXmlUtils.parseXml(this, n, xmlParseInfo);      
    }

    @Override
    public void toXml(Document doc, Element node) {
        throw new UnsupportedOperationException("Not supported yet.");
        //Stiftbreite
        //LXmlUtils.setAttribute(node, "width", LXmlUtils.doubleToXmlStr(width));
    }

    @Override
    public void execute(LCanvasRenderer canvasRenderer, long timeLine) {
        canvasRenderer.setStroke(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public LStroke clone() {
        try {
            LStroke po = (LStroke) LReflections.newInstance(getClass()); 
            po.width().set(width.get());
            po.join().set(join.get());
            po.cap().set(cap.get());
            po.miterLimit().set(miterLimit.get());
            po.dash().set(dash.get());
            po.dashPhase().set(dashPhase.get());
            /*if (dash != null) {
                po.dash = Arrays.copyOf(dash, dash.length);
            }*/
            return po;
        } catch (Exception e) {
            LLog.error(this, "LStroke.clone", e);
            throw new InternalError();
        }
    }

    @Override
    public String toString() {
        return LXmlUtils.classToString(this);
    }

    /*@Override
    public String toString() {
        return getClass().getName() + " [w=" + getWidth() + " cap=" + getCap() + " join=" + getJoin() + " limit=" + getMiterLimit()
                + " ary=" + Arrays.toString(getDash()) + " phase=" + getDashPhase() + "]";
    }*/
    @Override
    public boolean equals(Object st) {
        return ((st != null) && (st instanceof LStroke) ? this.hashCode() == st.hashCode() : false);
    }

    @Override
    public int hashCode() {
        //strokeWidth;
        //strokeJoin;
        //strokeCap;
        //strokeMiterLimit;
        //strokeDash;
        //strokeDashPhase;
        return Objects.hash(getWidth(), getJoin(), getCap(),
                getMiterLimit(), getDash(), getDashPhase());
    }

}
