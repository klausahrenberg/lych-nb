package com.ka.lych.graphics;

import java.util.Arrays;
import java.util.EnumSet;
import com.ka.lych.geometry.ILBounds;
import com.ka.lych.geometry.LBounds;
import com.ka.lych.geometry.LGeomUtils;
import com.ka.lych.geometry.LPoint;
import com.ka.lych.graphics.anim.ILAnimation;
import com.ka.lych.list.LMap;
import com.ka.lych.list.LYosos;
import com.ka.lych.list.LYososIterator;
import com.ka.lych.observable.*;
import com.ka.lych.util.ILCloneable;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LParseException;
import com.ka.lych.xml.ILXmlSupport;
import com.ka.lych.util.LReflections;
import com.ka.lych.xml.LXmlUtils;
import com.ka.lych.xml.LXmlUtils.LXmlParseInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.ka.lych.annotation.Xml;

/**
 *
 * @author klausahrenberg
 */
public class LShape
        implements ILBounds, ILCloneable, ILXmlSupport, Comparable<LShape>, ILCanvasCommand {

    protected static EnumSet<LPaintStyle> DEFAULT_STYLE = EnumSet.of(LPaintStyle.STROKE);
    /**
     * Used constant for calculating circles with biezier curves kappe = 4 *
     * (sqrt(2) - 1) / 3
     */
    public final static double KAPPA = 0.5522847498307933;

    public static final int WIND_EVEN_ODD = 0;
    public static final int WIND_NON_ZERO = 1;
    public static final byte SEG_MOVETO = 0;
    public static final byte SEG_LINETO = 1;
    public static final byte SEG_QUADTO = 2;
    public static final byte SEG_CUBICTO = 3;
    public static final byte SEG_CLOSE = 4;
    protected static final int EXPAND_MAX = 500;
    protected static final int INITIAL_SIZE = 12;
    @Xml
    protected LString id;
    @Xml
    protected LDouble x;
    @Xml
    protected LDouble y, width, height;
    @Xml
    protected LBoolean visible;
    @Xml
    protected LObservable<EnumSet<LPaintStyle>> style;
    @Xml
    protected LBoolean rotatable;
    @Xml
    protected LObservable<LYosos<ILAnimation>> animations;
    protected LMap<String, Object> clientProperties;
    protected transient byte[] pointTypes;
    protected transient int countPoints;
    protected transient int numCoords;
    protected transient double doubleCoords[];
    protected transient int windingRule;
    protected String[] neededShapeAttributes;

    private final ILChangeListener boundsListener = oldValue -> {
        if ((x != null) && (y != null) && (width != null) && (height != null)
                && (LGeomUtils.isNotEqual(getWidth(), 0.0)) && (LGeomUtils.isNotEqual(getHeight(), 0.0))) {
            createPath();
        }
    };

    public LShape() {
        this(INITIAL_SIZE, INITIAL_SIZE * 2);
    }

    public LShape(int initialPointTypes, int initialDoubleCoords) {
        super();
        this.pointTypes = new byte[initialPointTypes];
        this.doubleCoords = new double[initialDoubleCoords];
        this.windingRule = WIND_NON_ZERO;
        neededShapeAttributes = new String[]{"d"};
    }

    public LShape(Node n, LXmlParseInfo xmlParseInfo) throws LParseException {
        this.pointTypes = new byte[INITIAL_SIZE];
        this.doubleCoords = new double[INITIAL_SIZE * 2];
        this.windingRule = WIND_NON_ZERO;
        neededShapeAttributes = new String[]{"d"};
        parseXml(n, xmlParseInfo);
    }

    public LShape(ILBounds bounds) {
        this(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    public LShape(double x, double y, double width, double height) {
        setBounds(x, y, width, height);
    }

    public LString id() {
        if (id == null) {
            id = new LString();
        }
        return id;
    }

    public String getId() {
        return (id != null ? id.get() : null);
    }

    @Xml
    public void setId(String id) {
        id().set(id);
    }

    public LBoolean visible() {
        if (visible == null) {
            visible = new LBoolean(true);
        }
        return visible;
    }

    public boolean isVisible() {
        return (visible != null ? visible.get() : true);
    }

    @Xml
    public void setVisible(boolean visible) {
        visible().set(visible);
    }

    public LObservable<EnumSet<LPaintStyle>> style() {
        if (style == null) {
            style = new LObservable<>(DEFAULT_STYLE);
        }
        return style;
    }

    public boolean isStyle(LPaintStyle style) {
        return getStyle().contains(style);
    }
    
    public EnumSet<LPaintStyle> getStyle() {
        return (style != null ? style.get() : DEFAULT_STYLE);
    }
    
    @Xml
    public void setStyle(EnumSet<LPaintStyle> style) {
        style().set(style);
    }
    
    public void addStyle(LPaintStyle style) {
        style().get().add(style);
    }  

    public LBoolean rotatable() {
        if (rotatable == null) {
            rotatable = new LBoolean(true);
        }
        return rotatable;
    }

    public boolean isRotatable() {
        return (rotatable != null ? rotatable.get() : true);
    }

    public void setRotatable(boolean rotatable) {
        rotatable().set(rotatable);
    }

    public final LObservable<LYosos<ILAnimation>> animations() {
        if (animations == null) {
            animations = new LObservable<>(new LYosos<>());
        }
        return animations;
    }

    public LYosos<ILAnimation> getAnimations() {
        return (animations != null ? animations.get() : null);
    }

    public void setAnimations(LYosos<ILAnimation> animations) {
        animations().set(animations);
    }

    public boolean hasAnimations() {
        return ((animations != null) && (animations.get().size() > 0));
    }

    public final synchronized int getWindingRule() {
        return windingRule;
    }

    public final void setWindingRule(int rule) {
        if (rule != WIND_EVEN_ODD && rule != WIND_NON_ZERO) {
            throw new IllegalArgumentException("winding rule must be WIND_EVEN_ODD or WIND_NON_ZERO");
        }
        windingRule = rule;
    }

    public final synchronized void moveTo(double x, double y) {
        if (countPoints > 0 && pointTypes[countPoints - 1] == SEG_MOVETO) {
            doubleCoords[numCoords - 2] = x;
            doubleCoords[numCoords - 1] = y;
            ensureThatPointIsInShape(true, x, y);
        } else {
            needRoom(false, 2);
            pointTypes[countPoints++] = SEG_MOVETO;
            doubleCoords[numCoords++] = x;
            doubleCoords[numCoords++] = y;
            ensureThatPointIsInShape((countPoints == 1), x, y);
        }
        
    }

    public final synchronized void lineTo(double x, double y) {
        needRoom(true, 2);
        pointTypes[countPoints++] = SEG_LINETO;
        doubleCoords[numCoords++] = x;
        doubleCoords[numCoords++] = y;
        ensureThatPointIsInShape(false, x, y);
    }

    public final synchronized void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
        needRoom(true, 6);
        pointTypes[countPoints++] = SEG_CUBICTO;
        doubleCoords[numCoords++] = x1;
        doubleCoords[numCoords++] = y1;
        doubleCoords[numCoords++] = x2;
        doubleCoords[numCoords++] = y2;
        doubleCoords[numCoords++] = x3;
        doubleCoords[numCoords++] = y3;
        ensureThatPointIsInShape(false, x1, y1);
        ensureThatPointIsInShape(false, x2, y2);
        ensureThatPointIsInShape(false, x3, y3);
    }

    public final synchronized void quadTo(double x1, double y1, double x2, double y2) {
        needRoom(true, 4);
        pointTypes[countPoints++] = SEG_QUADTO;
        doubleCoords[numCoords++] = x1;
        doubleCoords[numCoords++] = y1;
        doubleCoords[numCoords++] = x2;
        doubleCoords[numCoords++] = y2;
        ensureThatPointIsInShape(false, x1, y1);
        ensureThatPointIsInShape(false, x2, y2);
    }

    /**
     * Adds an elliptical arc, defined by two radii, an angle from the x-axis, a
     * flag to choose the large arc or not, a flag to indicate if we increase or
     * decrease the angles and the final point of the arc.
     *
     * @deprecated 2018-02-19 function doesnt work
     *
     * @param rx the x radius of the ellipse
     * @param ry the y radius of the ellipse
     *
     * @param angle the angle from the x-axis of the current coordinate system
     * to the x-axis of the ellipse in degrees.
     *
     * @param largeArcFlag the large arc flag. If true the arc spanning less
     * than or equal to 180 degrees is chosen, otherwise the arc spanning
     * greater than 180 degrees is chosen
     *
     * @param sweepFlag the sweep flag. If true the line joining center to arc
     * sweeps through decreasing angles otherwise it sweeps through increasing
     * angles
     *
     * @param x the absolute x coordinate of the final point of the arc.
     * @param y the absolute y coordinate of the final point of the arc.
     */
    @Deprecated
    public synchronized void arcTo(double rx, double ry, double angle, boolean largeArcFlag, boolean sweepFlag, double x, double y) {
        /*
        // Ensure radii are valid
        if (rx == 0 || ry == 0) {
            lineTo(x, y);
            return;
        }
        needRoom(true, 7);
        pointTypes[countPoints++] = SEG_ARCTO;
        doubleCoords[numCoords++] = rx;
        doubleCoords[numCoords++] = ry;
        doubleCoords[numCoords++] = x;
        doubleCoords[numCoords++] = y;
        doubleCoords[numCoords++] = angle;
        doubleCoords[numCoords++] = (largeArcFlag ? 1 : 0);
        doubleCoords[numCoords++] = (sweepFlag ? 1 : 0);        
        ensureThatPointIsInShape(x, y);*/

        LPoint pc = this.getCurrentPoint();
        double x0 = pc.getX();
        double y0 = pc.getY();
        // Ensure radii are valid
        if (rx == 0 || ry == 0) {
            this.lineTo((float) x, (float) y);
            return;
        }
        if (x0 == x && y0 == y) {
            // If the endpoints (x, y) and (x0, y0) are identical, then this
            // is equivalent to omitting the elliptical arc segment entirely.
            return;
        }
        // Elliptical arc implementation based on the SVG specification notes        
        // Compute the half distance between the current and the final point
        double dx2 = (x0 - x) / 2.0;
        double dy2 = (y0 - y) / 2.0;
        // Convert angle from degrees to radians
        angle = Math.toRadians(-angle % 360.0);
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);
        // Step 1 : Compute (x1, y1)
        double x1 = (cosAngle * dx2 + sinAngle * dy2);
        double y1 = (-sinAngle * dx2 + cosAngle * dy2);
        // Ensure radii are large enough
        rx = Math.abs(rx);
        ry = Math.abs(ry);
        double Prx = rx * rx;
        double Pry = ry * ry;
        double Px1 = x1 * x1;
        double Py1 = y1 * y1;
        // check that radii are large enough
        double radiiCheck = Px1 / Prx + Py1 / Pry;
        if (radiiCheck > 1) {
            rx = Math.sqrt(radiiCheck) * rx;
            ry = Math.sqrt(radiiCheck) * ry;
            Prx = rx * rx;
            Pry = ry * ry;
        }
        // Step 2 : Compute (cx1, cy1)
        double sign = (largeArcFlag == sweepFlag) ? -1 : 1;
        double sq = ((Prx * Pry) - (Prx * Py1) - (Pry * Px1)) / ((Prx * Py1) + (Pry * Px1));
        sq = (sq < 0) ? 0 : sq;
        double coef = (sign * Math.sqrt(sq));
        double cx1 = coef * ((rx * y1) / ry);
        double cy1 = coef * -((ry * x1) / rx);
        // Step 3 : Compute (cx, cy) from (cx1, cy1)
        double sx2 = (x0 + x) / 2.0;
        double sy2 = (y0 + y) / 2.0;
        double cx = sx2 + (cosAngle * cx1 - sinAngle * cy1);
        double cy = sy2 + (sinAngle * cx1 + cosAngle * cy1);
        // Step 4 : Compute the angleStart (angle1) and the angleExtent (dangle)
        double ux = (x1 - cx1) / rx;
        double uy = (y1 - cy1) / ry;
        double vx = (-x1 - cx1) / rx;
        double vy = (-y1 - cy1) / ry;
        double p, n;
        // Compute the angle start
        n = Math.sqrt((ux * ux) + (uy * uy));
        p = ux; // (1 * ux) + (0 * uy)
        sign = (uy < 0) ? -1d : 1d;
        double angleStart = Math.toDegrees(sign * Math.acos(p / n));
        // Compute the angle extent
        n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
        p = ux * vx + uy * vy;
        sign = (ux * vy - uy * vx < 0) ? -1d : 1d;
        double angleExtent = Math.toDegrees(sign * Math.acos(p / n));
        if (!sweepFlag && angleExtent > 0) {
            angleExtent -= 360f;
        } else if (sweepFlag && angleExtent < 0) {
            angleExtent += 360f;
        }
        angleExtent %= 360f;
        angleStart %= 360f;
        arcToBezier(cx, cy, rx, ry, -angleStart, -angleExtent);
    }

    private void arcToBezier(double cx, double cy, double rx, double ry, double angleStart, double angleExtent, boolean lineToStart) {
        angleStart = angleStart % 360;

        //Evaluate start point of the arc
        double startX = (rx * ry) / Math.sqrt(Math.pow(ry, 2) + Math.pow(rx, 2) * Math.pow(Math.tan(Math.toRadians(angleStart)), 2));
        double startY = (rx * ry) / Math.sqrt(Math.pow(rx, 2) + Math.pow(ry, 2) / Math.pow(Math.tan(Math.toRadians(angleStart)), 2));
        startX = ((angleStart >= 90) && (angleStart < 270) ? cx - startX : cx + startX);
        startY = ((angleStart >= 0) && (angleStart < 180) ? cy + startY : cy - startY);
        if (lineToStart) {
            this.lineTo(startX, startY);
        } else {
            this.moveTo(startX, startY);
        }
        arcToBezier(cx, cy, rx, ry, angleStart, angleExtent);
    }

    private void arcToBezier(double cx, double cy, double rx, double ry, double angleStart, double angleExtent) {
        double angStRad = Math.toRadians(angleStart);
        double ext = angleExtent;
        int arcSegs;
        double increment, cv;
        if (ext >= 360.0 || ext <= -360) {
            arcSegs = 4;
            increment = Math.PI / 2;
            // btan(Math.PI / 2);
            cv = KAPPA;
            if (ext < 0) {
                increment = -increment;
                cv = -cv;
            }
        } else {
            arcSegs = (int) Math.ceil(Math.abs(ext) / 90.0);
            increment = Math.toRadians(ext / arcSegs);
            cv = btan(increment);
            if (cv == 0) {
                arcSegs = 0;
            }
        }
        if (rx < 0 || ry < 0) {
            arcSegs = -1;
        }
        //iterate now
        int index = 0;
        while (index < arcSegs) {
            double angle = angStRad;
            angle += increment * index;
            double relx = Math.cos(angle);
            double rely = Math.sin(angle);
            double coords0 = cx + (relx - cv * rely) * rx;
            double coords1 = cy + (rely + cv * relx) * ry;
            angle += increment;
            relx = Math.cos(angle);
            rely = Math.sin(angle);
            double coords2 = cx + (relx + cv * rely) * rx;
            double coords3 = cy + (rely - cv * relx) * ry;
            double coords4 = cx + relx * rx;
            double coords5 = cy + rely * ry;
            this.curveTo(coords0, coords1, coords2, coords3, coords4, coords5);
            index++;
        }
    }

    private static double btan(double increment) {
        increment /= 2.0;
        return 4.0 / 3.0 * Math.sin(increment) / (1.0 + Math.cos(increment));
    }

    public final synchronized void closePath() {
        if (countPoints == 0 || pointTypes[countPoints - 1] != SEG_CLOSE) {
            needRoom(true, 0);
            pointTypes[countPoints++] = SEG_CLOSE;
        }
    }

    public final synchronized void reset() {
        countPoints = numCoords = 0;
        setBounds(0, 0, 0, 0);
    }

    protected void needRoom(boolean needMove, int newCoords) {
        if (needMove && countPoints == 0) {
            moveTo(0, 0);
        }
        int size = pointTypes.length;
        if (countPoints >= size) {
            int grow = size;
            if (grow > EXPAND_MAX) {
                grow = EXPAND_MAX;
            }
            pointTypes = Arrays.copyOf(pointTypes, size + grow);
        }
        size = doubleCoords.length;
        if (numCoords + newCoords > size) {
            int grow = size;
            if (grow > EXPAND_MAX * 2) {
                grow = EXPAND_MAX * 2;
            }
            if (grow < newCoords) {
                grow = newCoords;
            }
            doubleCoords = Arrays.copyOf(doubleCoords, size + grow);
        }
    }

    protected void createPath(double[] xpoints, double[] ypoints, double[] radiuses, boolean closePath) {
        countPoints = numCoords = 0;
        if ((xpoints.length > 2) && (xpoints.length == ypoints.length)) {
            LPoint[] points = new LPoint[xpoints.length];
            for (int i_curr = 0; i_curr < xpoints.length; i_curr++) {
                points[i_curr] = new LPoint(xpoints[i_curr], ypoints[i_curr]);
            }
            createPath(points, radiuses, closePath);
        } else {
            throw new IllegalStateException("Different quantity of pointy x, y, arc or less than 3 points.");
        }
    }

    protected void createPath(LPoint[] points, double[] radiuses, boolean closePath) {
        countPoints = numCoords = 0;
        if (points.length > 2) {
            boolean start = true;
            LPoint prev = new LPoint();
            LPoint next = new LPoint();
            for (int i_curr = 0; i_curr < points.length; i_curr++) {
                LPoint curr = points[i_curr];
                double radX = (radiuses != null ? (points.length * 2 == radiuses.length ? radiuses[i_curr * 2] : radiuses[i_curr]) : 0);
                double radY = (radiuses != null ? (points.length * 2 == radiuses.length ? radiuses[i_curr * 2 + 1] : radiuses[i_curr]) : 0);
                if ((radX > 0) && (radY > 0)) {                    
                    int i = (i_curr - 1 < 0 ? points.length - 1 : i_curr - 1);
                    prev.setPoint(points[i].getX(), points[i].getY());
                    i = (i_curr + 1 >= points.length ? 0 : i_curr + 1);
                    next.setPoint(points[i].getX(), points[i].getY());

                    convertCurveCoord(curr, prev, (i_curr % 2 == 0 ? radX : radY));
                    convertCurveCoord(curr, next, (i_curr % 2 == 0 ? radY : radX));

                    //Move to start Point or draw Line to next curve
                    pointTypes[countPoints++] = (start ? SEG_MOVETO : SEG_LINETO);
                    doubleCoords[numCoords++] = prev.getX();
                    doubleCoords[numCoords++] = prev.getY();
                    start = false;
                    //Draw the curve
                    pointTypes[countPoints++] = SEG_QUADTO;
                    doubleCoords[numCoords++] = curr.getX();
                    doubleCoords[numCoords++] = curr.getY();
                    doubleCoords[numCoords++] = next.getX();
                    doubleCoords[numCoords++] = next.getY();
                } else if (start) {
                    pointTypes[countPoints++] = SEG_MOVETO;
                    doubleCoords[numCoords++] = curr.getX();
                    doubleCoords[numCoords++] = curr.getY();
                    start = false;
                } else {
                    pointTypes[countPoints++] = SEG_LINETO;
                    doubleCoords[numCoords++] = curr.getX();
                    doubleCoords[numCoords++] = curr.getY();
                }
            }
            if (closePath) {
                pointTypes[countPoints++] = SEG_CLOSE;
            }
        } else {
            throw new IllegalStateException("Less than 3 points.");
        }
    }

    protected void convertCurveCoord(LPoint curr, LPoint curvCoord, double radius) {
        double dx = curvCoord.getX() - curr.getX();
        double dy = curvCoord.getY() - curr.getY();
        double dh = Math.sqrt(dx * dx + dy * dy);
        radius = (radius * 2 > dh ? dh / 2 : radius);
        curvCoord.setX(curr.getX() + dx * radius / dh);
        curvCoord.setY(curr.getY() + dy * radius / dh);
    }

    public final void append(LShape s, boolean connect) {
        append(new ShapeIterator(s), connect);
    }

    public final void append(ShapeIterator p_it, boolean connect) {
        while (!p_it.isDone()) {
            switch (p_it.getPointType()) {
                case SEG_MOVETO:
                    if (!connect || countPoints < 1 || numCoords < 1) {
                        moveTo(p_it.getPointCoordinateX(0),
                                p_it.getPointCoordinateY(0));
                        break;
                    }
                    if (pointTypes[countPoints - 1] != SEG_CLOSE
                            && doubleCoords[numCoords - 2] == p_it
                                    .getPointCoordinateX(0)
                            && doubleCoords[numCoords - 1] == p_it
                                    .getPointCoordinateY(0)) {
                        // Collapse out initial moveto/lineto
                        break;
                    }
                // NO BREAK;
                case SEG_LINETO:
                    lineTo(p_it.getPointCoordinateX(0), p_it.getPointCoordinateY(0));
                    break;
                case SEG_QUADTO:
                    quadTo(p_it.getPointCoordinateX(0),
                            p_it.getPointCoordinateY(0),
                            p_it.getPointCoordinateX(1),
                            p_it.getPointCoordinateY(1));
                    break;
                case SEG_CUBICTO:
                    curveTo(p_it.getPointCoordinateX(0),
                            p_it.getPointCoordinateY(0),
                            p_it.getPointCoordinateX(1),
                            p_it.getPointCoordinateY(1),
                            p_it.getPointCoordinateX(2),
                            p_it.getPointCoordinateY(2));
                    break;
                case SEG_CLOSE:
                    closePath();
                    break;
            }
            p_it.next();
            connect = false;
        }
    }

    private String cutString(String value, int steps) {
        for (int b = 0; b < steps; b++) {
            if (!LString.isEmpty(value)) {
                int i = value.indexOf(" ");
                if (i > -1) {
                    value = value.substring(i + 1);
                } else {
                    value = "";
                }
            } else {
                break;
            }
        }
        return value;
    }

    protected void parseXmlCommonAttributes(Node n, LXmlParseInfo xmlParseInfo) throws LParseException {
        LYosos<String> excludeList = new LYosos<>();
        for (String neededAttribute : neededShapeAttributes) {
            excludeList.add(neededAttribute);
        }
        LXmlUtils.parseXml(this, n, xmlParseInfo, excludeList);
    }

    @Override
    public void parseXml(Node n, LXmlParseInfo xmlParseInfo) throws LParseException {
        this.reset();
        this.parseXmlCommonAttributes(n, xmlParseInfo);
        //path d
        if (n != null) {
            char c = ' ';
            double[] al;
            double[] lastPoint = new double[2];
            lastPoint[0] = 0.0;
            lastPoint[1] = 0.0;
            double[] secPoint = new double[2];
            secPoint[0] = 0.0;
            secPoint[1] = 0.0;
            char lastChar = ' ';
            StringBuilder d = new StringBuilder(LXmlUtils.getAttributeString(n, "d", "").trim());
            //d = LXmlUtils.prepareString(d);
            while (d.length() > 1) {
                if (Character.isLetter(d.charAt(0))) {
                    c = d.charAt(0);
                    //d = d.substring(1).trim();
                    d.deleteCharAt(0);
                    while (d.charAt(0) == ' ') {
                        d.deleteCharAt(0);
                    }
                } else if (c == ' ') {
                    throw new IllegalArgumentException("No draw command at start of d. / Rest of string to parse: " + d);
                }

                if (Character.isUpperCase(c)) {
                    secPoint[0] = secPoint[0] - lastPoint[0];
                    secPoint[1] = secPoint[1] - lastPoint[1];
                    if (c != 'V') lastPoint[0] = 0.0;
                    if (c != 'H') lastPoint[1] = 0.0;
                }
                
                switch (Character.toUpperCase(c)) {
                    case 'M' -> {
                        lastPoint = LXmlUtils.xmlStrToDoubleArray(d, 2, lastPoint[0], lastPoint[1]);
                        moveTo(lastPoint[0], lastPoint[1]);
                    }
                    case 'L' -> {
                        lastPoint = LXmlUtils.xmlStrToDoubleArray(d, 2, lastPoint[0], lastPoint[1]);
                        lineTo(lastPoint[0], lastPoint[1]);
                    }
                    case 'H' -> {
                        al = LXmlUtils.xmlStrToDoubleArray(d, 1, lastPoint[0], 0.0);
                        lineTo(al[0], lastPoint[1]);
                        lastPoint[0] = al[0];
                    }
                    case 'V' -> {
                        al = LXmlUtils.xmlStrToDoubleArray(d, 1, lastPoint[1], 0.0);
                        lineTo(lastPoint[0], al[0]);
                        lastPoint[1] = al[0];
                    }
                    case 'C' -> {
                        al = LXmlUtils.xmlStrToDoubleArray(d, 6, lastPoint[0], lastPoint[1]);
                        curveTo(al[0], al[1], al[2], al[3], al[4], al[5]);
                        lastPoint[0] = al[4];
                        lastPoint[1] = al[5];
                        secPoint[0] = al[2] - al[4];
                        secPoint[1] = al[3] - al[5];
                    }
                    case 'S' -> {
                        al = LXmlUtils.xmlStrToDoubleArray(d, 4, lastPoint[0], lastPoint[1]);
                        curveTo(lastPoint[0] - secPoint[0], lastPoint[1] - secPoint[1], al[0], al[1], al[2], al[3]);
                        lastPoint[0] = al[2];
                        lastPoint[1] = al[3];
                        secPoint[0] = al[0] - al[2];
                        secPoint[1] = al[1] - al[3];
                        
                    }
                    case 'Q', 'T' -> {
                        al = LXmlUtils.xmlStrToDoubleArray(d, 4, lastPoint[0], lastPoint[1]);
                        quadTo(al[0], al[1], al[2], al[3]);
                        lastPoint[0] = al[2];
                        lastPoint[1] = al[3];
                    }
                    case 'A' -> {
                        al = LXmlUtils.xmlStrToDoubleArray(d, 7);
                        arcToBezier(al[0], al[1], al[2], al[3], al[4], al[5], (al[6] == 1));
                        lastPoint[0] = al[0];
                        lastPoint[1] = al[1];
                    }    
                    case 'Z' -> {
                        closePath();
                    }
                    default -> {
                        throw new IllegalArgumentException("Unknown path command '" + c + "' / Rest of string to parse: " + d);
                    }    
                }
            }
        }

    }

    @Override
    public void toXml(Document doc, Element node) {
        String path = "";
        LShape.ShapeIterator p_it = new LShape.ShapeIterator(this);
        while (!p_it.isDone()) {
            switch (p_it.getPointType()) {
                case LShape.SEG_MOVETO:
                    path += "M " + Double.toString(p_it.getPointCoordinateX(0))
                            + " " + Double.toString(p_it.getPointCoordinateY(0))
                            + " ";
                    break;
                case LShape.SEG_LINETO:
                    path += "L " + Double.toString(p_it.getPointCoordinateX(0))
                            + " " + Double.toString(p_it.getPointCoordinateY(0))
                            + " ";
                    break;
                case LShape.SEG_QUADTO:
                    path += "Q " + Double.toString(p_it.getPointCoordinateX(0))
                            + " " + Double.toString(p_it.getPointCoordinateY(0))
                            + " " + Double.toString(p_it.getPointCoordinateX(1))
                            + " " + Double.toString(p_it.getPointCoordinateY(1))
                            + " ";
                    break;

                case LShape.SEG_CUBICTO:
                    path += "C " + Double.toString(p_it.getPointCoordinateX(0))
                            + " " + Double.toString(p_it.getPointCoordinateY(0))
                            + " " + Double.toString(p_it.getPointCoordinateX(1))
                            + " " + Double.toString(p_it.getPointCoordinateY(1))
                            + " " + Double.toString(p_it.getPointCoordinateX(2))
                            + " " + Double.toString(p_it.getPointCoordinateY(2))
                            + " ";
                    break;
                /*case LShape.SEG_ARCTO:    
                    path += "A " + Double.toString(p_it.getPointCoordinateX(0))
                            + " " + Double.toString(p_it.getPointCoordinateY(0))                            
                            + " " + Double.toString(p_it.getDouble(4))
                            + " " + (p_it.getFlag(5) ? "1" : "0")
                            + " " + (p_it.getFlag(6) ? "1" : "0")
                            + " " + Double.toString(p_it.getPointCoordinateX(1))
                            + " " + Double.toString(p_it.getPointCoordinateY(1))
                            + " ";*/
                case LShape.SEG_CLOSE:
                    path += "Z ";
                    break;
            }
            p_it.next();
        }
        path = path.trim();
        LXmlUtils.setAttribute(node, "d", path);
    }

    @SuppressWarnings("unchecked")
    public final LDouble x() {
        if (x == null) {
            x = new LDouble();
            x.addListener(boundsListener);
        }
        return x;
    }

    @Override
    public final double getX() {
        return (x != null ? x.get() : 0.0);
    }

    @Xml
    @Override
    public final void setX(double x) {
        x().set(x);
    }

    @SuppressWarnings("unchecked")
    public final LDouble y() {
        if (y == null) {
            y = new LDouble();
            y.addListener(boundsListener);
        }
        return y;
    }

    @Override
    public final double getY() {
        return (y != null ? y.get() : 0.0);
    }

    @Xml
    @Override
    public final void setY(double y) {
        y().set(y);
    }

    @SuppressWarnings("unchecked")
    public final LDouble width() {
        if (width == null) {
            width = new LDouble();
            width.addListener(boundsListener);
        }
        return width;
    }

    @Override
    public final double getWidth() {
        return (width != null ? width.get() : 0.0);
    }

    @Xml
    @Override
    public final void setWidth(double width) {
        width().set(width);
    }

    @SuppressWarnings("unchecked")
    public final LDouble height() {
        if (height == null) {
            height = new LDouble();
            height.addListener(boundsListener);
        }
        return height;
    }

    @Override
    public final double getHeight() {
        return (height != null ? height.get() : 0.0);
    }

    @Xml
    @Override
    public final void setHeight(double height) {
        height().set(height);
    }

    @Override
    public final void setBounds(double x, double y, double width, double height) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
    }

    protected void createPath() {
    }

    public LBounds getBounds() {
        return new LBounds(getX(), getY(), getWidth(), getHeight());
    }

    public void setBounds(ILBounds bounds) {
        this.setBounds(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    public double getCenterX() {
        return getX() + getWidth() / 2;
    }

    public double getCenterY() {
        return getY() + getHeight() / 2;
    }

    public final synchronized LPoint getCurrentPoint() {
        int index = numCoords;
        if (countPoints < 1 || index < 1) {
            return null;
        }
        if (pointTypes[countPoints - 1] == SEG_CLOSE) {
            loop:
            for (int i = countPoints - 2; i > 0; i--) {
                switch (pointTypes[i]) {
                    case SEG_MOVETO:
                        break loop;
                    case SEG_LINETO:
                        index -= 2;
                        break;
                    case SEG_QUADTO:
                        index -= 4;
                        break;
                    case SEG_CUBICTO:
                        index -= 6;
                        break;
                    case SEG_CLOSE:
                        break;
                }
            }
        }
        return getPoint(index - 2);
    }

    private LPoint getPoint(int coordindex) {
        return new LPoint(doubleCoords[coordindex], doubleCoords[coordindex + 1]);
    }

    public int getXIntValue() {
        return (int) Math.round(getX());
    }

    public int getYIntValue() {
        return (int) Math.round(getY());
    }

    public int getWidthIntValue() {
        return (int) Math.round(getWidth());
    }

    public int getHeightIntValue() {
        return (int) Math.round(getHeight());
    }

    public int getXIntValueCeil() {
        return (int) Math.ceil(getX());
    }

    public int getYIntValueCeil() {
        return (int) Math.ceil(getY());
    }

    public int getWidthIntCeil() {
        return (int) Math.ceil(getWidth());
    }

    public int getHeightIntCeil() {
        return (int) Math.ceil(getHeight());
    }

    public int getXIntValueFloor() {
        return (int) Math.floor(getX());
    }

    public int getYIntValueFloor() {
        return (int) Math.floor(getY());
    }

    public int getWidthIntValueFloor() {
        return (int) Math.floor(getWidth());
    }

    public int getHeightIntValueFloor() {
        return (int) Math.floor(getHeight());
    }

    @Override
    public boolean isEmpty() {
        return ((getWidth() <= 0.0) || (getHeight() <= 0.0));
    }

    public boolean contains(LShape otherShape) {
        if (otherShape == null) {
            return false;
        }
        return ((otherShape.getX() >= getX())
                && (otherShape.getY() >= getY())
                && (otherShape.getX() + otherShape.getWidth() <= getX() + getWidth())
                && (otherShape.getY() + otherShape.getHeight() <= getY() + getHeight()));
    }

    public boolean contains(double x, double y) {
        return ((x >= getX()) && (y >= getY()) && (x <= getX() + getWidth()) && (y <= getY()
                + getHeight()));
    }

    @Override
    public boolean intersects(ILBounds anotherBounds) {
        return LBounds.intersects(this, anotherBounds);
    }

    protected void ensureThatPointIsInShape(boolean initialMove, double x, double y) {
        double x1 = (initialMove ? x : Math.min(x, getX()));
        double y1 = (initialMove ? y : Math.min(y, getY()));
        double x2 = (initialMove ? x : Math.max(x, getX() + getWidth()));
        double y2 = (initialMove ? y : Math.max(y, getY() + getHeight()));
        setX(x1);
        setY(y1);
        setWidth(x2 - x1);
        setHeight(y2 - y1);
        //center.setLocation(this.x + this.width / 2, this.y + this.height / 2);
    }

    @Override
    public ILBounds createUnion(ILBounds anotherBounds) {
        try {
            ILBounds dest = (ILBounds) LReflections.newInstance(getClass());
            LBounds.union(this, anotherBounds, dest);
            return dest;
        } catch (Exception ex) {
            LLog.error(this, ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public ILBounds createIntersection(ILBounds anotherBounds) {
        try {
            ILBounds dest = (ILBounds) LReflections.newInstance(getClass());
            LBounds.intersect(this, anotherBounds, dest);
            return dest;
        } catch (Exception ex) {
            LLog.error(this, ex.getMessage(), ex);
            return null;
        }
    }

    public final synchronized LShape createTransformedShape(LMatrix matrix) {
        LShape p2d = (LShape) clone();
        if (matrix != null) {
            p2d.transform(matrix);
        }
        return p2d;
    }

    public void transform(LMatrix matrix) {
        matrix.transform(doubleCoords, 0, doubleCoords, 0, numCoords / 2);
        adjustBounds();
    }

    protected final synchronized void adjustBounds() {
        double x1, y1, x2, y2;
        int i = numCoords;
        if (i > 0) {
            y1 = y2 = doubleCoords[--i];
            x1 = x2 = doubleCoords[--i];
            while (i > 0) {
                double ky = doubleCoords[--i];
                double kx = doubleCoords[--i];
                x1 = (kx < x1 ? kx : x1);
                y1 = (ky < y1 ? ky : y1);
                x2 = (kx > x2 ? kx : x2);
                y2 = (ky > y2 ? ky : y2);
            }
        } else {
            x1 = y1 = x2 = y2 = 0.0;
        }
        setBounds(x1, y1, x2 - x1, y2 - y1);
    }

    public Object getClientProperty(String key) {
        if (clientProperties != null) {
            return clientProperties.get(key);
        }
        return null;
    }

    public void putClientProperty(String key, Object value) {
        if (value != null) {
            if (clientProperties == null) {
                clientProperties = new LMap<String, Object>();
            }
            Object oldValue = clientProperties.put(key, value);
            if (!value.equals(oldValue)) {
                //this.notifyItemChanged(key, oldValue);
            }
        }
    }

    public static LShape getResizedShape(ILBounds r, ILBounds viewBounds, ILBounds paintBounds) {
        LShape result = new LShape(LDrawUtils.getX(paintBounds, r.getX() / viewBounds.getWidth()),
                LDrawUtils.getY(paintBounds, r.getY() / viewBounds.getHeight()),
                LDrawUtils.getWidth(paintBounds, r.getWidth() / viewBounds.getWidth()),
                LDrawUtils.getHeight(paintBounds, r.getHeight() / viewBounds.getHeight()));
        return result;
    }

    @Override
    public void execute(LCanvasRenderer canvasRenderer, long now) {
        if (animations != null) {
            LYososIterator<ILAnimation> it_anim = new LYososIterator<>(getAnimations());
            while (it_anim.hasNext()) {
                ILAnimation anim = it_anim.next();
                if (anim != null) {
                    anim.execute(canvasRenderer, this, now);
                }
            }
        }
        EnumSet<LPaintStyle> pStyle = getStyle();
        if (pStyle.contains(LPaintStyle.CLIP)) {
            canvasRenderer.clip(this);
        }
        if (pStyle.contains(LPaintStyle.FILL)) {
            canvasRenderer.fillShape(this);
            canvasRenderer.setLastShape(this);
        }
        if (pStyle.contains(LPaintStyle.STROKE)) {
            canvasRenderer.strokeShape(this);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            LShape p = (LShape) LReflections.newInstance(getClass());
            p.x = LDouble.clone(x);
            p.y = LDouble.clone(y);
            p.width = LDouble.clone(width);
            p.height = LDouble.clone(height);
            p.id = LString.clone(id);
            p.visible = LBoolean.clone(visible);
            p.style = LObservable.clone(style);
            p.rotatable = LBoolean.clone(rotatable);
            LShape.ShapeIterator p_it = new LShape.ShapeIterator(this);
            while (!p_it.isDone()) {
                switch (p_it.getPointType()) {
                    case LShape.SEG_MOVETO:
                        p.moveTo(p_it.getPointCoordinateX(0),
                                p_it.getPointCoordinateY(0));
                        break;
                    case LShape.SEG_LINETO:
                        p.lineTo(p_it.getPointCoordinateX(0),
                                p_it.getPointCoordinateY(0));
                        break;
                    case LShape.SEG_QUADTO:
                        p.quadTo(p_it.getPointCoordinateX(0),
                                p_it.getPointCoordinateY(0),
                                p_it.getPointCoordinateX(1),
                                p_it.getPointCoordinateY(1));
                        break;
                    case LShape.SEG_CUBICTO:
                        p.curveTo(p_it.getPointCoordinateX(0),
                                p_it.getPointCoordinateY(0),
                                p_it.getPointCoordinateX(1),
                                p_it.getPointCoordinateY(1),
                                p_it.getPointCoordinateX(2),
                                p_it.getPointCoordinateY(2));
                        break;
                    case LShape.SEG_CLOSE:
                        p.closePath();
                        break;
                }
                p_it.next();
            }
            return p;
        } catch (Exception e) {
            LLog.error(this, "LShape.clone", e);
            throw new InternalError();
        }
    }

    @Override
    public String toString() {
        return LXmlUtils.classToString(this, Xml.class);
    }

    @Override
    public int compareTo(LShape os) {
        if (os == null) {
            return 1;
        }
        if ((LGeomUtils.isEqual(getX(), os.getX(), LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(getY(), os.getY(), LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(getWidth(), os.getWidth(), LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(getHeight(), os.getHeight(), LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
            return 0;
        } else {
            return (int) Math.ceil((getWidth() * getHeight()) - (os.getWidth() * os.getHeight()));
        }
    }

    static public class ShapeIterator {

        private int typeIdx;
        private int pointIdx;
        private final LShape path;
        private static final int[] CURVE_COORDS = {2, 2, 4, 6, 0};

        public ShapeIterator(LShape path) {
            this.path = path;
            if (path == null) {
                throw new IllegalArgumentException("Shape for iterator cant be null.");
            }
            typeIdx = 0;
            pointIdx = 0;
        }

        public boolean isDone() {
            return (typeIdx >= path.countPoints);
        }

        public void next() {
            int type = path.pointTypes[typeIdx++];
            pointIdx += CURVE_COORDS[type];
        }

        public byte getPointType() {
            return path.pointTypes[typeIdx];
        }

        public double getPointCoordinateX(int position) {
            return path.doubleCoords[pointIdx + 2 * position];
        }

        public double getPointCoordinateY(int position) {
            return path.doubleCoords[pointIdx + 2 * position + 1];
        }
    }

}
