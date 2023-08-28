package com.ka.lych.geometry;

/**
 *
 * @author klausahrenberg
 * @param <BC>
 */
public interface ILBounds<BC extends ILBounds> extends ILSize<BC>, ILPoint<BC> {
        
    public default BC bounds(double x, double y, double width, double height) {        
        x(x).y(y);
        width(width).height(height);        
        return (BC) this;
    }
    
    public default BC bounds(ILBounds bounds) {        
        x(bounds.x().get()).y(bounds.y().get());
        width(bounds.width().get()).height(bounds.height().get());        
        return (BC) this;
    }
    
    public default double centerX() {
        return x().get() + width().get() / 2.0;
    }
    
    public default double centerY() {
        return y().get() + height().get() / 2.0;
    }
    
    public default int widthIntValue() {
        return (int) Math.round(width().get());
    }
    
    public default int widthIntCeil() {
        return (int) Math.ceil(width().get());
    }

    public default int heightIntValue() {
        return (int) Math.round(height().get());
    }

    public default int heightIntCeil() {
        return (int) Math.ceil(height().get());
    }

    public default int widthIntFloor() {
        return (int) Math.floor(width().get());
    }

    public default int heightIntFloor() {
        return (int) Math.floor(height().get());
    }
    
    public boolean intersects(ILBounds anotherBounds);
    
    public ILBounds createUnion(ILBounds anotherBounds);
    
    public ILBounds createIntersection(ILBounds anotherBounds);

}
