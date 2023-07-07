package com.ka.lych.util;

import java.nio.ByteBuffer;

/**
 *
 * @author klausahrenberg
 */
public class LBitIterator {

    protected final static int BITS_PER_BYTE = 8;
    protected ByteBuffer buffer;
    protected long bitPosition;
    protected final int bitsPerStep;
    private final boolean normalizeResult;
    
    private int factor;
    private byte currentByte;
    private int currentBytePosition;
    private int bitPositionInCurrentByte;
    

    public LBitIterator(ByteBuffer buffer, int bitsPerStep, boolean normalizeResult) {
        this.buffer = buffer;
        this.bitPosition = 0;
        this.currentBytePosition = -1;
        this.bitsPerStep = bitsPerStep;
        this.normalizeResult = normalizeResult;
    }
    
    public void reset() {
        bitPosition = 0;
        currentBytePosition = -1;
    }

    public boolean hasNext() {
        return ((bitPosition + bitsPerStep) <= (buffer.capacity() * BITS_PER_BYTE));
    }       

    private int getBit(byte b, int bitNumber) {
        return ((b & (1 << bitNumber)) != 0 ? 1 : 0);
    }

    public void skip(int bitsToSkip) {
        bitPosition += bitsToSkip;
    }
    
    public void skipToNextByte() {
        if ((bitPosition % BITS_PER_BYTE) != 0) {
            bitPosition = ((int) (bitPosition / BITS_PER_BYTE)) * BITS_PER_BYTE + BITS_PER_BYTE;
        }
    }
    
    public double next() {
        double result = 0;
        if (bitsPerStep == 8) {
            result = (buffer.get((int) (bitPosition / 8)) & 0xFF);
            bitPosition += bitsPerStep;
        } else {
            factor = bitsPerStep;
            for (int i = 0; i < bitsPerStep; i++) {
                int bp = (int) (bitPosition / 8);
                if (bp != currentBytePosition) {
                    currentByte = buffer.get(bp);
                    currentBytePosition = bp;
                }
                bitPositionInCurrentByte = -1 * (((int) (bitPosition % 8)) - 7);
                
                result += getBit(currentByte, bitPositionInCurrentByte) * (1 << factor) / 2;  //= Math.pow(2, factor) / 2
                bitPosition++;
                factor--;
            }
            if (normalizeResult) {
                switch (bitsPerStep) {
                    case 1 : result *= 255;
                             break;
                    case 2 : result *= 85;
                             break;
                    case 4 : result *= 17;
                }
            }
        }
        
        return result;
    }

    public long getBitPosition() {
        return bitPosition;
    }        

}
