package com.ka.lych.graphics;

import com.ka.lych.geometry.ILBounds;

/**
 *
 * @author klausahrenberg
 */
public class LDrawUtils {
    
    public static double getX(ILBounds bounds, double percent) {
        return percent * bounds.width().get() + bounds.getX();
    }
    
    public static double getY(ILBounds bounds, double percent) {
        return percent * bounds.height().get() + bounds.getY();
    }
    
    public static double getWidth(ILBounds bounds, double percent) {
        return percent * bounds.width().get();
    }
    
    public static double getHeight(ILBounds bounds, double percent) {
        return percent * bounds.height().get();
    }
    
    public static LColor mixColor(LColor c1, LColor c2) {
        return new LColor((c1.red()+c2.red())/2,
                          (c1.green()+c2.green())/2,
                          (c1.blue()+c2.blue())/2);        
    }
    
    public static LColor fadeColor(LColor c, float fade) {        
        int offset = Math.round(255 * fade - 255);
        int r = c.red() + offset;
        int g = c.green() + offset;
        int b = c.blue() + offset;
        r = (r > 255 ? 255 : r);
        g = (g > 255 ? 255 : g);
        b = (b > 255 ? 255 : b);
        r = (r < 0 ? 0 : r);
        g = (g < 0 ? 0 : g);
        b = (b < 0 ? 0 : b);
        return new LColor(r, g, b);   
    }
    
}
