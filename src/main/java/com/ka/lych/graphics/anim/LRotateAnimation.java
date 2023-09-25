package com.ka.lych.graphics.anim;

import com.ka.lych.annotation.Json;
import com.ka.lych.graphics.LCanvasRenderer;
import com.ka.lych.graphics.LMatrix;
import com.ka.lych.graphics.LShape;
import com.ka.lych.observable.LDouble;
import com.ka.lych.util.LLog;

/**
 *
 * @author klausahrenberg
 */
public class LRotateAnimation extends LAnimation {

    private final Double DEFAULT_FROM_ANGLE = 0.0;
    private final Double DEFAULT_TOANGLE = 360.0;
    private final Double DEFAULT_CX = 0.0;
    private final Double DEFAULT_CY = 0.0;
    @Json
    protected LDouble fromAngle;
    @Json
    protected LDouble toAngle, cx, cy;
    protected LMatrix matrix;

    public LRotateAnimation() {
        this.matrix = new LMatrix();
    }

    @Override
    public void execute(LCanvasRenderer canvasRenderer, LShape shape, long now) {
        
        LLog.test("rotate %s / delay %s / duration %s", now, delay(), duration() );
        if ((now >= delay()) && (now < delay() + duration())) {
            double angle = getFromAngle() + (getToAngle() - getFromAngle()) * (now - delay()) / duration();            
            
            
            matrix.rotate(Math.toRadians(angle), getCx(), getCy());
            canvasRenderer.setMatrix(matrix, true);
            matrix.rotate(-Math.toRadians(angle), getCx(), getCy());
        }
    }   

    public LDouble fromAngle() {
        if (fromAngle == null) {
            fromAngle = new LDouble(DEFAULT_FROM_ANGLE);
        }
        return fromAngle;
    }

    public Double getFromAngle() {
        return fromAngle != null ? fromAngle.get() : DEFAULT_FROM_ANGLE;
    }

    public void setFromAngle(Double fromAngle) {
        fromAngle().set(fromAngle);
    }

    public LDouble toAngle() {
        if (toAngle == null) {
            toAngle = new LDouble(DEFAULT_TOANGLE);
        }
        return toAngle;
    }

    public Double getToAngle() {
        return toAngle != null ? toAngle.get() : DEFAULT_TOANGLE;
    }

    public void setToAngle(Double toAngle) {
        toAngle().set(toAngle);
    }

    public LDouble cx() {
        if (cx == null) {
            cx = new LDouble(DEFAULT_CX);
        }
        return cx;
    }

    public Double getCx() {
        return cx != null ? cx.get() : DEFAULT_CX;
    }

    public void setCx(Double cx) {
        cx().set(cx);
    }

    public LDouble cy() {
        if (cy == null) {
            cy = new LDouble(DEFAULT_CY);
        }
        return cy;
    }

    public Double getCy() {
        return cy != null ? cy.get() : DEFAULT_CY;
    }

    public void setCy(Double cy) {
        cy().set(cy);
    }

}
