package com.ka.lych.geometry;

import com.ka.lych.annotation.Json;
import com.ka.lych.exception.LException;
import com.ka.lych.observable.ILChangeListener;
import com.ka.lych.observable.ILObservable;
import com.ka.lych.observable.ILValidator;
import com.ka.lych.util.ILCloneable;
import com.ka.lych.xml.LXmlUtils;
import com.ka.lych.observable.LDouble;
import com.ka.lych.util.ILParseable;
import com.ka.lych.util.ILRegistration;
import com.ka.lych.util.LReflections;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LSize<T extends LSize> 
        implements ILSize<T>, ILCloneable, Comparable<T>, ILObservable<LSize<T>, LSize>, ILParseable {

    @Json
    LDouble _width = new LDouble();
    @Json
    LDouble _height = new LDouble();
    double _precision = LGeomUtils.DEFAULT_DOUBLE_PRECISION;
    boolean _notifyAllowed = true;
    ILChangeListener<LSize<T>, LSize> _changeListener;

    public LSize() {
        this(0, 0);
    }

    public LSize(double width, double height) {
        width(width).height(height);
        _width.addListener(c -> _notifyChangeListener());
        _height.addListener(c -> _notifyChangeListener());
    }
    
    protected void _notifyChangeListener() {
        if (_changeListener != null) {
            _changeListener.changed(null);
        }
    }
    
    @Override
    public LDouble width() {
        return _width;
    }
        
    @Override
    public LDouble height() {
        return _height;
    }
    
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
    public LSize clone() {
        try {
            LSize p = (LSize) LReflections.newInstance(getClass());            
            p.width(_width.get());
            p.height(_height.get());
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
    public void parse(String value) throws LException {
        double[] coord = LXmlUtils.xmlStrToDoubleArray(new StringBuilder(value), 2);
        this.width(coord[0]).height(coord[1]);
    }

    @Override
    public String toParseableString() {
        return LXmlUtils.sizeToXmlStr(this);
    }

    @Override
    public ILRegistration addListener(ILChangeListener<LSize<T>, LSize> changeListener) {
        if (_changeListener != null) {
            throw new IllegalStateException("Listener is already available");
        }
        _changeListener = changeListener;
        return () -> removeListener(_changeListener);
    }

    @Override
    public void removeListener(ILChangeListener<LSize<T>, LSize> changeListener) {
        if (_changeListener != changeListener) {
            throw new IllegalStateException("Current defined listener is another one");
        }
        _changeListener = null;
    }

    @Override
    public ILRegistration addAcceptor(ILValidator<LSize<T>, LSize> valueAcceptor) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void removeAcceptor(ILValidator<LSize<T>, LSize> valueAcceptor) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
