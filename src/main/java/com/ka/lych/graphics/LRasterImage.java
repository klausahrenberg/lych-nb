package com.ka.lych.graphics;

import com.ka.lych.util.ILBlobable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 *
 * @author klausahrenberg
 */
public class LRasterImage extends LShape 
        implements ILBlobable {

    protected int rawWidth;
    protected int rawHeight;
    private LPaint imageMask;
    protected byte[] rawImage;
    protected Object nativeImage;

    public LRasterImage(int width, int height) {
        super(0, 0, width, height);
        this.rawWidth = width;
        this.rawHeight = height;
        this.imageMask = null;
        /*this.moveTo(0, 0);
        this.lineTo(width, 0);
        this.lineTo(width, height);
        this.lineTo(0, height);
        this.closePath();*/
    }

    @Override
    public void read(InputStream is) throws IOException {
        var ois = new ObjectInputStream(is);
        rawWidth = ois.readInt();
        rawHeight = ois.readInt();
        this.setWidth(rawWidth);
        this.setHeight(rawHeight);
        rawImage = new byte[rawWidth * rawHeight * 4];
        ois.readFully(rawImage, 0, rawImage.length);
    }

    @Override
    public void write(OutputStream os) throws IOException {     
        var oos = new ObjectOutputStream(os);
        oos.writeInt(rawWidth);
        oos.writeInt(rawHeight);
        oos.write(rawImage, 0, rawImage.length);
    }

    /*@Override
    protected void createPath() {
        countPoints = numCoords = 0;
        if ((LGeomUtils.isNotEqual(getWidth(), 0.0)) && (LGeomUtils.isNotEqual(getHeight(), 0.0))) {            
            double[] xpoints = new double[4];
            double[] ypoints = new double[4];
            xpoints[0] = getX();
            ypoints[0] = getY();
            xpoints[1] = getX() + getWidth();
            ypoints[1] = getY();
            xpoints[2] = getX() + getWidth();
            ypoints[2] = getY() + getHeight();
            xpoints[3] = getX();
            ypoints[3] = getY() + getHeight();
            this.createPath(xpoints, ypoints, null, true);
        }
    }*/
    public int getRawWidth() {
        return rawWidth;
    }

    public int getRawHeight() {
        return rawHeight;
    }

    public boolean isImageMask() {
        return (imageMask != null);
    }

    public LPaint getImageMask() {
        return imageMask;
    }

    public void setImageMask(LPaint imageMask) {
        this.imageMask = imageMask;
    }

    public Object getNativeImage() {
        return nativeImage;
    }

    public void setNativeImage(Object nativeImage) {
        this.nativeImage = nativeImage;
        if (this.nativeImage != null) {
            //delete image data
            //this.rawImage = null;
        }
    }

    public byte[] getRawImage() {
        return rawImage;
    }

    public void setRawImage(byte[] rawImage) {
        this.rawImage = rawImage;
    }

    @Override
    public void execute(LCanvasRenderer canvasRenderer, long timeLine) {
        canvasRenderer.drawImage(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [x=" + getX() + "; y=" + getY() + "; width=" + getWidth() + "; height=" + getHeight()
                + "; rawWidth=" + getRawWidth() + "; rawHeight=" + getRawHeight() + "]";
    }
    
}
