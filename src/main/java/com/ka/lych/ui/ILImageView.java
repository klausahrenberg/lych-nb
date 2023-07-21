package com.ka.lych.ui;

import com.ka.lych.graphics.LRasterImage;
import com.ka.lych.observable.LObservable;

/**
 *
 * @author klausahrenberg
 */
public interface ILImageView extends ILControl, ILSupportsObservables<LRasterImage> {
    
    public LObservable<LRasterImage> observableRasterImage();
    
    public LRasterImage getRasterImage();
    
    public void setRasterImage(LRasterImage rasterImage);  
    
    public String getUrl();
    
    public void setUrl(String url);
    
}
