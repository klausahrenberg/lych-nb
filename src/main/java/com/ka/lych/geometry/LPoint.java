package com.ka.lych.geometry;

import com.ka.lych.annotation.Json;
import com.ka.lych.observable.ILChangeListener;
import com.ka.lych.observable.ILObservable;
import com.ka.lych.observable.ILValidator;
import com.ka.lych.util.ILCloneable;
import com.ka.lych.util.LParseException;
import com.ka.lych.xml.ILXmlSupport;
import com.ka.lych.xml.LXmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.ka.lych.observable.LDouble;
import com.ka.lych.util.ILRegistration;
import com.ka.lych.util.LReflections;
import com.ka.lych.xml.LXmlUtils.LXmlParseInfo;

/**
 *
 * @author klausahrenberg
 */
public class LPoint
        implements ILPoint<LPoint>, ILCloneable, Comparable<LPoint>, ILObservable<LPoint, LPoint>, ILXmlSupport {

    @Json
    LDouble _x = new LDouble(0.0);
    @Json
    LDouble _y = new LDouble(0.0);
    double _precision = LGeomUtils.DEFAULT_DOUBLE_PRECISION;
    boolean _notifyAllowed = true;
    ILChangeListener<LPoint, LPoint> _changeListener;

    public LPoint() {
        this(0, 0);
    }

    public LPoint(double x, double y) {
        x(x).y(y);
        _x.addListener(c -> _notifyChangeListener());
        _y.addListener(c -> _notifyChangeListener());
    }
    
    protected void _notifyChangeListener() {
        if (_changeListener != null) {
            _changeListener.changed(null);
        }
    }
  
    @Override
    public LDouble x() {
        return _x;
    }
    
    @Override
    public LDouble y() {
        return _y;
    }

    @Override
    public void parseXml(Node n, LXmlParseInfo xmlParseInfo) throws LParseException {
        if (n.hasAttributes()) {
            LXmlUtils.parseXml(this, n, xmlParseInfo);
        } else {
            double[] coord = LXmlUtils.xmlStrToDoubleArray(new StringBuilder(n.getTextContent()), 2);
            this.point(coord[0], coord[1]);
        }
    }

    @Override
    public void toXml(Document doc, Element node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public LPoint clone() {
        try {
            LPoint p = (LPoint) LReflections.newInstance(getClass());
            p.x(_x.get());
            p.y(_y.get());
            p._precision = _precision;
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
        if ((LGeomUtils.isEqual(_x.get(), os._x.get(), _precision))
                && (LGeomUtils.isEqual(_y.get(), os._y.get(), _precision))) {
            return 0;
        } else {
            return (int) Math.ceil((_x.get() * _y.get()) - (os._x.get() * os._y.get()));
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " [" + _x + ", " + _y + "]";
    }
    
    @Override
    public ILRegistration addListener(ILChangeListener<LPoint, LPoint> changeListener) {
        if (_changeListener != null) {
            throw new IllegalStateException("Listener is already available");
        }
        _changeListener = changeListener;
        return () -> removeListener(_changeListener);
    }

    @Override
    public void removeListener(ILChangeListener<LPoint, LPoint> changeListener) {
        if (_changeListener != changeListener) {
            throw new IllegalStateException("Current defined listener is another one");
        }
        _changeListener = null;
    }

    @Override
    public ILRegistration addAcceptor(ILValidator<LPoint, LPoint> valueAcceptor) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void removeAcceptor(ILValidator<LPoint, LPoint> valueAcceptor) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
