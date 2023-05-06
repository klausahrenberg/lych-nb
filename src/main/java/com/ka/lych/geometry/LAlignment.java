package com.ka.lych.geometry;

import static com.ka.lych.geometry.LPositionH.LEFT;
import static com.ka.lych.geometry.LPositionH.RIGHT;
import static com.ka.lych.geometry.LPositionV.BASELINE;
import static com.ka.lych.geometry.LPositionV.BOTTOM;
import static com.ka.lych.geometry.LPositionV.TOP;

/**
 *
 * @author klausahrenberg
 */
public enum LAlignment {
    
    TOP_LEFT(TOP, LEFT),

    /**
     * Represents positioning on the top vertically and on the center horizontally.
     */
    TOP_CENTER(TOP, LPositionH.CENTER),

    /**
     * Represents positioning on the top vertically and on the right horizontally.
     */
    TOP_RIGHT(TOP, RIGHT),

    /**
     * Represents positioning on the center vertically and on the left horizontally.
     */
    CENTER_LEFT(LPositionV.CENTER, LEFT),

    /**
     * Represents positioning on the center both vertically and horizontally.
     */
    CENTER(LPositionV.CENTER, LPositionH.CENTER),

    /**
     * Represents positioning on the center vertically and on the right horizontally.
     */
    CENTER_RIGHT(LPositionV.CENTER, RIGHT),

    /**
     * Represents positioning on the bottom vertically and on the left horizontally.
     */
    BOTTOM_LEFT(BOTTOM, LEFT),

    /**
     * Represents positioning on the bottom vertically and on the center horizontally.
     */
    BOTTOM_CENTER(BOTTOM, LPositionH.CENTER),

    /**
     * Represents positioning on the bottom vertically and on the right horizontally.
     */
    BOTTOM_RIGHT(BOTTOM, RIGHT),

    /**
     * Represents positioning on the baseline vertically and on the left horizontally.
     */
    BASELINE_LEFT(BASELINE, LEFT),

    /**
     * Represents positioning on the baseline vertically and on the center horizontally.
     */
    BASELINE_CENTER(BASELINE, LPositionH.CENTER),

    /**
     * Represents positioning on the baseline vertically and on the right horizontally.
     */
    BASELINE_RIGHT(BASELINE, RIGHT);
    
    private final LPositionV vpos;
    private final LPositionH hpos;

    private LAlignment(LPositionV vpos, LPositionH hpos) {
        this.vpos = vpos;
        this.hpos = hpos;
    }

    /**
     * Returns the vertical positioning/alignment.
     * @return the vertical positioning/alignment.
     */
    public LPositionV getVpos() {
        return vpos;
    }

    /**
     * Returns the horizontal positioning/alignment.
     * @return the horizontal positioning/alignment.
     */
    public LPositionH getHpos() {
        return hpos;
    }
    
}
