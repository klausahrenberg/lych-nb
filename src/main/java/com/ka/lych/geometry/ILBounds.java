package com.ka.lych.geometry;

/**
 *
 * @author klausahrenberg
 * @param <BC>
 */
public interface ILBounds<BC extends ILBounds> extends ILSize<BC> {
    
    public double getX();

    public void setX(double x);
    
    public double getY();

    public void setY(double y);
    
    public void setBounds(double x, double y, double width, double height);
    
    public boolean intersects(ILBounds anotherBounds);
    
    public ILBounds createUnion(ILBounds anotherBounds);
    
    public ILBounds createIntersection(ILBounds anotherBounds);

}
