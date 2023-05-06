package com.ka.lych.ui;

import com.ka.lych.observable.LPixel;
import com.ka.lych.annotation.Xml;

/**
 *
 * @author klausahrenberg
 * @param <V> the view that calls the renderer
 * @param <C> the component that have to be updated
 * @param <T> the item that have to be rendered
 */
public interface ILCellRenderer<V, C, T> {
    
    public C getRenderer(V view, T item, boolean isEnabled, boolean isSelected); 
    
    public void setListView(ILListView listView);
    
    public LPixel observableIconWidth();
    
    public double getIconWidth();
    
    @Xml
    public void setIconWidth(double iconWidth);
    
    public LPixel observableIconHeight();
    
    public double getIconHeight();
    
    @Xml
    public void setIconHeight(double iconHeight);
    
}
