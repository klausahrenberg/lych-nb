package com.ka.lych.geometry;

import java.util.Objects;

/**
 *
 * @author klausahrenberg
 */
public class LGeomUtils {

    public static final double DEFAULT_DOUBLE_PRECISION = 0.00001;

    public static boolean isEqual(Double a, Double b) {
        return isEqual(a, b, DEFAULT_DOUBLE_PRECISION);
    }
    
    public static boolean isEqual(Double a, Double b, double precision) {
        if ((a != null) && (b != null)) {
            double diff = a - b;
            return ((diff < precision) && (-diff < precision));
        } else {
            return Objects.equals(a, b);
        }
    }

    public static boolean isNotEqual(Double a, Double b) {
        return !isEqual(a, b, DEFAULT_DOUBLE_PRECISION);
    }
    
    public static boolean isNotEqual(Double a, Double b, double precision) {
        return !isEqual(a, b, precision);
    }    
    
    public static boolean isEqual(Integer a, Integer b) {
        if ((a != null) && (b != null)) {            
            return (a.intValue() == b.intValue());
        } else {
            return Objects.equals(a, b);
        }
    }

    public static boolean isNotEqual(Integer a, Integer b) {
        return !isEqual(a, b);
    }    
    
    public static boolean isEqual(Double[] a, Double[] b) {
        return isEqual(a, b, DEFAULT_DOUBLE_PRECISION);
    }
    
    public static boolean isEqual(Double[] a, Double[] b, double precision) {
        if ((a != null) && (b != null)) {
            if (a.length == b.length) {
                for (int i = 0; i < a.length; i++) {
                    if (!isEqual(a[i], b[i], precision)) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }    
        } else {
            return Objects.equals(a, b);
        }
    }

    public static boolean isEqualOrGreater(double baseValue, double valueThatShouldBeEqualOrGreater, double precision) {
        return ((valueThatShouldBeEqualOrGreater >= baseValue) || ((baseValue - valueThatShouldBeEqualOrGreater) < precision));
    }

    public static boolean isEqualOrLess(double baseValue, double valueThatShouldBeEqualOrLess, double precision) {
        return ((valueThatShouldBeEqualOrLess <= baseValue) || ((valueThatShouldBeEqualOrLess - baseValue) < precision));
    }
    
    /**
     * Compares function, if the value is within the given limits or not. If
     * null values are given for one limit, the function returns true, if the
     * value fulfill the other limit.
     *
     * @param value - value which should be within the limits
     * @param lowerLimit - lowerLimit or null, if there is no limit
     * @param upperLimit - upperLimit or null, if there is no limit
     * @param precision
     * @return
     */
    public static boolean isWithinLimits(Double value, Double lowerLimit, Double upperLimit, double precision) {
        return ((value == null) ||
                (((lowerLimit == null) || (isEqualOrGreater(lowerLimit, value, precision)))
                && ((upperLimit == null) || (isEqualOrLess(upperLimit, value, precision)))));
    }

    public static boolean isWithinLimits(Integer value, Integer lowerLimit, Integer upperLimit) {
        return ((value == null) ||
                (((lowerLimit == null) || (value >= lowerLimit))
                 && ((upperLimit == null) || (value <= upperLimit))));
    }

    public static boolean isWithinLimits(long value, Long lowerLimit, Long upperLimit) {
        return (((lowerLimit == null) || (value >= lowerLimit))
                && ((upperLimit == null) || (value <= upperLimit)));
    }

    public static double compareDoubles(double doubleValue1, double doubleValue2, double precision) {
        double diff = doubleValue1 - doubleValue2;
        if ((diff < precision) && (-diff < precision)) {
            return 0.0;
        } else {
            return doubleValue1 - doubleValue2;
        }
    }

    /**
     * Calculate the angle between a center and a target point in clockwise
     * coord system. E.g center(0, 0): target(0, 1) > 0°; target(1, 0) > 90°;
     * target(0, 1) > 180°; target(0, -1) > 270°
     *
     * @param centerX
     * @param centerY
     * @param targetX
     * @param targetY
     * @return a value between 0 and 360
     */
    public static double getAngle(double centerX, double centerY, double targetX, double targetY) {
        double angle = Math.sqrt((targetX - centerX) * (targetX - centerX) + (centerY - targetY) * (centerY - targetY));
        angle = Math.acos((centerY - targetY) / angle);
        if (targetX < centerX) {
            angle = -angle * 360 / (2 * Math.PI) + 360;
        } else {
            angle = angle * 360 / (2 * Math.PI);
        }
        return angle;
    }

}
