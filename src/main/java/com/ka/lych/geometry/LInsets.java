package com.ka.lych.geometry;

/**
 *
 * @author klausahrenberg
 */
public class LInsets 
        implements Comparable<LInsets> {

    protected double left, top, right, bottom;
    
    public LInsets() {
        this(0, 0, 0, 0);
    }

    public LInsets(LInsets i) {
        this(i.left, i.top, i.right, i.bottom);
    }

    public LInsets(double left, double top, double right, double bottom) {
    //public LInsets(double left, double top, double right, double bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public int getLeftIntValue() {
        return (int) Math.round(left);
    }
    
    public int getRightIntValue() {
        return (int) Math.round(right);
    }

    public int getTopIntValue() {
        return (int) Math.round(top);
    }
    
    public int getBottomIntValue() {
        return (int) Math.round(bottom);
    }

    public double getBottom() {
        return bottom;
    }

    public void setBottom(double bottom) {
        this.bottom = bottom;
    }

    public double getRight() {
        return this.right;
    }

    public void setRight(double right) {
        this.right = right;
    }

    public double getLeft() {
        return left;
    }

    public void setLeft(double left) {
        this.left = left;
    }

    public double getTop() {
        return top;
    }

    public void setTop(double top) {
        this.top = top;
    }

    public void setInsets(double left, double top, double right, double bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    @Override
    public int compareTo(LInsets os) {
        if (os == null) {
            return 1;
        }
        if ((LGeomUtils.isEqual(left, os.left, LGeomUtils.DEFAULT_DOUBLE_PRECISION)) && 
            (LGeomUtils.isEqual(top, os.top, LGeomUtils.DEFAULT_DOUBLE_PRECISION)) && 
            (LGeomUtils.isEqual(right, os.right, LGeomUtils.DEFAULT_DOUBLE_PRECISION)) && 
            (LGeomUtils.isEqual(bottom, os.bottom, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {       
            return 0;
        } else {
            return (int) Math.ceil((left * top * right * bottom) - (os.left * os.top * os.right * os.bottom));
        }
    }
    
}
