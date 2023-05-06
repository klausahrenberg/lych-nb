package com.ka.lych.graphics;

/**
 *
 * @author klausahrenberg
 * @param <C>
 * @param <D>
 */
public abstract class LBlendMode<C, D>
        implements ILCanvasCommand<C, D> {

    public static final LBlendMode NORMAL = new LBlendMode<>(){
        @Override
        public String toString() {
            return getClass().getName() + " [NORMAL]";
        }
    };        
    public static final LBlendMode MULTIPLY = new LBlendMode<>(){
        @Override
        public String toString() {
            return getClass().getName() + " [MULTIPLY]";
        }
    };    
    public static final LBlendMode SCREEN = new LBlendMode<>(){
        @Override
        public String toString() {
            return getClass().getName() + " [SCREEN]";
        }
    };    
    public static final LBlendMode OVERLAY = new LBlendMode<>(){
        @Override
        public String toString() {
            return getClass().getName() + " [OVERLAY]";
        }
    };    
    public static final LBlendMode DARKEN = new LBlendMode<>(){
        @Override
        public String toString() {
            return getClass().getName() + " [DARKEN]";
        }
    };    
    public static final LBlendMode LIGHTEN = new LBlendMode<>(){
        @Override
        public String toString() {
            return getClass().getName() + " [LIGHTEN]";
        }
    };    
    public static final LBlendMode COLOR_DODGE = new LBlendMode<>(){
        @Override
        public String toString() {
            return getClass().getName() + " [COLOR_DODGE]";
        }
    };    
    public static final LBlendMode COLOR_BURN = new LBlendMode<>(){
        @Override
        public String toString() {
            return getClass().getName() + " [COLOR_BURN]";
        }
    };    
    public static final LBlendMode HARD_LIGHT = new LBlendMode<>(){
        @Override
        public String toString() {
            return getClass().getName() + " [HARD_LIGHT]";
        }
    };    
    public static final LBlendMode SOFT_LIGHT = new LBlendMode<>(){
        @Override
        public String toString() {
            return getClass().getName() + " [SOFT_LIGHT]";
        }
    };    
    public static final LBlendMode DIFFERENCE = new LBlendMode<>(){
        @Override
        public String toString() {
            return getClass().getName() + " [DIFFERENCE]";
        }
    };    
    public static final LBlendMode EXCLUSION = new LBlendMode<>(){
        @Override
        public String toString() {
            return getClass().getName() + " [EXCLUSION]";
        }
    };    
    public static final LBlendMode COMPATIBLE = new LBlendMode<>(){
        @Override
        public String toString() {
            return getClass().getName() + " [COMPATIBLE]";
        }
    };    
    public static final LBlendMode HUE = new LBlendMode<>(){
        @Override
        public String toString() {
            return getClass().getName() + " [HUE]";
        }
    };    
    public static final LBlendMode SATURATION = new LBlendMode<>(){
        @Override
        public String toString() {
            return getClass().getName() + " [SATURATION]";
        }
    };    
    public static final LBlendMode COLOR = new LBlendMode<>(){
        @Override
        public String toString() {
            return getClass().getName() + " [COLOR]";
        }
    };    
    public static final LBlendMode LUMINOSITY = new LBlendMode<>(){
        @Override
        public String toString() {
            return getClass().getName() + " [LUMINOSITY]";
        }
    };    
    
    @Override
    public void execute(LCanvasRenderer<C, D> canvasRenderer, long now) {
        canvasRenderer.setBlendMode(this);
    }    
    
}
