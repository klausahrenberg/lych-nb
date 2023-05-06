package com.ka.lych.geometry;

/**
 *
 * @author klausahrenberg
 */
public enum LDirection { 
    
    LEFT_TO_RIGHT, RIGHT_TO_LEFT, TOP_TO_BOTTOM, BOTTOM_TO_TOP;

    public LDirection getOpposite() {
        switch (this) {
            case LEFT_TO_RIGHT : return RIGHT_TO_LEFT;
            case RIGHT_TO_LEFT : return LEFT_TO_RIGHT;
            case TOP_TO_BOTTOM : return BOTTOM_TO_TOP;
            default /*BOTTOM_TO_TOP*/ : return TOP_TO_BOTTOM;
        }
    }
    
}
