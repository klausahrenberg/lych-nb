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
        implements ILPoint, ILBounds {

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

    public double getCenterX() {
        return getX() + getWidth() / 2.0;
    }
    
    public double getCenterY() {
        return getY() + getHeight() / 2.0;
    }
    
    @Override
    public void setBounds(double x, double y, double width, double height) {
        if ((notifyAllowed) && (changeListener != null)
                && ((LGeomUtils.isNotEqual(x, this.x, precision))
                || (LGeomUtils.isNotEqual(y, this.y, precision))
                || (LGeomUtils.isNotEqual(width, this.getWidth(), precision))
                || (LGeomUtils.isNotEqual(height, this.getHeight(), precision)))) {
            double oldValue = this.x;
            this.x = x;
            this.y = y;
            notifyAllowed = false;
            setSize(width, height);
            notifyAllowed = true;
            synchronized(changeListener) {
                changeListener.changed(null);
            }    
        } else {
            this.x = x;
            this.y = y;
            setSize(width, height);
        }
    }
    
    @Override
    public boolean intersects(ILBounds anotherBounds) {
        return intersects(this, anotherBounds);
    }     

    public static boolean intersects(ILBounds b1, ILBounds b2) {
        if ((b1.isEmpty()) || (b2.isEmpty())) {
            return false;
        }
        return (b2.getX() + b2.getWidth() > b1.getX()
                && b2.getY() + b2.getHeight() > b1.getY()
                && b2.getX() < b1.getX() + b1.getWidth()
                && b2.getY() < b1.getY() + b1.getHeight());
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
        double x2 = Math.min(src1.getX() + src1.getWidth(),
                src2.getX() + src2.getWidth());
        double y2 = Math.min(src1.getY() + src1.getHeight(),
                src2.getY() + src2.getHeight());
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
        double x2 = Math.max(src1.getX() + src1.getWidth(), src2.getX()
                + src2.getWidth());
        double y2 = Math.max(src1.getY() + src1.getHeight(), src2.getY()
                + src2.getHeight());
        dest.setBounds(x1, y1, x2, y2);
    }

    @Override
    public int compareTo(T os) {
        if (os == null) {
            return 1;
        }
        if ((LGeomUtils.isEqual(getX(), os.getX(), LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(getY(), os.getY(), LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(getWidth(), os.getWidth(), LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(getHeight(), os.getHeight(), LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
            return 0;
        } else {
            return (int) Math.ceil((getWidth() * getHeight()) - (os.getWidth() * os.getHeight()));
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
        return this.getClass().getName() + " [" + x + ", " + y + ", " + getWidth() + ", " + getHeight() + "]";
    }

}
