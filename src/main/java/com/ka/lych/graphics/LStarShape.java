package com.ka.lych.graphics;

import com.ka.lych.exception.LException;
import com.ka.lych.geometry.LPoint;
import com.ka.lych.util.LReflections.LMethod;
import org.w3c.dom.Node;

/**
 *
 * @author klausahrenberg
 */
public class LStarShape extends LShape {

    public LStarShape() {
        this(0, 0, 0, 0);
    }

    public LStarShape(Object parent, LMethod[] parentMethods, Node n) throws LException {
        this(0, 0, 0, 0);
        parseXml(n, null);
    }

    public LStarShape(double x, double y, double width, double height) {
        super(21, 60);
        bounds(x, y, width, height);
    }
    
    @Override
    protected void createPath() {
        countPoints = numCoords = 0;
        if ((width().get() > 0) && (height().get() > 0)) {
            int vertexCount = 5;
            double innerRadius = 0.4;
            double degreeOffset = 0;
            double[] radiuses = new double[vertexCount * 2];
            for (int i = 0; i < vertexCount * 2; i++) {
                radiuses[i] = 0;
            }
            this.createPath(this.getPoints(x().get(), y().get(), width().get(), height().get(), innerRadius, vertexCount, degreeOffset),
                    radiuses,
                    true);
        }
    }

    protected LPoint getPoint(double x, double y, double width, double height, double degree, double radius) {
        double x1 = Math.sin(degree * 2 * Math.PI / 360) * radius;
        double y1 = Math.cos(degree * 2 * Math.PI / 360) * radius;
        return new LPoint(x + width / 2 + x1, y + height / 2 - y1);
    }

    public LPoint[] getPoints(double x, double y, double width, double height, double innerRadius, int vertexCount, double degreeOffset) {
        LPoint[] result = new LPoint[vertexCount * 2];
        double radius = (width > height ? height / 2 : width / 2);
        for (int i = 0; i < vertexCount; i++) {
            double degree = (360.0 / (double) vertexCount) * i + degreeOffset;
            result[i * 2] = this.getPoint(x, y, width, height, degree, radius);
            degree += (360.0 / (double) (vertexCount * 2));
            result[i * 2 + 1] = this.getPoint(x, y, width, height, degree, radius * innerRadius);
        }
        return result;
    }

}
