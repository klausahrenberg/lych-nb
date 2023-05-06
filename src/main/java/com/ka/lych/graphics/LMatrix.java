package com.ka.lych.graphics;

import com.ka.lych.geometry.LGeomUtils;
import com.ka.lych.geometry.LPoint;
import com.ka.lych.util.ILCloneable;
import com.ka.lych.util.LNoninvertibleMatrixException;
import com.ka.lych.xml.LXmlUtils;
import com.ka.lych.annotation.Xml;

/**
 *
 * @author klausahrenberg
 */
public class LMatrix
        implements ILCloneable, Comparable<LMatrix>, ILCanvasCommand {

    static final int APPLY_IDENTITY = 0;
    static final int APPLY_TRANSLATE = 1;
    static final int APPLY_SCALE = 2;
    static final int APPLY_SHEAR = 4;

    private static final int TYPE_UNKNOWN = -1;
    public static final int TYPE_IDENTITY = 0;
    public static final int TYPE_TRANSLATION = 1;
    public static final int TYPE_UNIFORM_SCALE = 2;
    public static final int TYPE_GENERAL_SCALE = 4;
    public static final int TYPE_MASK_SCALE = (TYPE_UNIFORM_SCALE | TYPE_GENERAL_SCALE);
    public static final int TYPE_FLIP = 64;
    public static final int TYPE_QUADRANT_ROTATION = 8;
    public static final int TYPE_GENERAL_ROTATION = 16;

    private static final int HI_SHIFT = 3;
    private static final int HI_IDENTITY = APPLY_IDENTITY << HI_SHIFT;
    private static final int HI_TRANSLATE = APPLY_TRANSLATE << HI_SHIFT;
    private static final int HI_SCALE = APPLY_SCALE << HI_SHIFT;
    private static final int HI_SHEAR = APPLY_SHEAR << HI_SHIFT;

    protected double scaleX = 1.0;
    protected double m00, m10, m01, m11, m02, m12;
    private transient int type;
    protected transient int state;

    //protected Object nativeMatrix;
    public LMatrix() {
        m00 = m11 = 1.0;
        m01 = m10 = m02 = m12 = 0.0;
        state = APPLY_IDENTITY;
        type = TYPE_IDENTITY;
    }

    public LMatrix(LMatrix srcMatrix) {
        this.m00 = srcMatrix.m00;
        this.m10 = srcMatrix.m10;
        this.m01 = srcMatrix.m01;
        this.m11 = srcMatrix.m11;
        this.m02 = srcMatrix.m02;
        this.m12 = srcMatrix.m12;
        this.state = srcMatrix.state;
        this.type = srcMatrix.type;
    }

    @Override
    public int compareTo(LMatrix mc) {
        if ((mc == null)
                || (!LGeomUtils.isEqual(m00, mc.m00, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                || (!LGeomUtils.isEqual(m10, mc.m10, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                || (!LGeomUtils.isEqual(m01, mc.m01, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                || (!LGeomUtils.isEqual(m11, mc.m11, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                || (!LGeomUtils.isEqual(m02, mc.m02, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                || (!LGeomUtils.isEqual(m12, mc.m12, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                || (this.state != mc.state) || (this.type != mc.type)) {
            return 1;
        } else {
            return 0;
        }
    }

    public LMatrix(double m00, double m10, double m01, double m11, double m02, double m12) {
        this.m00 = m00;
        this.m10 = m10;
        this.m01 = m01;
        this.m11 = m11;
        this.m02 = m02;
        this.m12 = m12;
        updateState();
    }

    public LMatrix(double[] flatmatrix) {
        m00 = flatmatrix[0];
        m10 = flatmatrix[1];
        m01 = flatmatrix[2];
        m11 = flatmatrix[3];
        if (flatmatrix.length > 5) {
            m02 = flatmatrix[4];
            m12 = flatmatrix[5];
        }
        updateState();
    }

    public LMatrix(float[] flatmatrix) {
        m00 = flatmatrix[0];
        m10 = flatmatrix[1];
        m01 = flatmatrix[2];
        m11 = flatmatrix[3];
        if (flatmatrix.length > 5) {
            m02 = flatmatrix[4];
            m12 = flatmatrix[5];
        }
        updateState();
    }

    private LMatrix(double m00, double m10,
            double m01, double m11,
            double m02, double m12,
            int state) {
        this.m00 = m00;
        this.m10 = m10;
        this.m01 = m01;
        this.m11 = m11;
        this.m02 = m02;
        this.m12 = m12;
        this.state = state;
        this.type = TYPE_UNKNOWN;
    }

    protected final void updateState() {
        if ((LGeomUtils.isEqual(m01, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(m10, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
            if ((LGeomUtils.isEqual(m00, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                    && (LGeomUtils.isEqual(m11, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                if ((LGeomUtils.isEqual(m02, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        && (LGeomUtils.isEqual(m12, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    state = APPLY_IDENTITY;
                    type = TYPE_IDENTITY;
                } else {
                    state = APPLY_TRANSLATE;
                    type = TYPE_TRANSLATION;
                }
            } else if ((LGeomUtils.isEqual(m02, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                    && (LGeomUtils.isEqual(m12, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                state = APPLY_SCALE;
                type = TYPE_UNKNOWN;
            } else {
                state = (APPLY_SCALE | APPLY_TRANSLATE);
                type = TYPE_UNKNOWN;
            }
        } else if ((LGeomUtils.isEqual(m00, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(m11, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
            if ((LGeomUtils.isEqual(m02, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                    && (LGeomUtils.isEqual(m12, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                state = APPLY_SHEAR;
                type = TYPE_UNKNOWN;
            } else {
                state = (APPLY_SHEAR | APPLY_TRANSLATE);
                type = TYPE_UNKNOWN;
            }
        } else if ((LGeomUtils.isEqual(m02, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(m12, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
            state = (APPLY_SHEAR | APPLY_SCALE);
            type = TYPE_UNKNOWN;
        } else {
            state = (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE);
            type = TYPE_UNKNOWN;
        }
    }

    public void setToIdentity() {
        m00 = m11 = 1.0;
        m10 = m01 = m02 = m12 = 0.0;
        state = APPLY_IDENTITY;
        type = TYPE_IDENTITY;
    }

    public void setToTranslation(double tx, double ty) {
        m00 = 1.0;
        m10 = 0.0;
        m01 = 0.0;
        m11 = 1.0;
        m02 = tx;
        m12 = ty;
        if ((!LGeomUtils.isEqual(tx, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                || (!LGeomUtils.isEqual(ty, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
            state = APPLY_TRANSLATE;
            type = TYPE_TRANSLATION;
        } else {
            state = APPLY_IDENTITY;
            type = TYPE_IDENTITY;
        }
    }

    public void setToScale(double sx, double sy) {
        m00 = sx;
        m10 = 0.0;
        m01 = 0.0;
        m11 = sy;
        m02 = 0.0;
        m12 = 0.0;
        if ((!LGeomUtils.isEqual(sx, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                || (!LGeomUtils.isEqual(sy, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
            state = APPLY_SCALE;
            type = TYPE_UNKNOWN;
        } else {
            state = APPLY_IDENTITY;
            type = TYPE_IDENTITY;
        }
    }

    public void setTransform(LMatrix Tx) {
        this.m00 = Tx.m00;
        this.m10 = Tx.m10;
        this.m01 = Tx.m01;
        this.m11 = Tx.m11;
        this.m02 = Tx.m02;
        this.m12 = Tx.m12;
        this.state = Tx.state;
        this.type = Tx.type;
    }

    public LPoint transform(LPoint ptSrc, LPoint ptDst) {
        return transform(ptSrc.getX(), ptSrc.getY(), ptDst);
    }

    public LPoint transform(double x, double y, LPoint ptDst) {
        if (ptDst == null) {
            ptDst = new LPoint();
        }
        switch (state) {
            case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
                ptDst.setPoint(x * m00 + y * m01 + m02,
                        x * m10 + y * m11 + m12);
                return ptDst;
            case (APPLY_SHEAR | APPLY_SCALE):
                ptDst.setPoint(x * m00 + y * m01, x * m10 + y * m11);
                return ptDst;
            case (APPLY_SHEAR | APPLY_TRANSLATE):
                ptDst.setPoint(y * m01 + m02, x * m10 + m12);
                return ptDst;
            case (APPLY_SHEAR):
                ptDst.setPoint(y * m01, x * m10);
                return ptDst;
            case (APPLY_SCALE | APPLY_TRANSLATE):
                ptDst.setPoint(x * m00 + m02, y * m11 + m12);
                return ptDst;
            case (APPLY_SCALE):
                ptDst.setPoint(x * m00, y * m11);
                return ptDst;
            case (APPLY_TRANSLATE):
                ptDst.setPoint(x + m02, y + m12);
                return ptDst;
            case (APPLY_IDENTITY):
                ptDst.setPoint(x, y);
                return ptDst;
            default:
                throw new InternalError("missing case in transform state switch");
        }
    }

    public LVector transform(LVector vector) {
        double a = this.getM00ScaleX();
        double b = this.getM10ShearY();
        double c = this.getM01ShearX();
        double d = this.getM11ScaleY();
        double e = this.getM02TranslateX();
        double f = this.getM12TranslateY();
        double x = vector.getX();
        double y = vector.getY();
        return new LVector(x * a + y * c + e, x * b + y * d + f);
    }
    
    public LPoint inverseTransform(LPoint ptSrc, LPoint ptDst) throws LNoninvertibleMatrixException {
        return inverseTransform(ptSrc.getX(), ptSrc.getY(), ptDst);
    }

    public LPoint inverseTransform(double x, double y, LPoint ptDst) throws LNoninvertibleMatrixException {
        if (ptDst == null) {
            ptDst = new LPoint();
        }
        switch (state) {
            case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
                x -= m02;
                y -= m12;
            // NOBREAK
            case (APPLY_SHEAR | APPLY_SCALE):
                double det = m00 * m11 - m01 * m10;
                if (Math.abs(det) <= Double.MIN_VALUE) {
                    throw new LNoninvertibleMatrixException("Determinant is " + det);
                }
                ptDst.setPoint((x * m11 - y * m01) / det, (y * m00 - x * m10) / det);
                return ptDst;
            case (APPLY_SHEAR | APPLY_TRANSLATE):
                x -= m02;
                y -= m12;
            // NOBREAK
            case (APPLY_SHEAR):
                if (m01 == 0.0 || m10 == 0.0) {
                    throw new LNoninvertibleMatrixException("Determinant is 0");
                }
                ptDst.setPoint(y / m10, x / m01);
                return ptDst;
            case (APPLY_SCALE | APPLY_TRANSLATE):
                x -= m02;
                y -= m12;
            // NOBREAK
            case (APPLY_SCALE):
                if (m00 == 0.0 || m11 == 0.0) {
                    throw new LNoninvertibleMatrixException("Determinant is 0");
                }
                ptDst.setPoint(x / m00, y / m11);
                return ptDst;
            case (APPLY_TRANSLATE):
                ptDst.setPoint(x - m02, y - m12);
                return ptDst;
            case (APPLY_IDENTITY):
                ptDst.setPoint(x, y);
                return ptDst;
            default:
                throw new InternalError("missing case in transform state switch");
        }
    }

    public void transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) {
        double M00, M01, M02, M10, M11, M12;	// For caching
        switch (state) {
            default:
                throw new InternalError("missing case in transform state switch");
            case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
                M00 = m00;
                M01 = m01;
                M02 = m02;
                M10 = m10;
                M11 = m11;
                M12 = m12;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    double y = srcPts[srcOff++];
                    dstPts[dstOff++] = M00 * x + M01 * y + M02;
                    dstPts[dstOff++] = M10 * x + M11 * y + M12;
                }
                return;
            case (APPLY_SHEAR | APPLY_SCALE):
                M00 = m00;
                M01 = m01;
                M10 = m10;
                M11 = m11;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    double y = srcPts[srcOff++];
                    dstPts[dstOff++] = M00 * x + M01 * y;
                    dstPts[dstOff++] = M10 * x + M11 * y;
                }
                return;
            case (APPLY_SHEAR | APPLY_TRANSLATE):
                M01 = m01;
                M02 = m02;
                M10 = m10;
                M12 = m12;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    dstPts[dstOff++] = M01 * srcPts[srcOff++] + M02;
                    dstPts[dstOff++] = M10 * x + M12;
                }
                return;
            case (APPLY_SHEAR):
                M01 = m01;
                M10 = m10;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    dstPts[dstOff++] = M01 * srcPts[srcOff++];
                    dstPts[dstOff++] = M10 * x;
                }
                return;
            case (APPLY_SCALE | APPLY_TRANSLATE):
                M00 = m00;
                M02 = m02;
                M11 = m11;
                M12 = m12;
                while (--numPts >= 0) {
                    dstPts[dstOff++] = M00 * srcPts[srcOff++] + M02;
                    dstPts[dstOff++] = M11 * srcPts[srcOff++] + M12;
                }
                return;
            case (APPLY_SCALE):
                M00 = m00;
                M11 = m11;
                while (--numPts >= 0) {
                    dstPts[dstOff++] = M00 * srcPts[srcOff++];
                    dstPts[dstOff++] = M11 * srcPts[srcOff++];
                }
                return;
            case (APPLY_TRANSLATE):
                M02 = m02;
                M12 = m12;
                while (--numPts >= 0) {
                    dstPts[dstOff++] = srcPts[srcOff++] + M02;
                    dstPts[dstOff++] = srcPts[srcOff++] + M12;
                }
                return;
            case (APPLY_IDENTITY):
                while (--numPts >= 0) {
                    dstPts[dstOff++] = srcPts[srcOff++];
                    dstPts[dstOff++] = srcPts[srcOff++];
                }
        }
    }

    public double getM00ScaleX() {
        return m00;
    }

    @Xml
    protected void setScaleX(double m00) {
        this.m00 = m00;
        this.updateState();
    }

    public double getM11ScaleY() {
        return m11;
    }

    @Xml
    protected void setscaleY(double m11) {
        this.m11 = m11;
        this.updateState();
    }

    public double getM01ShearX() {
        return m01;
    }

    @Xml
    protected void setShearX(double m01) {
        this.m01 = m01;
        this.updateState();
    }

    public double getM10ShearY() {
        return m10;
    }

    @Xml
    protected void setShearY(double m10) {
        this.m10 = m10;
        this.updateState();
    }

    public double getM02TranslateX() {
        return m02;
    }

    @Xml
    protected void setTranslateX(double m02) {
        this.m02 = m02;
        this.updateState();
    }

    public double getM12TranslateY() {
        return m12;
    }

    @Xml
    protected void setTranslateY(double m12) {
        this.m12 = m12;
        this.updateState();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " [m00.scaleX="
                + Double.toString(m00) + ", m01.shearX="
                + Double.toString(m01) + ", m02.translateX="
                + Double.toString(m02) + ", m10.shearY="
                + Double.toString(m10) + ", m11.scaleY="
                + Double.toString(m11) + ", m12.translateY="
                + Double.toString(m12) + "]";

        //double m00, double m10, double m01, double m11, double m02, double m12  
    }

    public void translate(double tx, double ty) {
        switch (state) {
            default:
                throw new InternalError("missing case in transform state switch");
            case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
                m02 = tx * m00 + ty * m01 + m02;
                m12 = tx * m10 + ty * m11 + m12;
                if ((LGeomUtils.isEqual(m02, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        && (LGeomUtils.isEqual(m12, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    state = APPLY_SHEAR | APPLY_SCALE;
                    if (type != TYPE_UNKNOWN) {
                        type -= TYPE_TRANSLATION;
                    }
                }
                return;
            case (APPLY_SHEAR | APPLY_SCALE):
                m02 = tx * m00 + ty * m01;
                m12 = tx * m10 + ty * m11;
                if ((!LGeomUtils.isEqual(m02, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        || (!LGeomUtils.isEqual(m12, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    state = APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE;
                    type |= TYPE_TRANSLATION;
                }
                return;
            case (APPLY_SHEAR | APPLY_TRANSLATE):
                m02 = ty * m01 + m02;
                m12 = tx * m10 + m12;
                if ((LGeomUtils.isEqual(m02, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        && (LGeomUtils.isEqual(m12, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    state = APPLY_SHEAR;
                    if (type != TYPE_UNKNOWN) {
                        type -= TYPE_TRANSLATION;
                    }
                }
                return;
            case (APPLY_SHEAR):
                m02 = ty * m01;
                m12 = tx * m10;
                if ((!LGeomUtils.isEqual(m02, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        || (!LGeomUtils.isEqual(m12, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    state = APPLY_SHEAR | APPLY_TRANSLATE;
                    type |= TYPE_TRANSLATION;
                }
                return;
            case (APPLY_SCALE | APPLY_TRANSLATE):
                m02 = tx * m00 + m02;
                m12 = ty * m11 + m12;
                if ((LGeomUtils.isEqual(m02, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        && (LGeomUtils.isEqual(m12, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    state = APPLY_SCALE;
                    if (type != TYPE_UNKNOWN) {
                        type -= TYPE_TRANSLATION;
                    }
                }
                return;
            case (APPLY_SCALE):
                m02 = tx * m00;
                m12 = ty * m11;
                if ((!LGeomUtils.isEqual(m02, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        || (!LGeomUtils.isEqual(m12, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    state = APPLY_SCALE | APPLY_TRANSLATE;
                    type |= TYPE_TRANSLATION;
                }
                return;
            case (APPLY_TRANSLATE):
                m02 = tx + m02;
                m12 = ty + m12;
                if ((LGeomUtils.isEqual(m02, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        && (LGeomUtils.isEqual(m12, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    state = APPLY_IDENTITY;
                    type = TYPE_IDENTITY;
                }
                return;
            case (APPLY_IDENTITY):
                m02 = tx;
                m12 = ty;
                if ((!LGeomUtils.isEqual(tx, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        || (!LGeomUtils.isEqual(ty, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    state = APPLY_TRANSLATE;
                    type = TYPE_TRANSLATION;
                }
                return;
        }
    }

    public void scale(double sx, double sy) {
        int stateTemp = this.state;
        switch (stateTemp) {
            default:
                throw new InternalError("missing case in transform state switch");
            case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            case (APPLY_SHEAR | APPLY_SCALE):
                m00 *= sx;
                m11 *= sy;
            /* NOBREAK */
            case (APPLY_SHEAR | APPLY_TRANSLATE):
            case (APPLY_SHEAR):
                m01 *= sy;
                m10 *= sx;
                if ((LGeomUtils.isEqual(m01, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        && (LGeomUtils.isEqual(m10, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    stateTemp &= APPLY_TRANSLATE;
                    if ((LGeomUtils.isEqual(m00, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                            && (LGeomUtils.isEqual(m11, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                        this.type = (stateTemp == APPLY_IDENTITY
                                ? TYPE_IDENTITY
                                : TYPE_TRANSLATION);
                    } else {
                        stateTemp |= APPLY_SCALE;
                        this.type = TYPE_UNKNOWN;
                    }
                    this.state = stateTemp;
                }
                return;
            case (APPLY_SCALE | APPLY_TRANSLATE):
            case (APPLY_SCALE):
                m00 *= sx;
                m11 *= sy;
                if ((LGeomUtils.isEqual(m00, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        && (LGeomUtils.isEqual(m11, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    this.state = (stateTemp &= APPLY_TRANSLATE);
                    this.type = (stateTemp == APPLY_IDENTITY
                            ? TYPE_IDENTITY
                            : TYPE_TRANSLATION);
                } else {
                    this.type = TYPE_UNKNOWN;
                }
                return;
            case (APPLY_TRANSLATE):
            case (APPLY_IDENTITY):
                m00 = sx;
                m11 = sy;
                if ((LGeomUtils.isEqual(sx, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        || (!LGeomUtils.isEqual(sy, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    this.state = stateTemp | APPLY_SCALE;
                    this.type = TYPE_UNKNOWN;
                }
        }
    }

    /**
     * Rotates the matrix in degrees 0..360°
     *
     * @param degree
     */
    @Xml
    public void setRotation(double degree) {
        rotate(Math.toRadians(degree));
    }

    public void rotate(double radians) {
        double sin = Math.sin(radians);
        if (LGeomUtils.isEqual(sin, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION)) {
            rotate90();
        } else if (LGeomUtils.isEqual(sin, -1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION)) {
            rotate270();
        } else {
            double cos = Math.cos(radians);
            if (LGeomUtils.isEqual(cos, -1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION)) {
                rotate180();
            } else if (!LGeomUtils.isEqual(cos, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION)) {
                double M0, M1;
                M0 = m00;
                M1 = m01;
                m00 = cos * M0 + sin * M1;
                m01 = -sin * M0 + cos * M1;
                M0 = m10;
                M1 = m11;
                m10 = cos * M0 + sin * M1;
                m11 = -sin * M0 + cos * M1;
                updateState();
            }
        }
    }

    public void rotate(double radians, double centerX, double centerY) {
        translate(centerX, centerY);
        rotate(radians);
        translate(-centerX, -centerY);
    }

    private static final int rot90conversion[] = {
        /* IDENTITY => */APPLY_SHEAR,
        /* TRANSLATE (TR) => */ APPLY_SHEAR | APPLY_TRANSLATE,
        /* SCALE (SC) => */ APPLY_SHEAR,
        /* SC | TR => */ APPLY_SHEAR | APPLY_TRANSLATE,
        /* SHEAR (SH) => */ APPLY_SCALE,
        /* SH | TR => */ APPLY_SCALE | APPLY_TRANSLATE,
        /* SH | SC => */ APPLY_SHEAR | APPLY_SCALE,
        /* SH | SC | TR => */ APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE,};

    private void rotate90() {
        double M0 = m00;
        m00 = m01;
        m01 = -M0;
        M0 = m10;
        m10 = m11;
        m11 = -M0;
        int state = rot90conversion[this.state];
        if ((state & (APPLY_SHEAR | APPLY_SCALE)) == APPLY_SCALE
                && (LGeomUtils.isEqual(m00, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(m11, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
            state -= APPLY_SCALE;
        }
        this.state = state;
        type = TYPE_UNKNOWN;
    }

    private void rotate180() {
        m00 = -m00;
        m11 = -m11;
        int stateTemp = this.state;
        if ((stateTemp & (APPLY_SHEAR)) != 0) {
            // If there was a shear, then this rotation has no
            // effect on the state.
            m01 = -m01;
            m10 = -m10;
        } else // No shear means the SCALE state may toggle when
        // m00 and m11 are negated.
        {
            if ((LGeomUtils.isEqual(m00, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                    && (LGeomUtils.isEqual(m11, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                this.state = stateTemp & ~APPLY_SCALE;
            } else {
                this.state = stateTemp | APPLY_SCALE;
            }
        }
        type = TYPE_UNKNOWN;
    }

    private void rotate270() {
        double M0 = m00;
        m00 = -m01;
        m01 = M0;
        M0 = m10;
        m10 = -m11;
        m11 = M0;
        int state = rot90conversion[this.state];
        if ((state & (APPLY_SHEAR | APPLY_SCALE)) == APPLY_SCALE
                && (LGeomUtils.isEqual(m00, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(m11, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
            state -= APPLY_SCALE;
        }
        this.state = state;
        type = TYPE_UNKNOWN;
    }

    public void concatenate(LMatrix Tx) {
        double M0, M1;
        double T00, T01, T10, T11;
        double T02, T12;
        int mystate = state;
        int txstate = Tx.state;
        switch ((txstate << HI_SHIFT) | mystate) {

            /* ---------- Tx == IDENTITY cases ---------- */
            case (HI_IDENTITY):
            case (APPLY_TRANSLATE):
            case (APPLY_SCALE):
            case (APPLY_SCALE | APPLY_TRANSLATE):
            case (APPLY_SHEAR):
            case (APPLY_SHEAR | APPLY_TRANSLATE):
            case (APPLY_SHEAR | APPLY_SCALE):
            case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
                return;

            /* ---------- this == IDENTITY cases ---------- */
            case (HI_SHEAR | HI_SCALE | HI_TRANSLATE):
                m01 = Tx.m01;
                m10 = Tx.m10;
            /* NOBREAK */
            case (HI_SCALE | HI_TRANSLATE):
                m00 = Tx.m00;
                m11 = Tx.m11;
            /* NOBREAK */
            case (HI_TRANSLATE):
                m02 = Tx.m02;
                m12 = Tx.m12;
                state = txstate;
                type = Tx.type;
                return;
            case (HI_SHEAR | HI_SCALE):
                m01 = Tx.m01;
                m10 = Tx.m10;
            /* NOBREAK */
            case (HI_SCALE):
                m00 = Tx.m00;
                m11 = Tx.m11;
                state = txstate;
                type = Tx.type;
                return;
            case (HI_SHEAR | HI_TRANSLATE):
                m02 = Tx.m02;
                m12 = Tx.m12;
            /* NOBREAK */
            case (HI_SHEAR):
                m01 = Tx.m01;
                m10 = Tx.m10;
                m00 = m11 = 0.0;
                state = txstate;
                type = Tx.type;
                return;

            /* ---------- Tx == TRANSLATE cases ---------- */
            case (HI_TRANSLATE | APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            case (HI_TRANSLATE | APPLY_SHEAR | APPLY_SCALE):
            case (HI_TRANSLATE | APPLY_SHEAR | APPLY_TRANSLATE):
            case (HI_TRANSLATE | APPLY_SHEAR):
            case (HI_TRANSLATE | APPLY_SCALE | APPLY_TRANSLATE):
            case (HI_TRANSLATE | APPLY_SCALE):
            case (HI_TRANSLATE | APPLY_TRANSLATE):
                translate(Tx.m02, Tx.m12);
                return;

            /* ---------- Tx == SCALE cases ---------- */
            case (HI_SCALE | APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            case (HI_SCALE | APPLY_SHEAR | APPLY_SCALE):
            case (HI_SCALE | APPLY_SHEAR | APPLY_TRANSLATE):
            case (HI_SCALE | APPLY_SHEAR):
            case (HI_SCALE | APPLY_SCALE | APPLY_TRANSLATE):
            case (HI_SCALE | APPLY_SCALE):
            case (HI_SCALE | APPLY_TRANSLATE):
                scale(Tx.m00, Tx.m11);
                return;

            /* ---------- Tx == SHEAR cases ---------- */
            case (HI_SHEAR | APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            case (HI_SHEAR | APPLY_SHEAR | APPLY_SCALE):
                T01 = Tx.m01;
                T10 = Tx.m10;
                M0 = m00;
                m00 = m01 * T10;
                m01 = M0 * T01;
                M0 = m10;
                m10 = m11 * T10;
                m11 = M0 * T01;
                type = TYPE_UNKNOWN;
                return;
            case (HI_SHEAR | APPLY_SHEAR | APPLY_TRANSLATE):
            case (HI_SHEAR | APPLY_SHEAR):
                m00 = m01 * Tx.m10;
                m01 = 0.0;
                m11 = m10 * Tx.m01;
                m10 = 0.0;
                state = mystate ^ (APPLY_SHEAR | APPLY_SCALE);
                type = TYPE_UNKNOWN;
                return;
            case (HI_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            case (HI_SHEAR | APPLY_SCALE):
                m01 = m00 * Tx.m01;
                m00 = 0.0;
                m10 = m11 * Tx.m10;
                m11 = 0.0;
                state = mystate ^ (APPLY_SHEAR | APPLY_SCALE);
                type = TYPE_UNKNOWN;
                return;
            case (HI_SHEAR | APPLY_TRANSLATE):
                m00 = 0.0;
                m01 = Tx.m01;
                m10 = Tx.m10;
                m11 = 0.0;
                state = APPLY_TRANSLATE | APPLY_SHEAR;
                type = TYPE_UNKNOWN;
                return;
        }
        // If Tx has more than one attribute, it is not worth optimizing
        // all of those cases...
        T00 = Tx.m00;
        T01 = Tx.m01;
        T02 = Tx.m02;
        T10 = Tx.m10;
        T11 = Tx.m11;
        T12 = Tx.m12;
        switch (mystate) {
            default:
                throw new InternalError("missing case in transform state switch");
            case (APPLY_SHEAR | APPLY_SCALE):
                state = mystate | txstate;
            /* NOBREAK */
            case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
                M0 = m00;
                M1 = m01;
                m00 = T00 * M0 + T10 * M1;
                m01 = T01 * M0 + T11 * M1;
                m02 += T02 * M0 + T12 * M1;

                M0 = m10;
                M1 = m11;
                m10 = T00 * M0 + T10 * M1;
                m11 = T01 * M0 + T11 * M1;
                m12 += T02 * M0 + T12 * M1;
                type = TYPE_UNKNOWN;
                return;

            case (APPLY_SHEAR | APPLY_TRANSLATE):
            case (APPLY_SHEAR):
                M0 = m01;
                m00 = T10 * M0;
                m01 = T11 * M0;
                m02 += T12 * M0;

                M0 = m10;
                m10 = T00 * M0;
                m11 = T01 * M0;
                m12 += T02 * M0;
                break;

            case (APPLY_SCALE | APPLY_TRANSLATE):
            case (APPLY_SCALE):
                M0 = m00;
                m00 = T00 * M0;
                m01 = T01 * M0;
                m02 += T02 * M0;

                M0 = m11;
                m10 = T10 * M0;
                m11 = T11 * M0;
                m12 += T12 * M0;
                break;

            case (APPLY_TRANSLATE):
                m00 = T00;
                m01 = T01;
                m02 += T02;

                m10 = T10;
                m11 = T11;
                m12 += T12;
                state = txstate | APPLY_TRANSLATE;
                type = TYPE_UNKNOWN;
                return;
        }
        updateState();
    }

    public LMatrix createInverse() {
        double det;
        switch (state) {
            default:
                throw new InternalError("missing case in transform state switch");
            case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
                det = m00 * m11 - m01 * m10;
                if (Math.abs(det) <= Double.MIN_VALUE) {
                    throw new InternalError("Determinant is "
                            + det);
                }
                return new LMatrix(m11 / det, -m10 / det,
                        -m01 / det, m00 / det,
                        (m01 * m12 - m11 * m02) / det,
                        (m10 * m02 - m00 * m12) / det,
                        (APPLY_SHEAR
                        | APPLY_SCALE
                        | APPLY_TRANSLATE));
            case (APPLY_SHEAR | APPLY_SCALE):
                det = m00 * m11 - m01 * m10;
                if (Math.abs(det) <= Double.MIN_VALUE) {
                    throw new InternalError("Determinant is "
                            + det);
                }
                return new LMatrix(m11 / det, -m10 / det,
                        -m01 / det, m00 / det,
                        0.0, 0.0,
                        (APPLY_SHEAR | APPLY_SCALE));
            case (APPLY_SHEAR | APPLY_TRANSLATE):
                if ((LGeomUtils.isEqual(m01, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        || (LGeomUtils.isEqual(m10, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    throw new InternalError("Determinant is 0");
                }
                return new LMatrix(0.0, 1.0 / m01,
                        1.0 / m10, 0.0,
                        -m12 / m10, -m02 / m01,
                        (APPLY_SHEAR | APPLY_TRANSLATE));
            case (APPLY_SHEAR):
                if ((LGeomUtils.isEqual(m01, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        || (LGeomUtils.isEqual(m10, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    throw new InternalError("Determinant is 0");
                }
                return new LMatrix(0.0, 1.0 / m01,
                        1.0 / m10, 0.0,
                        0.0, 0.0,
                        (APPLY_SHEAR));
            case (APPLY_SCALE | APPLY_TRANSLATE):
                if ((LGeomUtils.isEqual(m00, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        || (LGeomUtils.isEqual(m11, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    throw new InternalError("Determinant is 0");
                }
                return new LMatrix(1.0 / m00, 0.0,
                        0.0, 1.0 / m11,
                        -m02 / m00, -m12 / m11,
                        (APPLY_SCALE | APPLY_TRANSLATE));
            case (APPLY_SCALE):
                if ((LGeomUtils.isEqual(m00, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        || (LGeomUtils.isEqual(m11, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    throw new InternalError("Determinant is 0");
                }
                return new LMatrix(1.0 / m00, 0.0,
                        0.0, 1.0 / m11,
                        0.0, 0.0,
                        (APPLY_SCALE));
            case (APPLY_TRANSLATE):
                return new LMatrix(1.0, 0.0,
                        0.0, 1.0,
                        -m02, -m12,
                        (APPLY_TRANSLATE));
            case (APPLY_IDENTITY):
                return new LMatrix();
        }
    }

    public static LMatrix getTranslateInstance(double tx, double ty) {
        LMatrix Tx = new LMatrix();
        Tx.setToTranslation(tx, ty);
        return Tx;
    }

    public static LMatrix getScaleInstance(double sx, double sy) {
        LMatrix Tx = new LMatrix();
        Tx.setToScale(sx, sy);
        return Tx;
    }

    public double getDeterminant() {
        switch (state) {
            case APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE:
            case APPLY_SHEAR | APPLY_SCALE:
                return this.getM00ScaleX() * this.getM11ScaleY() - this.getM01ShearX() * this.getM10ShearY();
            case APPLY_SHEAR | APPLY_TRANSLATE:
            case APPLY_SHEAR:
                return -(this.getM01ShearX() * this.getM10ShearY());
            case APPLY_SCALE | APPLY_TRANSLATE:
            case APPLY_SCALE:
                return this.getM00ScaleX() * this.getM11ScaleY();
            case APPLY_TRANSLATE:
            case APPLY_IDENTITY:
                return 1.0;
            default:
                return 0;
        }
    }

    @Override
    public void execute(LCanvasRenderer canvasRenderer, long timeLine) {
        canvasRenderer.setMatrix(this, true);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

}
