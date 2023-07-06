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
import com.ka.lych.util.LReflections;
import com.ka.lych.xml.LXmlUtils.LXmlParseInfo;
import com.ka.lych.util.ILRegistration;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LSize<T extends ILSize> 
        implements ILSize, ILCloneable, Comparable<T>, ILObservable<LSize<T>>, ILXmlSupport {

    @Json
    private double width;
    @Json
    private double height;
    protected double precision;
    protected boolean notifyAllowed;
    protected ILChangeListener<LSize<T>> changeListener;

    public LSize() {
        this(0, 0);
    }

    public LSize(double width, double height) {
        this.width = width;
        this.height = height;
        this.notifyAllowed = true;
        precision = LGeomUtils.DEFAULT_DOUBLE_PRECISION;
    }

    @Override
    public ILRegistration addListener(ILChangeListener<LSize<T>> changeListener) {
        if (this.changeListener != null) {
            throw new IllegalStateException("Listener is already available");
        }
        this.changeListener = changeListener;
        return () -> removeListener(changeListener);
    }

    @Override
    public void removeListener(ILChangeListener<LSize<T>> changeListener) {
        if (this.changeListener != this.changeListener) {
            throw new IllegalStateException("Current defined listener is another one");
        }
        this.changeListener = null;
    }

    
    /*@Override
    public void addListener(_ILObservableListener<LSize<T>> changeListener) {
        if (this.changeListener != null) {
            throw new IllegalStateException("Listener is already available");
        }
        this.changeListener = changeListener;
    }

    @Override
    public void removeListener(_ILObservableListener<LSize<T>> changeListener) {
        if (this.changeListener != changeListener) {
            throw new IllegalStateException("Current defined changeListener is another one");
        }
        this.changeListener = null;
    }*/

    @Override
    public double getWidth() {
        return width;
    }
    
    public int getWidthIntValue() {
        return (int) Math.round(getWidth());
    }
    
    public int getWidthIntCeil() {
        return (int) Math.ceil(getWidth());
    }

    @Override
    public void setWidth(double width) {
        if ((notifyAllowed) && (changeListener != null) && (LGeomUtils.isNotEqual(width, this.width, precision))) {
            double oldValue = this.width;
            this.width = width;
            synchronized(changeListener) {
                changeListener.changed(null);
            }
        } else {
            this.width = width;
        }
    }

    @Override
    public double getHeight() {
        return height;
    }
    
    public int getHeightIntValue() {
        return (int) Math.round(getHeight());
    }   

    public int getHeightIntCeil() {
        return (int) Math.ceil(getHeight());
    }

    @Override
    public void setHeight(double height) {
        if ((notifyAllowed) && (changeListener != null) && (LGeomUtils.isNotEqual(height, this.height, precision))) {
            double oldValue = this.height;
            this.height = height;
            synchronized(changeListener) {
                changeListener.changed(null);
            }    
        } else {
            this.height = height;
        }
    }
    
    public void setSize(double width, double height) {
        if ((notifyAllowed) && (changeListener != null)
                && ((LGeomUtils.isNotEqual(width, this.width, precision))
                || (LGeomUtils.isNotEqual(height, this.height, precision)))) {
            double oldValue = this.width;
            this.width = width;
            this.height = height;
            synchronized(changeListener) {
                changeListener.changed(null);
            }    
        } else {
            this.width = width;
            this.height = height;
        }
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }
    
    @Override
    public boolean isEmpty() {
        return ((getWidth() <= 0.0) || (getHeight() <= 0.0));
    }

    @Override
    public Object clone() {
        try {
            LSize p = (LSize) LReflections.newInstance(getClass());            
            p.width = width;
            p.height = height;
            p.precision = precision;
            return p;
        } catch (Exception ex) {
            throw new InternalError(ex);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " [" + width + ", " + height + "]";
    }

    @Override
    public int compareTo(T os) {
        if (os == null) {
            return 1;
        }
        if ((LGeomUtils.isEqual(width, os.getWidth(), precision))
                && (LGeomUtils.isEqual(height, os.getHeight(), precision))) {
            return 0;
        } else {
            return (int) Math.ceil((width * height) - (os.getWidth() * os.getHeight()));
        }
    }

    @Override
    public void parseXml(Node n, LXmlParseInfo xmlParseInfo) throws LParseException {
        if (n.hasAttributes()) {
            LXmlUtils.parseXml(this, n, xmlParseInfo);
        } else {
            double[] coord = LXmlUtils.xmlStrToDoubleArray(new StringBuilder(n.getTextContent()), 2);
            this.setSize(coord[0], coord[1]);
        }
    }

    @Override
    public void toXml(Document doc, Element node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ILRegistration addAcceptor(ILValidator<LSize<T>> valueAcceptor) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeAcceptor(ILValidator<LSize<T>> valueAcceptor) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
