package com.ka.lych.graphics;

import com.ka.lych.annotation.Json;
import com.ka.lych.list.LList;
import java.util.Objects;
import com.ka.lych.observable.*;
import com.ka.lych.util.LLog;
import java.util.List;

/**
 *
 * @author klausahrenberg
 *
 * fxml example:
 * 
 * <LRadialGradient style="fill" spreadMethod="pad" cx="7" cy="7" r="7" fx="7" fy="0" >    
 *     <stops>         
 *         <LStop offset="0.0" color="red" />
 *         <LStop offset="1.0" color="blue" />
 *     </stops>
 * </LRadialGradient>
 * 
 */
public class LRadialGradient extends LAbstractPaint<LRadialGradient> {

    @Json
    protected LDouble cx;

    @Json
    protected LDouble cy, r, fx, fy;
    @Json
    protected LObject<LSpreadMethod> spreadMethod;
    @Json
    protected LObject<List<LStop>> stops;

    public LRadialGradient() {
    }

    public final LDouble cx() {
        if (cx == null) {
            cx = new LDouble();
        }
        return cx;
    }

    public final double getCx() {
        return (cx != null ? cx.get() : 0.0);
    }

    public final void setCx(double cx) {
        cx().set(cx);
    }

    public final LDouble cy() {
        if (cy == null) {
            cy = new LDouble();
        }
        return cy;
    }

    public final double getCy() {
        return (cy != null ? cy.get() : 0.0);
    }

    public final void setCy(double cy) {
        cy().set(cy);
    }

    public final LDouble r() {
        if (r == null) {
            r = new LDouble();
        }
        return r;
    }

    public final double getR() {
        return (r != null ? r.get() : 0.0);
    }

    public final void setR(double r) {
        r().set(r);
    }

    public final LDouble fx() {
        if (fx == null) {
            fx = new LDouble();
        }
        return fx;
    }

    public final Double getFx() {
        return (fx != null ? fx.get() : null);
    }

    public final void setFx(Double fx) {
        fx().set(fx);
    }

    public final LDouble fy() {
        if (fy == null) {
            fy = new LDouble();
        }
        return fy;
    }

    public final Double getFy() {
        return (fy != null ? fy.get() : null);
    }

    public final void setFy(Double y2) {
        fy().set(y2);
    }

    public LObject<LSpreadMethod> spreadMethod() {
        if (spreadMethod == null) {
            spreadMethod = new LObject<>(LSpreadMethod.PAD);
        }
        return spreadMethod;
    }

    public LSpreadMethod getSpreadMethod() {
        return (spreadMethod != null ? spreadMethod.get() : LSpreadMethod.PAD);
    }

    public void setSpreadMethod(LSpreadMethod spreadMethod) {
        spreadMethod().set(spreadMethod);
    }

    public LObject<List<LStop>> stops() {
        if (stops == null) {
            stops = new LObject<>(new LList<>());
        }
        return stops;
    }

    public List<LStop> getStops() {
        return (stops != null ? stops.get() : null);
    }

    public void setStops(List<LStop> stops) {
        stops().set(stops);
    }

    public double getFocusAngle() {
        double focusAngle = 0;
        if ((getFx() != null) || (getFy() != null)) {
            double fx = (getFx() != null ? getFx() : getCx());
            double fy = (getFy() != null ? getFy() : getCy());
            focusAngle = Math.toDegrees(Math.atan2(fy - getCy(), fx - getCx()));
            if (focusAngle < 0) {
                focusAngle += 360;
            }
        }
        return focusAngle;
    }

    public double getFocusDistance() {
        if ((getFx() != null) || (getFy() != null)) {
            double fx = (getFx() != null ? getFx() : getCx());
            double fy = (getFy() != null ? getFy() : getCy());
            return Math.sqrt(Math.pow((fx - getCx()), 2) + Math.pow((fy - getCy()), 2)) / getR();
        } else {
            return 0;
        }
    }

    @Override
    public Object clone() {
        try {
            LRadialGradient p = (LRadialGradient) super.clone();
            p.cx = this.cx.clone();
            p.cy = this.cy.clone();
            p.fx = this.fx.clone();
            p.r = this.r.clone();
            p.spreadMethod = this.spreadMethod.clone();
            p.stops = this.stops.clone();
            return p;
        } catch (Exception e) {
            LLog.error("clone failed", e);
            throw new InternalError();
        }
    }

    @Override
    public boolean equals(Object st) {
        return ((st != null) && (st instanceof LRadialGradient) ? this.hashCode() == st.hashCode() : false);
    }

    @Override
    public int hashCode() {
        //cx, cy, r, fx, fy;
        //spreadMethod;
        //<LYosos<LStop>> stops;
        return Objects.hash(style(), getCx(), getCy(), getR(), getFx(), getFy(), getSpreadMethod(), getStops());
    }

}
