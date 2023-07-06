package com.ka.lych.graphics.anim;

import com.ka.lych.annotation.Json;
import com.ka.lych.graphics.LCanvasRenderer;
import com.ka.lych.graphics.LMatrix;
import com.ka.lych.graphics.LShape;
import com.ka.lych.observable.LDouble;

/**
 *
 * @author klausahrenberg
 */
public class LTranslateAnimation extends LAnimation {

    private final Double DEFAULT_FROM_X = 0.0;
    private final Double DEFAULT_FROM_Y = 0.0;
    private final Double DEFAULT_TO_X = 0.0;
    private final Double DEFAULT_TO_Y = 0.0;
    @Json
    protected LDouble fromX;
    @Json
    protected LDouble toX, fromY, toY;
    protected LMatrix matrix;
    
    public LTranslateAnimation() {
        this.matrix = new LMatrix();
    }
    
    public LTranslateAnimation(double fromX, double toX, double fromY, double toY) {
        setFromX(fromX);
        setToX(toX);
        setFromY(fromY);
        setToY(toY);
        this.matrix = new LMatrix();
    }

    @Override
    public void execute(LCanvasRenderer canvasRenderer, LShape shape, long now) {
        if ((now >= getDelay()) && (now < getDelay() + getDuration())) {
            double deltaX = getFromX() + (getToX() - getFromX()) * (now - getDelay()) / getDuration();
            double deltaY = getFromY() + (getToY() - getFromY()) * (now - getDelay()) / getDuration();            
            matrix.setToTranslation(deltaX, deltaY);
            canvasRenderer.setMatrix(matrix, true);
        }
    }

    public LDouble fromX() {
        if (fromX == null) {
            fromX = new LDouble(DEFAULT_FROM_X);
        }
        return fromX;
    }

    public Double getFromX() {
        return fromX != null ? fromX.get() : DEFAULT_FROM_X;
    }

    public void setFromX(Double fromX) {
        fromX().set(fromX);
    }

    public LDouble toX() {
        if (toX == null) {
            toX = new LDouble(DEFAULT_TO_X);
        }
        return toX;
    }

    public Double getToX() {
        return toX != null ? toX.get() : DEFAULT_TO_X;
    }

    public void setToX(Double toX) {
        toX().set(toX);
    }

    public LDouble fromY() {
        if (fromY == null) {
            fromY = new LDouble(DEFAULT_FROM_Y);
        }
        return fromY;
    }

    public Double getFromY() {
        return fromY != null ? fromY.get() : DEFAULT_FROM_Y;
    }

    public void setFromY(Double fromY) {
        fromY().set(fromY);
    }

    public LDouble toY() {
        if (toY == null) {
            toY = new LDouble(DEFAULT_TO_Y);
        }
        return toY;
    }

    public Double getToY() {
        return toY != null ? toY.get() : DEFAULT_TO_Y;
    }

    public void setToY(Double toY) {
        toY().set(toY);
    }
    
}
