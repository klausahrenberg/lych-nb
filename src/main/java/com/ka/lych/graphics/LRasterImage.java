package com.ka.lych.graphics;

import com.ka.lych.util.ILBlobable;
import com.ka.lych.util.LLog;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author klausahrenberg
 */
public class LRasterImage extends LShape 
        implements ILBlobable {

    int _rawWidth;
    int _rawHeight;
    LPaint _imageMask;
    byte[] _rawImage;
    Object _nativeImage;

    public LRasterImage() {
        this(0, 0);
    }
    
    public LRasterImage(int width, int height) {
        super(0, 0, width, height);
        _rawWidth = width;
        _rawHeight = height;
        _imageMask = null;
    }

    @Override
    public void read(ObjectInputStream ois) throws IOException {
        _rawWidth = ois.readInt();
        _rawHeight = ois.readInt();
        width(_rawWidth);
        height(_rawHeight);
        _rawImage = new byte[_rawWidth * _rawHeight * 4];
        ois.readFully(_rawImage, 0, _rawImage.length);
    }

    @Override
    public void write(ObjectOutputStream oos) throws IOException {     
        oos.writeInt(_rawWidth);
        oos.writeInt(_rawHeight);
        oos.write(_rawImage, 0, _rawImage.length);
    }
    
    public int rawWidth() {
        return _rawWidth;
    }

    public int rawHeight() {
        return _rawHeight;
    }

    public boolean isImageMask() {
        return (_imageMask != null);
    }

    public LPaint imageMask() {
        return _imageMask;
    }

    public LRasterImage imageMask(LPaint imageMask) {
        _imageMask = imageMask;
        return this;
    }

    public Object nativeImage() {
        return _nativeImage;
    }

    public LRasterImage nativeImage(Object nativeImage) {
        _nativeImage = nativeImage;  
        return this;
    }

    public byte[] rawImage() {
        return _rawImage;
    }

    public LRasterImage rawImage(byte[] rawImage) {
        _rawImage = rawImage;
        return this;
    }

    @Override
    public void execute(LCanvasRenderer canvasRenderer, long timeLine) {
        canvasRenderer.drawImage(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [x=" + x().get() + "; y=" + y().get() + "; width=" + width().get() + "; height=" + height().get()
                + "; rawWidth=" + rawWidth() + "; rawHeight=" + rawHeight() + "]";
    }
    
}
