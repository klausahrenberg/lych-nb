package com.ka.lych.geometry;

import com.ka.lych.annotation.Json;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LParseException;
import com.ka.lych.util.LReflections;
import com.ka.lych.xml.LXmlUtils;
import com.ka.lych.xml.LXmlUtils.LXmlParseInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LBounds<T extends ILBounds> extends LSize<T>
        implements ILPoint, ILBounds<T> {

    @Json
    private double x;
    @Json
    private double y;

    public LBounds() {
    }

    public LBounds(double x, double y, double width, double height) {
        super(width, height);
        this.x = x;
        this.y = y;
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
        /*if ((_notifyAllowed) && (_changeListener != null) && (LGeomUtils.isNotEqual(x, this.x, _precision))) {
            double oldValue = this.x;
            this.x = x;
            synchronized(_changeListener) {
                _changeListener.changed(null);
            }
        } else {*/
            this.x = x;
        //}
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
        /*if ((_notifyAllowed) && (_changeListener != null) && (LGeomUtils.isNotEqual(y, this.y, _precision))) {
            double oldValue = this.y;
            this.y = y;
            synchronized(_changeListener) {
                _changeListener.changed(null);
            }
        } else {*/
            this.y = y;
        //}
    }

    public double getCenterX() {
        return getX() + width().get() / 2.0;
    }
    
    public double getCenterY() {
        return getY() + height().get() / 2.0;
    }
    
    @Override
    public void setBounds(double x, double y, double width, double height) {
        /*if ((_notifyAllowed) && (_changeListener != null)
                && ((LGeomUtils.isNotEqual(x, this.x, _precision))
                || (LGeomUtils.isNotEqual(y, this.y, _precision))
                || (LGeomUtils.isNotEqual(width, this.width(), _precision))
                || (LGeomUtils.isNotEqual(height, this.height(), _precision)))) {
            double oldValue = this.x;
            this.x = x;
            this.y = y;
            _notifyAllowed = false;
            setSize(width, height);
            _notifyAllowed = true;
            synchronized(_changeListener) {
                _changeListener.changed(null);
            }    
        } else {*/
            this.x = x;
            this.y = y;
            width(width).height(height);
        //}
    }
    
    @Override
    public boolean intersects(ILBounds anotherBounds) {
        return intersects(this, anotherBounds);
    }     

    public static boolean intersects(ILBounds b1, ILBounds b2) {
        if ((b1.isEmpty()) || (b2.isEmpty())) {
            return false;
        }
        return (b2.getX() + b2.width().get() > b1.getX()
                && b2.getY() + b2.height().get() > b1.getY()
                && b2.getX() < b1.getX() + b1.width().get()
                && b2.getY() < b1.getY() + b1.height().get());
    }   

    @Override
    public ILBounds createIntersection(ILBounds anotherBounds) {
        try {
            ILBounds dest = (ILBounds) LReflections.newInstance(getClass());
            LBounds.intersect(this, anotherBounds, dest);
            return dest;
        } catch (Exception ex) {
            LLog.error(this, ex.getMessage(), ex);
            return null;
        }
    }        
    
    public static void intersect(ILBounds src1, ILBounds src2, ILBounds dest) {
        if (src1 == null) {
            throw new IllegalArgumentException("src1 can't be null.");
        }
        if (src2 == null) {
            throw new IllegalArgumentException("src2 can't be null.");
        }
        if (dest == null) {
            throw new IllegalArgumentException("dest can't be null.");
        }
        double x1 = Math.max(src1.getX(), src2.getX());
        double y1 = Math.max(src1.getY(), src2.getY());
        double x2 = Math.min(src1.getX() + src1.width().get(),
                src2.getX() + src2.width().get());
        double y2 = Math.min(src1.getY() + src1.height().get(),
                src2.getY() + src2.height().get());
        dest.setBounds(x1, y1, x2 - x1, y2 - y1);
    }
    
    @Override
    public ILBounds createUnion(ILBounds anotherBounds) {
        try {
            ILBounds dest = (ILBounds) LReflections.newInstance(getClass());
            LBounds.union(this, anotherBounds, dest);
            return dest;
        } catch (Exception ex) {
            LLog.error(this, ex.getMessage(), ex);
            return null;
        }
    }
    
    public static void union(ILBounds src1, ILBounds src2, ILBounds dest) {
        if (src1 == null) {
            throw new IllegalArgumentException("src1 can't be null.");
        }
        if (src2 == null) {
            throw new IllegalArgumentException("src2 can't be null.");
        }
        if (dest == null) {
            throw new IllegalArgumentException("dest can't be null.");
        }
        double x1 = Math.min(src1.getX(), src2.getX());
        double y1 = Math.min(src1.getY(), src2.getY());
        double x2 = Math.max(src1.getX() + src1.width().get(), src2.getX()
                + src2.width().get());
        double y2 = Math.max(src1.getY() + src1.height().get(), src2.getY()
                + src2.height().get());
        dest.setBounds(x1, y1, x2, y2);
    }

    @Override
    public int compareTo(T os) {
        if (os == null) {
            return 1;
        }
        if ((LGeomUtils.isEqual(getX(), os.getX(), LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(getY(), os.getY(), LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(width().get(), os.width().get(), LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(height().get(), os.height().get(), LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
            return 0;
        } else {
            return (int) Math.ceil((width().get() * height().get()) - (os.width().get() * os.height().get()));
        }
    }
    
    @Override
    public void parseXml(Node n, LXmlParseInfo xmlParseInfo) throws LParseException {        
        if (n.hasAttributes()) {            
            LXmlUtils.parseXml(this, n, xmlParseInfo);
        } else {
            double[] coord = LXmlUtils.xmlStrToDoubleArray(new StringBuilder(n.getTextContent()), 4);
            this.setBounds(coord[0], coord[1], coord[2], coord[3]);
        }
    }

    @Override
    public void toXml(Document doc, Element node) {
        throw new UnsupportedOperationException("toXml: Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object clone() {
        LBounds clone = (LBounds) super.clone();
        clone.x = x;
        clone.y = y;
        return clone;
    }
    
    @Override
    public String toString() {
        return this.getClass().getName() + " [" + x + ", " + y + ", " + width() + ", " + height() + "]";
    }

}
