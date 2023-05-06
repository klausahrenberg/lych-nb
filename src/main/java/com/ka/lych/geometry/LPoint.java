package com.ka.lych.geometry;

import com.ka.lych.observable.ILChangeListener;
import com.ka.lych.observable.ILObservable;
import com.ka.lych.util.ILCloneable;
import com.ka.lych.util.LParseException;
import com.ka.lych.xml.ILXmlSupport;
import com.ka.lych.xml.LXmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.ka.lych.observable.ILValidator;
import com.ka.lych.util.LReflections;
import com.ka.lych.xml.LXmlUtils.LXmlParseInfo;
import com.ka.lych.annotation.Xml;
import com.ka.lych.util.ILRegistration;

/**
 *
 * @author klausahrenberg
 */
public class LPoint
        implements ILPoint, ILCloneable, Comparable<LPoint>,ILObservable<LPoint>, ILXmlSupport {

    @Xml
    private double x;
    @Xml
    private double y;
    protected double precision;
    protected boolean notifyAllowed;
    protected ILChangeListener<LPoint> changeListener;

    public LPoint() {
        this(0, 0);
    }

    public LPoint(double x, double y) {
        this.x = x;
        this.y = y;
        this.notifyAllowed = true;
        precision = LGeomUtils.DEFAULT_DOUBLE_PRECISION;
    }

    @Override
    public ILRegistration addListener(ILChangeListener<LPoint> changeListener) {
        if (this.changeListener != null) {
            throw new IllegalStateException("Listener is already available");
        }
        this.changeListener = changeListener;
        return () -> removeListener(changeListener);
    }

    @Override
    public void removeListener(ILChangeListener<LPoint> changeListener) {
        if (this.changeListener != this.changeListener) {
            throw new IllegalStateException("Current defined listener is another one");
        }
        this.changeListener = null;
    }
    

    @Override
    public double getX() {
        return x;
    }
    
    public int getXIntValue() {
        return (int) Math.round(getX());
    }

    @Override
    public void setX(double x) {
        if ((notifyAllowed) && (changeListener != null) && (LGeomUtils.isNotEqual(x, this.x, precision))) {
            double oldValue = this.x;
            this.x = x;
            synchronized(changeListener) {
                changeListener.changed(null);
            }    
        } else {
            this.x = x;
        }
    }

    @Override
    public double getY() {
        return y;
    }

    public int getYIntValue() {
        return (int) Math.round(getY());
    }

    @Override
    public void setY(double y) {
        if ((notifyAllowed) && (changeListener != null) && (LGeomUtils.isNotEqual(y, this.y, precision))) {
            double oldValue = this.y;
            this.y = y;
            synchronized(changeListener) {
                changeListener.changed(null);
            }    
        } else {
            this.y = y;
        }
    }

    public void setXY(double x, double y) {        
        this.setPoint(x, y);
    }

    public void setPoint(double x, double y) {
        if ((notifyAllowed) && (changeListener != null)
                && ((LGeomUtils.isNotEqual(x, this.x, precision))
                || (LGeomUtils.isNotEqual(y, this.y, precision)))) {
            double oldValue = this.x;
            this.x = x;
            this.y = y;
            synchronized(changeListener) {
                changeListener.changed(null);
            }    
        } else {
            this.x = x;
            this.y = y;
        }
    }

    @Override
    public void parseXml(Node n, LXmlParseInfo xmlParseInfo) throws LParseException {
        if (n.hasAttributes()) {
            LXmlUtils.parseXml(this, n, xmlParseInfo);
        } else {
            double[] coord = LXmlUtils.xmlStrToDoubleArray(new StringBuilder(n.getTextContent()), 2);
            this.setPoint(coord[0], coord[1]);
        }
    }

    @Override
    public void toXml(Document doc, Element node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Object clone() {
        try {
            LPoint p = (LPoint) LReflections.newInstance(getClass());
            p.x = x;
            p.y = y;
            p.precision = precision;
            return p;
        } catch (Exception ex) {
            throw new InternalError(ex);
        }
    }
    
    @Override
    public int compareTo(LPoint os) {
        if (os == null) {
            return 1;
        }
        if ((LGeomUtils.isEqual(x, os.x, precision))
                && (LGeomUtils.isEqual(y, os.y, precision))) {
            return 0;
        } else {
            return (int) Math.ceil((x * y) - (os.x * os.y));
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " [" + x + ", " + y + "]";
    }

    @Override
    public ILRegistration addAcceptor(ILValidator<LPoint> valueAcceptor) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeAcceptor(ILValidator<LPoint> valueAcceptor) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
