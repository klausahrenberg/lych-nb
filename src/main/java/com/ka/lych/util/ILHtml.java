package com.ka.lych.util;

import com.ka.lych.observable.LString;

/**
 *
 * @author klausahrenberg
 */
public interface ILHtml {
    
    public final static String ML = "<";
    public final static String MR = ">";
    public final static String MC = "/";
    
    public final static String LINE_BREAK = open("br");
    
    public final static String BOLD = open("b");
    public final static String BOLD_CLOSE = close("b");
    public final static String HTML = open("html");
    public final static String HTML_CLOSE = close("html");
    public final static String ITALIC = open("i");
    public final static String ITALIC_CLOSE = close("i");
    public final static String SMALL = open("small");
    public final static String SMALL_CLOSE = close("small");
    
    
    private static String open(String tag) {
        return ML + tag + MR;
    }
    
    private static String close(String tag) {
        return ML + MC + tag + MR;
    }
    
    public static boolean contains(String text, String htmlTag) {
        return (!LString.isEmpty(text) ? text.toLowerCase().equals(htmlTag) : false);
    }
    
    public static boolean isHtmlFormatted(String text) {        
        return ((text.startsWith(HTML)) && text.endsWith(HTML_CLOSE));
    }
    
    public static String toHtmlFormatted(String text) {
        text = HTML + text + HTML_CLOSE;        
        return text;
    }
    
    public static String removeHtmlFormatted(String text) {
        if (isHtmlFormatted(text)) {
            return text.substring(0, text.length() - HTML_CLOSE.length()).substring(HTML.length());
        } else {
            return text;
        }
    }
    
    public static String bold(String text) {
        return BOLD + text + BOLD_CLOSE;
    }
    
    public static String italic(String text) {
        return ITALIC + text + ITALIC_CLOSE;
    }
    
    public static String small(String text) {
        return SMALL + text + SMALL_CLOSE;
    }
    
}
