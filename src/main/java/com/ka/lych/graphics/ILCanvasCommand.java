package com.ka.lych.graphics;

/**
 *
 * @author klausahrenberg
 * @param <C> native Canvas class
 * @param <D> native Color class
 */
public interface ILCanvasCommand<C, D> {
    
    public void execute(LCanvasRenderer<C, D> canvasRenderer, long now);    
    
}
