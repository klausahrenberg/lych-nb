package com.ka.lych.ui;

import com.ka.lych.observable.LPixel;

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
    
    public void setIconWidth(double iconWidth);
    
    public LPixel observableIconHeight();
    
    public double getIconHeight();
    
    public void setIconHeight(double iconHeight);
    
}
