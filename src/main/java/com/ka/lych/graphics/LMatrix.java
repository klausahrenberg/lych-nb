package com.ka.lych.graphics;

import com.ka.lych.annotation.Json;
import com.ka.lych.geometry.LGeomUtils;
import com.ka.lych.geometry.LPoint;
import com.ka.lych.util.ILCloneable;
import com.ka.lych.util.LNoninvertibleMatrixException;

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

    @Json
    double _scaleX; 
    @Json
    double _scaleY;
    @Json
    double _shearX;
    @Json
    double _shearY;
    @Json
    double _translateX;
    @Json
    double _translateY;
    private transient int type;
    protected transient int state;

    //protected Object nativeMatrix;
    public LMatrix() {
        _scaleX = _scaleY = 1.0;
        _shearX = _shearY = _translateX = _translateY = 0.0;
        state = APPLY_IDENTITY;
        type = TYPE_IDENTITY;
    }

    public LMatrix(LMatrix srcMatrix) {
        _scaleX = srcMatrix._scaleX;
        _shearY = srcMatrix._shearY;
        _shearX = srcMatrix._shearX;
        _scaleY = srcMatrix._scaleY;
        _translateX = srcMatrix._translateX;
        _translateY = srcMatrix._translateY;
        this.state = srcMatrix.state;
        this.type = srcMatrix.type;
    }

    @Override
    public int compareTo(LMatrix mc) {
        if ((mc == null)
                || (!LGeomUtils.isEqual(_scaleX, mc._scaleX, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                || (!LGeomUtils.isEqual(_shearY, mc._shearY, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                || (!LGeomUtils.isEqual(_shearX, mc._shearX, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                || (!LGeomUtils.isEqual(_scaleY, mc._scaleY, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                || (!LGeomUtils.isEqual(_translateX, mc._translateX, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                || (!LGeomUtils.isEqual(_translateY, mc._translateY, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                || (this.state != mc.state) || (this.type != mc.type)) {
            return 1;
        } else {
            return 0;
        }
    }

    public LMatrix(double m00, double m10, double m01, double m11, double m02, double m12) {
        _scaleX = m00;
        _shearY = m10;
        _shearX = m01;
        _scaleY = m11;
        _translateX = m02;
        _translateY = m12;
        updateState();
    }

    public LMatrix(double[] flatmatrix) {
        _scaleX = flatmatrix[0];
        _shearY = flatmatrix[1];
        _shearX = flatmatrix[2];
        _scaleY = flatmatrix[3];
        if (flatmatrix.length > 5) {
            _translateX = flatmatrix[4];
            _translateY = flatmatrix[5];
        }
        updateState();
    }

    public LMatrix(float[] flatmatrix) {
        _scaleX = flatmatrix[0];
        _shearY = flatmatrix[1];
        _shearX = flatmatrix[2];
        _scaleY = flatmatrix[3];
        if (flatmatrix.length > 5) {
            _translateX = flatmatrix[4];
            _translateY = flatmatrix[5];
        }
        updateState();
    }

    private LMatrix(double m00, double m10,
            double m01, double m11,
            double m02, double m12,
            int state) {
        _scaleX = m00;
        _shearY = m10;
        _shearX = m01;
        _scaleY = m11;
        _translateX = m02;
        _translateY = m12;
        this.state = state;
        this.type = TYPE_UNKNOWN;
    }

    protected final void updateState() {
        if ((LGeomUtils.isEqual(_shearX, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(_shearY, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
            if ((LGeomUtils.isEqual(_scaleX, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                    && (LGeomUtils.isEqual(_scaleY, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                if ((LGeomUtils.isEqual(_translateX, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        && (LGeomUtils.isEqual(_translateY, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    state = APPLY_IDENTITY;
                    type = TYPE_IDENTITY;
                } else {
                    state = APPLY_TRANSLATE;
                    type = TYPE_TRANSLATION;
                }
            } else if ((LGeomUtils.isEqual(_translateX, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                    && (LGeomUtils.isEqual(_translateY, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                state = APPLY_SCALE;
                type = TYPE_UNKNOWN;
            } else {
                state = (APPLY_SCALE | APPLY_TRANSLATE);
                type = TYPE_UNKNOWN;
            }
        } else if ((LGeomUtils.isEqual(_scaleX, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(_scaleY, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
            if ((LGeomUtils.isEqual(_translateX, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                    && (LGeomUtils.isEqual(_translateY, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                state = APPLY_SHEAR;
                type = TYPE_UNKNOWN;
            } else {
                state = (APPLY_SHEAR | APPLY_TRANSLATE);
                type = TYPE_UNKNOWN;
            }
        } else if ((LGeomUtils.isEqual(_translateX, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(_translateY, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
            state = (APPLY_SHEAR | APPLY_SCALE);
            type = TYPE_UNKNOWN;
        } else {
            state = (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE);
            type = TYPE_UNKNOWN;
        }
    }

    public void setToIdentity() {
        _scaleX = _scaleY = 1.0;
        _shearY = _shearX = _translateX = _translateY = 0.0;
        state = APPLY_IDENTITY;
        type = TYPE_IDENTITY;
    }

    public void setToTranslation(double tx, double ty) {
        _scaleX = 1.0;
        _shearY = 0.0;
        _shearX = 0.0;
        _scaleY = 1.0;
        _translateX = tx;
        _translateY = ty;
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
        _scaleX = sx;
        _shearY = 0.0;
        _shearX = 0.0;
        _scaleY = sy;
        _translateX = 0.0;
        _translateY = 0.0;
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
        _scaleX = Tx._scaleX;
        _shearY = Tx._shearY;
        _shearX = Tx._shearX;
        _scaleY = Tx._scaleY;
        _translateX = Tx._translateX;
        _translateY = Tx._translateY;
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
                ptDst.setPoint(x * _scaleX + y * _shearX + _translateX,
                        x * _shearY + y * _scaleY + _translateY);
                return ptDst;
            case (APPLY_SHEAR | APPLY_SCALE):
                ptDst.setPoint(x * _scaleX + y * _shearX, x * _shearY + y * _scaleY);
                return ptDst;
            case (APPLY_SHEAR | APPLY_TRANSLATE):
                ptDst.setPoint(y * _shearX + _translateX, x * _shearY + _translateY);
                return ptDst;
            case (APPLY_SHEAR):
                ptDst.setPoint(y * _shearX, x * _shearY);
                return ptDst;
            case (APPLY_SCALE | APPLY_TRANSLATE):
                ptDst.setPoint(x * _scaleX + _translateX, y * _scaleY + _translateY);
                return ptDst;
            case (APPLY_SCALE):
                ptDst.setPoint(x * _scaleX, y * _scaleY);
                return ptDst;
            case (APPLY_TRANSLATE):
                ptDst.setPoint(x + _translateX, y + _translateY);
                return ptDst;
            case (APPLY_IDENTITY):
                ptDst.setPoint(x, y);
                return ptDst;
            default:
                throw new InternalError("missing case in transform state switch");
        }
    }

    public LVector transform(LVector vector) {
        double a = this.scaleX();
        double b = this.shearY();
        double c = this.shearX();
        double d = this.scaleY();
        double e = this.translateX();
        double f = this.translateY();
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
                x -= _translateX;
                y -= _translateY;
            // NOBREAK
            case (APPLY_SHEAR | APPLY_SCALE):
                double det = _scaleX * _scaleY - _shearX * _shearY;
                if (Math.abs(det) <= Double.MIN_VALUE) {
                    throw new LNoninvertibleMatrixException("Determinant is " + det);
                }
                ptDst.setPoint((x * _scaleY - y * _shearX) / det, (y * _scaleX - x * _shearY) / det);
                return ptDst;
            case (APPLY_SHEAR | APPLY_TRANSLATE):
                x -= _translateX;
                y -= _translateY;
            // NOBREAK
            case (APPLY_SHEAR):
                if (_shearX == 0.0 || _shearY == 0.0) {
                    throw new LNoninvertibleMatrixException("Determinant is 0");
                }
                ptDst.setPoint(y / _shearY, x / _shearX);
                return ptDst;
            case (APPLY_SCALE | APPLY_TRANSLATE):
                x -= _translateX;
                y -= _translateY;
            // NOBREAK
            case (APPLY_SCALE):
                if (_scaleX == 0.0 || _scaleY == 0.0) {
                    throw new LNoninvertibleMatrixException("Determinant is 0");
                }
                ptDst.setPoint(x / _scaleX, y / _scaleY);
                return ptDst;
            case (APPLY_TRANSLATE):
                ptDst.setPoint(x - _translateX, y - _translateY);
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
                M00 = _scaleX;
                M01 = _shearX;
                M02 = _translateX;
                M10 = _shearY;
                M11 = _scaleY;
                M12 = _translateY;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    double y = srcPts[srcOff++];
                    dstPts[dstOff++] = M00 * x + M01 * y + M02;
                    dstPts[dstOff++] = M10 * x + M11 * y + M12;
                }
                return;
            case (APPLY_SHEAR | APPLY_SCALE):
                M00 = _scaleX;
                M01 = _shearX;
                M10 = _shearY;
                M11 = _scaleY;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    double y = srcPts[srcOff++];
                    dstPts[dstOff++] = M00 * x + M01 * y;
                    dstPts[dstOff++] = M10 * x + M11 * y;
                }
                return;
            case (APPLY_SHEAR | APPLY_TRANSLATE):
                M01 = _shearX;
                M02 = _translateX;
                M10 = _shearY;
                M12 = _translateY;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    dstPts[dstOff++] = M01 * srcPts[srcOff++] + M02;
                    dstPts[dstOff++] = M10 * x + M12;
                }
                return;
            case (APPLY_SHEAR):
                M01 = _shearX;
                M10 = _shearY;
                while (--numPts >= 0) {
                    double x = srcPts[srcOff++];
                    dstPts[dstOff++] = M01 * srcPts[srcOff++];
                    dstPts[dstOff++] = M10 * x;
                }
                return;
            case (APPLY_SCALE | APPLY_TRANSLATE):
                M00 = _scaleX;
                M02 = _translateX;
                M11 = _scaleY;
                M12 = _translateY;
                while (--numPts >= 0) {
                    dstPts[dstOff++] = M00 * srcPts[srcOff++] + M02;
                    dstPts[dstOff++] = M11 * srcPts[srcOff++] + M12;
                }
                return;
            case (APPLY_SCALE):
                M00 = _scaleX;
                M11 = _scaleY;
                while (--numPts >= 0) {
                    dstPts[dstOff++] = M00 * srcPts[srcOff++];
                    dstPts[dstOff++] = M11 * srcPts[srcOff++];
                }
                return;
            case (APPLY_TRANSLATE):
                M02 = _translateX;
                M12 = _translateY;
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

    public double scaleX() {
        return _scaleX;
    }

    protected void scaleX(double m00) {
        _scaleX = m00;
        this.updateState();
    }

    public double scaleY() {
        return _scaleY;
    }

    protected void scaleY(double m11) {
        _scaleY = m11;
        this.updateState();
    }

    public double shearX() {
        return _shearX;
    }

    protected void shearX(double m01) {
        _shearX = m01;
        this.updateState();
    }

    public double shearY() {
        return _shearY;
    }

    protected void shearY(double m10) {
        _shearY = m10;
        this.updateState();
    }

    public double translateX() {
        return _translateX;
    }

    protected void translateX(double m02) {
        _translateX = m02;
        this.updateState();
    }

    public double translateY() {
        return _translateY;
    }

    protected void translateY(double m12) {
        _translateY = m12;
        this.updateState();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " [m00.scaleX="
                + Double.toString(_scaleX) + ", m01.shearX="
                + Double.toString(_shearX) + ", m02.translateX="
                + Double.toString(_translateX) + ", m10.shearY="
                + Double.toString(_shearY) + ", m11.scaleY="
                + Double.toString(_scaleY) + ", m12.translateY="
                + Double.toString(_translateY) + "]";

        //double m00, double m10, double m01, double m11, double m02, double m12  
    }

    public void translate(double tx, double ty) {
        switch (state) {
            default:
                throw new InternalError("missing case in transform state switch");
            case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
                _translateX = tx * _scaleX + ty * _shearX + _translateX;
                _translateY = tx * _shearY + ty * _scaleY + _translateY;
                if ((LGeomUtils.isEqual(_translateX, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        && (LGeomUtils.isEqual(_translateY, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    state = APPLY_SHEAR | APPLY_SCALE;
                    if (type != TYPE_UNKNOWN) {
                        type -= TYPE_TRANSLATION;
                    }
                }
                return;
            case (APPLY_SHEAR | APPLY_SCALE):
                _translateX = tx * _scaleX + ty * _shearX;
                _translateY = tx * _shearY + ty * _scaleY;
                if ((!LGeomUtils.isEqual(_translateX, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        || (!LGeomUtils.isEqual(_translateY, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    state = APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE;
                    type |= TYPE_TRANSLATION;
                }
                return;
            case (APPLY_SHEAR | APPLY_TRANSLATE):
                _translateX = ty * _shearX + _translateX;
                _translateY = tx * _shearY + _translateY;
                if ((LGeomUtils.isEqual(_translateX, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        && (LGeomUtils.isEqual(_translateY, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    state = APPLY_SHEAR;
                    if (type != TYPE_UNKNOWN) {
                        type -= TYPE_TRANSLATION;
                    }
                }
                return;
            case (APPLY_SHEAR):
                _translateX = ty * _shearX;
                _translateY = tx * _shearY;
                if ((!LGeomUtils.isEqual(_translateX, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        || (!LGeomUtils.isEqual(_translateY, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    state = APPLY_SHEAR | APPLY_TRANSLATE;
                    type |= TYPE_TRANSLATION;
                }
                return;
            case (APPLY_SCALE | APPLY_TRANSLATE):
                _translateX = tx * _scaleX + _translateX;
                _translateY = ty * _scaleY + _translateY;
                if ((LGeomUtils.isEqual(_translateX, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        && (LGeomUtils.isEqual(_translateY, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    state = APPLY_SCALE;
                    if (type != TYPE_UNKNOWN) {
                        type -= TYPE_TRANSLATION;
                    }
                }
                return;
            case (APPLY_SCALE):
                _translateX = tx * _scaleX;
                _translateY = ty * _scaleY;
                if ((!LGeomUtils.isEqual(_translateX, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        || (!LGeomUtils.isEqual(_translateY, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    state = APPLY_SCALE | APPLY_TRANSLATE;
                    type |= TYPE_TRANSLATION;
                }
                return;
            case (APPLY_TRANSLATE):
                _translateX = tx + _translateX;
                _translateY = ty + _translateY;
                if ((LGeomUtils.isEqual(_translateX, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        && (LGeomUtils.isEqual(_translateY, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    state = APPLY_IDENTITY;
                    type = TYPE_IDENTITY;
                }
                return;
            case (APPLY_IDENTITY):
                _translateX = tx;
                _translateY = ty;
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
                _scaleX *= sx;
                _scaleY *= sy;
            /* NOBREAK */
            case (APPLY_SHEAR | APPLY_TRANSLATE):
            case (APPLY_SHEAR):
                _shearX *= sy;
                _shearY *= sx;
                if ((LGeomUtils.isEqual(_shearX, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        && (LGeomUtils.isEqual(_shearY, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    stateTemp &= APPLY_TRANSLATE;
                    if ((LGeomUtils.isEqual(_scaleX, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                            && (LGeomUtils.isEqual(_scaleY, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
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
                _scaleX *= sx;
                _scaleY *= sy;
                if ((LGeomUtils.isEqual(_scaleX, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        && (LGeomUtils.isEqual(_scaleY, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
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
                _scaleX = sx;
                _scaleY = sy;
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
                M0 = _scaleX;
                M1 = _shearX;
                _scaleX = cos * M0 + sin * M1;
                _shearX = -sin * M0 + cos * M1;
                M0 = _shearY;
                M1 = _scaleY;
                _shearY = cos * M0 + sin * M1;
                _scaleY = -sin * M0 + cos * M1;
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
        double M0 = _scaleX;
        _scaleX = _shearX;
        _shearX = -M0;
        M0 = _shearY;
        _shearY = _scaleY;
        _scaleY = -M0;
        int state = rot90conversion[this.state];
        if ((state & (APPLY_SHEAR | APPLY_SCALE)) == APPLY_SCALE
                && (LGeomUtils.isEqual(_scaleX, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(_scaleY, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
            state -= APPLY_SCALE;
        }
        this.state = state;
        type = TYPE_UNKNOWN;
    }

    private void rotate180() {
        _scaleX = -_scaleX;
        _scaleY = -_scaleY;
        int stateTemp = this.state;
        if ((stateTemp & (APPLY_SHEAR)) != 0) {
            // If there was a shear, then this rotation has no
            // effect on the state.
            _shearX = -_shearX;
            _shearY = -_shearY;
        } else // No shear means the SCALE state may toggle when
        // m00 and m11 are negated.
        {
            if ((LGeomUtils.isEqual(_scaleX, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                    && (LGeomUtils.isEqual(_scaleY, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                this.state = stateTemp & ~APPLY_SCALE;
            } else {
                this.state = stateTemp | APPLY_SCALE;
            }
        }
        type = TYPE_UNKNOWN;
    }

    private void rotate270() {
        double M0 = _scaleX;
        _scaleX = -_shearX;
        _shearX = M0;
        M0 = _shearY;
        _shearY = -_scaleY;
        _scaleY = M0;
        int state = rot90conversion[this.state];
        if ((state & (APPLY_SHEAR | APPLY_SCALE)) == APPLY_SCALE
                && (LGeomUtils.isEqual(_scaleX, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                && (LGeomUtils.isEqual(_scaleY, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
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
                _shearX = Tx._shearX;
                _shearY = Tx._shearY;
            /* NOBREAK */
            case (HI_SCALE | HI_TRANSLATE):
                _scaleX = Tx._scaleX;
                _scaleY = Tx._scaleY;
            /* NOBREAK */
            case (HI_TRANSLATE):
                _translateX = Tx._translateX;
                _translateY = Tx._translateY;
                state = txstate;
                type = Tx.type;
                return;
            case (HI_SHEAR | HI_SCALE):
                _shearX = Tx._shearX;
                _shearY = Tx._shearY;
            /* NOBREAK */
            case (HI_SCALE):
                _scaleX = Tx._scaleX;
                _scaleY = Tx._scaleY;
                state = txstate;
                type = Tx.type;
                return;
            case (HI_SHEAR | HI_TRANSLATE):
                _translateX = Tx._translateX;
                _translateY = Tx._translateY;
            /* NOBREAK */
            case (HI_SHEAR):
                _shearX = Tx._shearX;
                _shearY = Tx._shearY;
                _scaleX = _scaleY = 0.0;
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
                translate(Tx._translateX, Tx._translateY);
                return;

            /* ---------- Tx == SCALE cases ---------- */
            case (HI_SCALE | APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            case (HI_SCALE | APPLY_SHEAR | APPLY_SCALE):
            case (HI_SCALE | APPLY_SHEAR | APPLY_TRANSLATE):
            case (HI_SCALE | APPLY_SHEAR):
            case (HI_SCALE | APPLY_SCALE | APPLY_TRANSLATE):
            case (HI_SCALE | APPLY_SCALE):
            case (HI_SCALE | APPLY_TRANSLATE):
                scale(Tx._scaleX, Tx._scaleY);
                return;

            /* ---------- Tx == SHEAR cases ---------- */
            case (HI_SHEAR | APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            case (HI_SHEAR | APPLY_SHEAR | APPLY_SCALE):
                T01 = Tx._shearX;
                T10 = Tx._shearY;
                M0 = _scaleX;
                _scaleX = _shearX * T10;
                _shearX = M0 * T01;
                M0 = _shearY;
                _shearY = _scaleY * T10;
                _scaleY = M0 * T01;
                type = TYPE_UNKNOWN;
                return;
            case (HI_SHEAR | APPLY_SHEAR | APPLY_TRANSLATE):
            case (HI_SHEAR | APPLY_SHEAR):
                _scaleX = _shearX * Tx._shearY;
                _shearX = 0.0;
                _scaleY = _shearY * Tx._shearX;
                _shearY = 0.0;
                state = mystate ^ (APPLY_SHEAR | APPLY_SCALE);
                type = TYPE_UNKNOWN;
                return;
            case (HI_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            case (HI_SHEAR | APPLY_SCALE):
                _shearX = _scaleX * Tx._shearX;
                _scaleX = 0.0;
                _shearY = _scaleY * Tx._shearY;
                _scaleY = 0.0;
                state = mystate ^ (APPLY_SHEAR | APPLY_SCALE);
                type = TYPE_UNKNOWN;
                return;
            case (HI_SHEAR | APPLY_TRANSLATE):
                _scaleX = 0.0;
                _shearX = Tx._shearX;
                _shearY = Tx._shearY;
                _scaleY = 0.0;
                state = APPLY_TRANSLATE | APPLY_SHEAR;
                type = TYPE_UNKNOWN;
                return;
        }
        // If Tx has more than one attribute, it is not worth optimizing
        // all of those cases...
        T00 = Tx._scaleX;
        T01 = Tx._shearX;
        T02 = Tx._translateX;
        T10 = Tx._shearY;
        T11 = Tx._scaleY;
        T12 = Tx._translateY;
        switch (mystate) {
            default:
                throw new InternalError("missing case in transform state switch");
            case (APPLY_SHEAR | APPLY_SCALE):
                state = mystate | txstate;
            /* NOBREAK */
            case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
                M0 = _scaleX;
                M1 = _shearX;
                _scaleX = T00 * M0 + T10 * M1;
                _shearX = T01 * M0 + T11 * M1;
                _translateX += T02 * M0 + T12 * M1;

                M0 = _shearY;
                M1 = _scaleY;
                _shearY = T00 * M0 + T10 * M1;
                _scaleY = T01 * M0 + T11 * M1;
                _translateY += T02 * M0 + T12 * M1;
                type = TYPE_UNKNOWN;
                return;

            case (APPLY_SHEAR | APPLY_TRANSLATE):
            case (APPLY_SHEAR):
                M0 = _shearX;
                _scaleX = T10 * M0;
                _shearX = T11 * M0;
                _translateX += T12 * M0;

                M0 = _shearY;
                _shearY = T00 * M0;
                _scaleY = T01 * M0;
                _translateY += T02 * M0;
                break;

            case (APPLY_SCALE | APPLY_TRANSLATE):
            case (APPLY_SCALE):
                M0 = _scaleX;
                _scaleX = T00 * M0;
                _shearX = T01 * M0;
                _translateX += T02 * M0;

                M0 = _scaleY;
                _shearY = T10 * M0;
                _scaleY = T11 * M0;
                _translateY += T12 * M0;
                break;

            case (APPLY_TRANSLATE):
                _scaleX = T00;
                _shearX = T01;
                _translateX += T02;

                _shearY = T10;
                _scaleY = T11;
                _translateY += T12;
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
                det = _scaleX * _scaleY - _shearX * _shearY;
                if (Math.abs(det) <= Double.MIN_VALUE) {
                    throw new InternalError("Determinant is "
                            + det);
                }
                return new LMatrix(_scaleY / det, -_shearY / det,
                        -_shearX / det, _scaleX / det,
                        (_shearX * _translateY - _scaleY * _translateX) / det,
                        (_shearY * _translateX - _scaleX * _translateY) / det,
                        (APPLY_SHEAR
                        | APPLY_SCALE
                        | APPLY_TRANSLATE));
            case (APPLY_SHEAR | APPLY_SCALE):
                det = _scaleX * _scaleY - _shearX * _shearY;
                if (Math.abs(det) <= Double.MIN_VALUE) {
                    throw new InternalError("Determinant is "
                            + det);
                }
                return new LMatrix(_scaleY / det, -_shearY / det,
                        -_shearX / det, _scaleX / det,
                        0.0, 0.0,
                        (APPLY_SHEAR | APPLY_SCALE));
            case (APPLY_SHEAR | APPLY_TRANSLATE):
                if ((LGeomUtils.isEqual(_shearX, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        || (LGeomUtils.isEqual(_shearY, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    throw new InternalError("Determinant is 0");
                }
                return new LMatrix(0.0, 1.0 / _shearX,
                        1.0 / _shearY, 0.0,
                        -_translateY / _shearY, -_translateX / _shearX,
                        (APPLY_SHEAR | APPLY_TRANSLATE));
            case (APPLY_SHEAR):
                if ((LGeomUtils.isEqual(_shearX, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        || (LGeomUtils.isEqual(_shearY, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    throw new InternalError("Determinant is 0");
                }
                return new LMatrix(0.0, 1.0 / _shearX,
                        1.0 / _shearY, 0.0,
                        0.0, 0.0,
                        (APPLY_SHEAR));
            case (APPLY_SCALE | APPLY_TRANSLATE):
                if ((LGeomUtils.isEqual(_scaleX, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        || (LGeomUtils.isEqual(_scaleY, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    throw new InternalError("Determinant is 0");
                }
                return new LMatrix(1.0 / _scaleX, 0.0,
                        0.0, 1.0 / _scaleY,
                        -_translateX / _scaleX, -_translateY / _scaleY,
                        (APPLY_SCALE | APPLY_TRANSLATE));
            case (APPLY_SCALE):
                if ((LGeomUtils.isEqual(_scaleX, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))
                        || (LGeomUtils.isEqual(_scaleY, 0.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION))) {
                    throw new InternalError("Determinant is 0");
                }
                return new LMatrix(1.0 / _scaleX, 0.0,
                        0.0, 1.0 / _scaleY,
                        0.0, 0.0,
                        (APPLY_SCALE));
            case (APPLY_TRANSLATE):
                return new LMatrix(1.0, 0.0,
                        0.0, 1.0,
                        -_translateX, -_translateY,
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
                return this.scaleX() * this.scaleY() - this.shearX() * this.shearY();
            case APPLY_SHEAR | APPLY_TRANSLATE:
            case APPLY_SHEAR:
                return -(this.shearX() * this.shearY());
            case APPLY_SCALE | APPLY_TRANSLATE:
            case APPLY_SCALE:
                return this.scaleX() * this.scaleY();
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
