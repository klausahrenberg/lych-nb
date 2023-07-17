package com.ka.lych.util;

/**
 *
 * @author klausahrenberg
 */
public class LCrypt {

    protected static String deCrypt(String crypted) {
        String result = "";
        if ((crypted != null) && (crypted.length() > 0)) {
            int keyint = (int) crypted.charAt(crypted.length() - 1);
            for (int i = 0; i < crypted.length() - 1; ++i) {
                result = result + (char) (crypted.charAt(i) ^ keyint);
            }
        }
        return result;
    }

    protected static String enCrypt(char[] value) {
        String result = "";
        if ((value != null) && (value.length > 0)) {
            int keyint = (int) (Math.random() * 64 + 63);
            for (int i = 0; i < value.length; ++i) {
                result = result + (char) (value[i] ^ keyint);
            }
            result = result + (char) keyint;
        }
        return result;
    }

    /*protected static String enCrypt(String value) {
        String result = "";
        if ((value != null) && (value.length() > 0)) {
            int keyint = (int) (Math.random() * 64 + 63);
            for (int i = 0; i < value.length(); ++i) {
                result = result + (char) (value.charAt(i) ^ keyint);
            }
            result = result + (char) keyint;
        }
        return result;
    }*/
    public static String deCryptEx(String crypted) {
        int i = 0;
        String s = "";
        while ((crypted != null) && (i < crypted.length() - 1)) {
            try {
                s = s + (char) (LNumberSystem.digitsToInt(crypted.substring(i, i + 2), LNumberSystem.DIGITS_CODE_36));
            } catch (Exception e) {
            }
            i++;
            i++;
        }
        return deCrypt(s);
    }

    public static String enCryptEx(char[] value) {
        String s = enCrypt(value);
        String result = "";
        for (int i = 0; i < s.length(); ++i) {
            result = result + LNumberSystem.intToDigits(s.charAt(i), LNumberSystem.DIGITS_CODE_36, 2, true);
        }
        return result;
    }

    public static String enCryptEx(String value) {
        return enCryptEx(value != null ? value.toCharArray() : null);
    }

}
