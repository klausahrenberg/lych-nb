package com.ka.lych.geometry;

import com.ka.lych.annotation.Json;
import com.ka.lych.exception.LException;
import com.ka.lych.observable.LDouble;
import com.ka.lych.util.ILParseable;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LReflections;
import com.ka.lych.xml.LXmlUtils;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LBounds<T extends LBounds> extends LSize<T>
        implements ILBounds<T> {

    @Json
    LDouble _x = new LDouble(0.0);
    @Json
    LDouble _y = new LDouble(0.0);

    public LBounds() {
    }

    public LBounds(double x, double y, double width, double height) {
        super(width, height);
        x(x).y(y);
        _x.addListener(c -> _notifyChangeListener());
        _y.addListener(c -> _notifyChangeListener());
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
    public boolean intersects(ILBounds anotherBounds) {
        return intersects(this, anotherBounds);
    }     

    public static boolean intersects(ILBounds b1, ILBounds b2) {
        if ((b1.isEmpty()) || (b2.isEmpty())) {
            return false;
        }
        return (b2.x().get() + b2.width().get() > b1.x().get()
                && b2.y().get() + b2.height().get() > b1.y().get()
                && b2.x().get() < b1.x().get() + b1.width().get()
                && b2.y().get() < b1.y().get() + b1.height().get());
    }   

    @Override
    public ILBounds createIntersection(ILBounds anotherBounds) {
        try {
            ILBounds dest = (ILBounds) LReflections.newInstance(getClass());
            LBounds.intersect(this, anotherBounds, dest);
            return dest;
        } catch (Exception ex) {
            LLog.error(ex.getMessage(), ex);
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
        double x1 = Math.max(src1.x().get(), src2.x().get());
        double y1 = Math.max(src1.y().get(), src2.y().get());
        double x2 = Math.min(src1.x().get() + src1.width().get(),
                src2.x().get() + src2.width().get());
        double y2 = Math.min(src1.y().get() + src1.height().get(),
                src2.y().get() + src2.height().get());
        dest.bounds(x1, y1, x2 - x1, y2 - y1);
    }
    
    @Override
    public ILBounds createUnion(ILBounds anotherBounds) {
        try {
            ILBounds dest = (ILBounds) LReflections.newInstance(getClass());
            LBounds.union(this, anotherBounds, dest);
            return dest;
        } catch (Exception ex) {
            LLog.error(ex.getMessage(), ex);
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
        double x1 = Math.min(src1.x().get(), src2.x().get());
        double y1 = Math.min(src1.y().get(), src2.y().get());
        double x2 = Math.max(src1.x().get() + src1.width().get(), src2.x().get()
                + src2.width().get());
        double y2 = Math.max(src1.y().get() + src1.height().get(), src2.y().get()
                + src2.height().get());
        dest.bounds(x1, y1, x2, y2);
    }

    @Override
    public int compareTo(T os) {
        if (os == null) {
            return 1;
        }
        if ((LGeomUtils.isEqual(x().get(), os.x().get(), LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(y().get(), os.y().get(), LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(width().get(), os.width().get(), LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(height().get(), os.height().get(), LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
            return 0;
        } else {
            return (int) Math.ceil((width().get() * height().get()) - (os.width().get() * os.height().get()));
        }
    }

    @Override
    public void parse(String value) throws LException {
        double[] coord = LXmlUtils.xmlStrToDoubleArray(new StringBuilder(value), 4);
        this.bounds(coord[0], coord[1], coord[2], coord[3]);
    }

    @Override
    public String toParseableString() {
        return LXmlUtils.boundsToXmlStr(this);
    }

    @Override
    public LSize clone() {
        LBounds clone = (LBounds) super.clone();
        clone.x(_x.get());
        clone.y(_y.get());
        return clone;
    }
    
    @Override
    public String toString() {
        return this.getClass().getName() + " [" + _x + ", " + _y + ", " + width() + ", " + height() + "]";
    }

}
