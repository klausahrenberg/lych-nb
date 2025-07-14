package com.ka.lych.ui;

import com.ka.lych.graphics.LCanvas;
import com.ka.lych.graphics.LCanvasRenderer;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public interface ILCanvasPane<T extends LCanvas> extends ILControl {
    
    public T getCanvas();
    
    public void setCanvas(T canvas);
    
    public LCanvasRenderer getCanvasRenderer();
    
}
