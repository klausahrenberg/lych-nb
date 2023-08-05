package com.ka.lych.geometry;

import com.ka.lych.annotation.Json;
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
import com.ka.lych.observable.LDouble;
import com.ka.lych.util.LReflections;
import com.ka.lych.xml.LXmlUtils.LXmlParseInfo;
import com.ka.lych.util.ILRegistration;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LSize<T extends ILSize> 
        implements ILSize<T>, ILCloneable, Comparable<T>/*, ILObservable<LSize<T>>*/, ILXmlSupport {

    @Json
    LDouble _width = new LDouble();
    @Json
    LDouble _height = new LDouble();
    double _precision;
    boolean _notifyAllowed;
    //ILChangeListener<LSize<T>> _changeListener;

    public LSize() {
        this(0, 0);
    }

    public LSize(double width, double height) {
        _width.set(width);
        _height.set(height);
        _notifyAllowed = true;
        _precision = LGeomUtils.DEFAULT_DOUBLE_PRECISION;
    }

    /*@Override
    public ILRegistration addListener(ILChangeListener<LSize<T>> changeListener) {
        if (this._changeListener != null) {
            throw new IllegalStateException("Listener is already available");
        }
        this._changeListener = changeListener;
        return () -> removeListener(changeListener);
    }

    @Override
    public void removeListener(ILChangeListener<LSize<T>> changeListener) {
        if (this._changeListener != this._changeListener) {
            throw new IllegalStateException("Current defined listener is another one");
        }
        this._changeListener = null;
    }*/
    
    @Override
    public LDouble width() {
        return _width;
    }
    
    public int getWidthIntValue() {
        return (int) Math.round(width().get());
    }
    
    public int getWidthIntCeil() {
        return (int) Math.ceil(width().get());
    }

    /*@Override
    public T width(double width) {
        if ((_notifyAllowed) && (_changeListener != null) && (LGeomUtils.isNotEqual(width, _width.get(), _precision))) {
            double oldValue = _width.get();
            this._width = width;
            synchronized(_changeListener) {
                _changeListener.changed(null);
            }
        } else {
            this._width = width;
        }
        return (T) this;
    }*/

    @Override
    public LDouble height() {
        return _height;
    }
    
    public int getHeightIntValue() {
        return (int) Math.round(height().get());
    }   

    public int getHeightIntCeil() {
        return (int) Math.ceil(height().get());
    }

    /*@Override
    public T height(double height) {
        if ((_notifyAllowed) && (_changeListener != null) && (LGeomUtils.isNotEqual(height, this._height, _precision))) {
            double oldValue = this._height;
            this._height = height;
            synchronized(_changeListener) {
                _changeListener.changed(null);
            }    
        } else {
            this._height = height;
        }
        return (T) this;
    }*/
    
    /*public void setSize(double width, double height) {
        if ((_notifyAllowed) && (_changeListener != null)
                && ((LGeomUtils.isNotEqual(width, this._width, _precision))
                || (LGeomUtils.isNotEqual(height, this._height, _precision)))) {
            double oldValue = this._width;
            this._width = width;
            this._height = height;
            synchronized(_changeListener) {
                _changeListener.changed(null);
            }    
        } else {
            this._width = width;
            this._height = height;
        }
    }*/

    public double getPrecision() {
        return _precision;
    }

    public void setPrecision(double precision) {
        this._precision = precision;
    }
    
    @Override
    public boolean isEmpty() {
        return ((_width.get() <= 0.0) || (_height.get() <= 0.0));
    }

    @Override
    public Object clone() {
        try {
            LSize p = (LSize) LReflections.newInstance(getClass());            
            p._width = _width;
            p._height = _height;
            p._precision = _precision;
            return p;
        } catch (Exception ex) {
            throw new InternalError(ex);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " [" + _width + ", " + _height + "]";
    }

    @Override
    public int compareTo(T os) {
        if (os == null) {
            return 1;
        }
        if ((LGeomUtils.isEqual(_width.get(), os.width().get(), _precision))
                && (LGeomUtils.isEqual(_height.get(), os.height().get(), _precision))) {
            return 0;
        } else {
            return (int) Math.ceil((_width.get() * _height.get()) - (os.width().get() * os.height().get()));
        }
    }

    @Override
    public void parseXml(Node n, LXmlParseInfo xmlParseInfo) throws LParseException {
        if (n.hasAttributes()) {
            LXmlUtils.parseXml(this, n, xmlParseInfo);
        } else {
            double[] coord = LXmlUtils.xmlStrToDoubleArray(new StringBuilder(n.getTextContent()), 2);
            this.width(coord[0]).height(coord[1]);
        }
    }

    @Override
    public void toXml(Document doc, Element node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
