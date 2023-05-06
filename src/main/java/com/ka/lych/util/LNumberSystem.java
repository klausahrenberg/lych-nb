package com.ka.lych.util;


/**
 *
 * @author klausahrenberg
 */
public class LNumberSystem {

    public final static char[] DIGITS_DECIMAL = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    public final static char[] DIGITS_HEXA_DECIMAL = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F'};

    public final static char[] DIGITS_CODE_26 = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z'};

    public final static char[] DIGITS_CODE_36 = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z'};

    public static String intToDigits(int value, char[] digits, int valueStringLength, boolean strict) {
        if ((digits == null) || (digits.length < 2)) {
            throw new IllegalArgumentException("digits are not defined " + digits);
        }
        String result = "";
        int max = (int) Math.pow(digits.length, 4);
        if (!strict) {
            max = value +1;
        } else if (valueStringLength > 0) {
            max = (int) Math.pow(digits.length, valueStringLength);
        }
        if ((value >= 0) && (value < max)) {
            int i = 1;
            int v = value;
            while (v >= digits.length) {
                v = v / digits.length;
                i++;
            }
            v = value;
            for (int j = i; j >= 1; j--) {
                result = result + digits[v / ((int) Math.pow(digits.length, j - 1))];
                v = v - (v / ((int) Math.pow(digits.length, j - 1))) * (int) Math.pow(digits.length, j - 1);
            }
            if ((valueStringLength > 0) && (result.length() < valueStringLength)) {
                for (i = result.length(); i < valueStringLength; i++) {
                    result = digits[0] + result;
                }
            }
        } else {
            throw new IllegalArgumentException("Value is out of bounds. value=" + value + "; max=" + (max - 1));
        }
        return result;
    }

    private static int getCharIndex(char digit, char[] digits) {
        int result = -1;
        for (int i = 0; i < 36; i++) {
            if (digit == digits[i]) {
                result = i;
                break;
            }
        }
        return result;
    }

    public static int getMaximum(char[] digits, int valueStringLength) {
        if ((digits == null) || (digits.length < 2)) {
            throw new IllegalArgumentException("digits are not defined " + digits);
        }
        return (int) Math.pow(digits.length, valueStringLength);
    }

    public static int digitsToInt(String value, char[] digits) throws LParseException {
        if ((digits == null) || (digits.length < 2)) {
            throw new IllegalArgumentException("digits are not defined " + digits);
        }
        int result = 0;
        int max = 1;        
        while ((int) Math.pow(digits.length, max) <= 2147483646){                  
            max++;
        }
        value = value.toUpperCase();
        if (value.length() <= max) {
            for (int i = 0; i <= value.length() - 1; i++) {
                int b = getCharIndex(value.charAt(i), digits);
                if (b > -1) {
                    result = result + b * (int) (Math.pow(digits.length, value.length() - i - 1));
                } else {
                    throw new LParseException(LNumberSystem.class, "Invalid number: '" + value + "'");
                }
            }
        } else {
            throw new LParseException(LNumberSystem.class, "Value out of range: '" + value + "' / max: " + max);
        }
        return result;
    }

    public static boolean isValidDigit(char ch, char[] digits) {
        return ((LArrays.contains(digits, Character.toLowerCase(ch))) || (LArrays.contains(digits, Character.toUpperCase(ch))));
    }

}
