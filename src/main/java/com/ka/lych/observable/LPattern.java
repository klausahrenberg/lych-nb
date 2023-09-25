package com.ka.lych.observable;

import com.ka.lych.exception.LParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.ka.lych.list.LYosos;
import com.ka.lych.util.ILConstants;
import com.ka.lych.util.LNumberSystem;
import com.ka.lych.xml.LXmlUtils;

/**
 * This class provides a string property which is build by a pattern and a int
 * value For the int value the following patterns are allowed: - 0 decimal
 * number [0..9] - X hexa decimal number [0..9; A..F] - A alphabetic number with
 * 26 characters per digit [A..Z] - C alphabetic number with 36 characters per
 * digit [0..9; A..Z]
 *
 * Additionally some date fields are included - Y year number, 2 or 4 digits - M
 * month [1..12] - D day [1..31] - H hour [0..23] - M minutes [0..59]
 *
 * A pattern is given via setPattern() method. The pattern string can include
 * the elements above, e.g.:
 *
 * '"Note "000-XX'
 *
 * The example has a fixed String "Note " at the beginning, followed by a
 * 3-digit-decimal number, finished by a 2-digit-hex-number. If the intValue
 * starts at 0, the first value is:
 *
 * 'Note 000-00'
 *
 * For further values the resulting strings are:
 *
 * intValue 10: 'Note 000-0A' intValue 16: 'Note 000-10' intValue 255: 'Note
 * 000-FF' intValue 256: 'Note 001-00'
 *
 * @author klausahrenberg
 */
public class LPattern extends LString
        implements ILConstants {

    private final static char KEYWORD_DECIMAL = '0';
    private final static char KEYWORD_HEX = 'X';
    private final static char KEYWORD_CODE_26 = 'A';
    private final static char KEYWORD_CODE_36 = 'C';
    private final static char KEYWORD_YEAR = 'Y';
    private final static char KEYWORD_MONTH = 'M';
    private final static char KEYWORD_DAY = 'D';
    private final static char KEYWORD_HOUR = 'H';
    private final static char KEYWORD_MINUTE = 'm';
    private final static char KEYWORD_SECOND = 's';
    private static char[] CONTROL_DIGITS = {' ', '-', '_', '.', ',', ';', ':'};

    private LYosos<LPatternElement> elements;
    private int counterStart, counterIncrement;
    private Integer numberValue;
    //private String pattern;
    private boolean strictPattern;

    public LPattern() {
        this(null);
    }

    public LPattern(String pattern) {
        super();
        elements = new LYosos<>();
        counterStart = 0;
        counterIncrement = 1;
        numberValue = null;
        strictPattern = false;
        if (!LString.isEmpty(pattern)) {
            try {
                setPattern(pattern);
            } catch (LParseException pe) {
                throw new IllegalArgumentException(pe.getMessage(), pe);
            }
        }
    }

    @Override
    public String toLocalizedString() {
        return super.toLocalizedString();
    }

    @Override
    public String toParseableString() {
        return super.toParseableString();
    }

    @Override
    public void parseLocalized(String value) throws LParseException {
        this.parse(value);
    }

    @Override
    public void parse(String value) throws LParseException {
        this.numberValue = null;
        if (!LString.isEmpty(value)) {
            for (LPatternElement element : elements) {
                value = element.parseValue(value);
            }
            buildAndSetStringValue();
        } else {
            set(null);
            throw new LParseException("No value given.");
        }
    }

    private void safePatternBuffer(String buffer, LYosos<String> storage) {
        if (buffer.length() > 0) {
            storage.add(buffer);
        }
    }

    private LYosos<String> splitPattern(String value) {
        LYosos<String> result = new LYosos<>();
        if (value == null) {
            throw new IllegalArgumentException("value can't be null");
        }
        int i = 0;
        boolean inString = false;
        boolean ctrlChar = false;
        boolean inFormula = false;
        String buffer = "";
        char[] vChars = value.toCharArray();
        while (i < vChars.length) {
            char ch = vChars[i];
            if (ch == '\"') {
                if (!inString) {
                    safePatternBuffer(buffer, result);
                    buffer = String.valueOf(ch);
                    inString = true;
                } else if (!ctrlChar) {
                    buffer += ch;
                    inString = false;
                    ctrlChar = false;
                    safePatternBuffer(buffer, result);
                    buffer = "";
                } else {
                    buffer += ch;
                }
            } else if (ch == '\\') {
                ctrlChar = true;
            } else if (inString) {
                buffer += ch;
                ctrlChar = false;
            } else if ((ch == '(') && (!inFormula)) {
                safePatternBuffer(buffer, result);
                buffer = String.valueOf(ch);
                inFormula = true;
            } else if ((ch == ')') && (inFormula)) {
                inFormula = false;
                buffer += ch;
                safePatternBuffer(buffer, result);
                buffer = "";
            } else if (inFormula) {
                buffer += ch;
            } else if (LNumberSystem.isValidDigit(ch, CONTROL_DIGITS)) {
                if ((buffer.length() > 0) && (!LNumberSystem.isValidDigit(buffer.charAt(0), CONTROL_DIGITS))) {
                    safePatternBuffer(buffer, result);
                    buffer = String.valueOf(ch);
                } else {
                    buffer += ch;
                }
            } else if ((buffer.length() > 0) && (buffer.charAt(0) != ch)) {
                safePatternBuffer(buffer, result);
                buffer = String.valueOf(ch);
            } else {
                buffer += ch;
            }
            i++;
        }
        safePatternBuffer(buffer, result);
        return (result.size() > 0 ? result : null);
    }

    public void setPattern(String value) throws LParseException {        
        elements.clear();
        counterStart = 0;
        counterIncrement = 1;
        //numberValue = 0;
        LYosos<String> tokens = splitPattern(value);

        if (tokens != null) {
            //String[] tokens = value.splitPattern(KEYWORD_SEPARATOR);//"\\+");
            for (String token : tokens) {
                switch (token.charAt(0)) {
                    case '\"':
                        if ((token.charAt(0) == '\"') && (token.charAt(token.length() - 1) == '\"')) {
                            elements.add(new LStringElement(token.substring(1, token.length() - 1)));
                        } else {
                            throw new LParseException("Illegal part of value: %s", value);
                        }
                        break;
                    case KEYWORD_DECIMAL:
                        elements.add(new LNumberElement(token, KEYWORD_DECIMAL, LNumberSystem.DIGITS_DECIMAL));
                        break;
                    case KEYWORD_HEX:
                        elements.add(new LNumberElement(token, KEYWORD_HEX, LNumberSystem.DIGITS_HEXA_DECIMAL));
                        break;
                    case KEYWORD_CODE_26:
                        elements.add(new LNumberElement(token, KEYWORD_CODE_26, LNumberSystem.DIGITS_CODE_26));
                        break;
                    case KEYWORD_CODE_36:
                        elements.add(new LNumberElement(token, KEYWORD_CODE_36, LNumberSystem.DIGITS_CODE_36));
                        break;
                    case KEYWORD_YEAR:
                        elements.add(new LYearElement(token));
                        break;
                    case KEYWORD_MONTH:
                        elements.add(new LMonthElement(token));
                        break;
                    case KEYWORD_DAY:
                    case KEYWORD_HOUR:
                    case KEYWORD_MINUTE:
                    case KEYWORD_SECOND:
                        elements.add(new LDayElement(token));
                        break;
                    case '(':
                        configureCounter(token);
                        break;
                    default:
                        if (LNumberSystem.isValidDigit(token.charAt(0), CONTROL_DIGITS)) {
                            elements.add(new LControlDigitElement(token));
                        } else {
                            throw new LParseException("Illegal token: %s", token);
                        }
                }
            }
            int divisor = 1;
            LNumberElement lastElement = null;
            for (int i = elements.size() - 1; i >= 0; i--) {
                if (elements.get(i) instanceof LNumberElement) {
                    LNumberElement nElement = (LNumberElement) elements.get(i);
                    nElement.setDivisor(divisor);
                    divisor = divisor * nElement.getMaximum();
                    lastElement = nElement;
                }
            }
            if (lastElement != null) {
                lastElement.lastElement = true;
            }
            buildAndSetStringValue();
        } else {
            throw new IllegalArgumentException("Invalid value: " + value);
        }        
    }

    private void configureCounter(String value) throws LParseException {
        if ((value.charAt(0) == '(') && (value.charAt(value.length() - 1) == ')')) {
            String[] cDetails = value.substring(1, value.length() - 1).split(";");
            try {
                for (String cDetail : cDetails) {
                    int eq = cDetail.indexOf("=");

                    if (eq > -1) {
                        switch (cDetail.substring(0, eq)) {
                            case "start":
                                counterStart = Integer.valueOf(cDetail.substring(eq + 1));
                                break;
                            case "increment":
                                counterIncrement = Integer.valueOf(cDetail.substring(eq + 1));
                                break;
                        }
                    } else {
                        throw new LParseException("Incomplete counter parameter: %s", cDetail);
                    }
                }
            } catch (NumberFormatException nfe) {
                throw new LParseException(nfe);
            }
        } else {
            throw new LParseException("Illegal part of value: %s", value);
        }
    }

    private void buildAndSetStringValue() {
        String value = (numberValue != null && elements.size() > 0 ? "" : null);
        if (numberValue != null) {
            for (LPatternElement element : elements) {
                value += element.toParseableString();
            }
        }
        this.set(value);
    }

    public Integer getIntValue() {
        return numberValue;
    }
    
    public void setNumberValue(Integer numberValue) {
        if ((this.numberValue == null) || (!this.numberValue.equals(numberValue))) {               
            this.numberValue = numberValue;
            this.buildAndSetStringValue();
        }
    }

    private void addIntValue(int summand) {
        if (this.numberValue == null) {
            throw new IllegalStateException("numberValue is null. Can't add something to null value.");
        }
        this.numberValue = this.numberValue + summand;
    }

    public boolean isStrictPattern() {
        return strictPattern;
    }

    private abstract class LPatternElement {

        public abstract void parsePattern(String value) throws LParseException;

        public abstract String toParseableString();

        public abstract String parseValue(String value) throws LParseException;

        @Override
        public String toString() {
            return LXmlUtils.classToString(this);
        }

    }

    private class LStringElement extends LPatternElement {

        private String value;

        public LStringElement(String p) throws LParseException {
            this.parsePattern(p);
        }

        @Override
        public void parsePattern(String value) throws LParseException {
            this.value = value;
        }

        @Override
        public String toParseableString() {
            return value;
        }

        @Override
        public String parseValue(String value) throws LParseException {
            String trimmedValue = this.value.trim().toLowerCase();
            while ((value.length() > 0) && (value.charAt(0) == ' ')) {
                value = value.substring(1);
            }
            if (value.toLowerCase().startsWith(trimmedValue)) {
                value = value.substring(trimmedValue.length());
                return value;
            } else {                
                throw new LParseException("invalid parsing value: '%s'", value);
            }            
        }
    }

    private class LControlDigitElement extends LPatternElement {

        private String value;

        public LControlDigitElement(String p) throws LParseException {
            this.parsePattern(p);
        }

        @Override
        public void parsePattern(String value) throws LParseException {
            this.value = value;
        }

        @Override
        public String toParseableString() {
            return value;
        }

        @Override
        public String parseValue(String value) throws LParseException {
            if ((value.length() > 0) && (LNumberSystem.isValidDigit(value.charAt(0), CONTROL_DIGITS))) {
                while ((value.length() > 0) && (LNumberSystem.isValidDigit(value.charAt(0), CONTROL_DIGITS))) {
                    value = value.substring(1);
                }
                return value;
            } else {
                throw new LParseException("invalid parsing value: '%s'", value);
            }
        }

    }

    private abstract class LDatetimeElement extends LPatternElement {

        protected LocalDateTime value;
        protected DateTimeFormatter parseFormat, outputFormat;

        public LDatetimeElement(String p) throws LParseException {
            parseFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            value = LocalDateTime.now();
            this.parsePattern(p);
        }

        @Override
        public String toParseableString() {
            return (value != null && outputFormat != null ? outputFormat.format(value) : null);
        }

    }

    private class LYearElement extends LDatetimeElement {

        public LYearElement(String p) throws LParseException {
            super(p);
        }

        @Override
        public void parsePattern(String value) throws LParseException {
            int digits = 0;
            while ((value.length() > 0) && (value.charAt(0) == KEYWORD_YEAR)) {
                digits++;
                value = value.substring(1);
            }
            if ((digits == 2) || (digits == 4)) {
                outputFormat = DateTimeFormatter.ofPattern(digits == 2 ? "uu" : "uuuu");
            } else {
                outputFormat = null;
                throw new LParseException("Year component must be 2 or 4 digits. Illegal length: %s", digits);
            }
        }

        @Override
        public String parseValue(String value) throws LParseException {
            while ((value.length() > 0) && (LNumberSystem.isValidDigit(value.charAt(0), CONTROL_DIGITS))) {
                value = value.substring(1);
            }
            String v2 = "";
            while ((value.length() > 0) && (v2.length() < 4) && (LNumberSystem.isValidDigit(value.charAt(0), LNumberSystem.DIGITS_DECIMAL))) {
                v2 = v2 + value.charAt(0);
                value = value.substring(1);
            }
            this.value = LocalDateTime.parse(v2 + "-01-01T00:00:00", parseFormat);
            //this.value = LocalDate.of(Integer.valueOf(v2), 1, 1);            
            return value;
        }

    }

    private class LMonthElement extends LDatetimeElement {

        public LMonthElement(String p) throws LParseException {
            super(p);
        }

        @Override
        public void parsePattern(String value) throws LParseException {
            int digits = 0;
            while ((value.length() > 0) && (value.charAt(0) == KEYWORD_MONTH)) {
                digits++;
                value = value.substring(1);
            }
            if ((digits >= 1) && (digits <= 3)) {
                outputFormat = DateTimeFormatter.ofPattern(digits == 3 ? "MMM" : "MM");
            } else {
                outputFormat = null;
                throw new LParseException("Month component must be 1 to 3 digits. Illegal length: %s", digits);
            }
        }

        @Override
        public String parseValue(String value) throws LParseException {
            while ((value.length() > 0) && (LNumberSystem.isValidDigit(value.charAt(0), CONTROL_DIGITS))) {
                value = value.substring(1);
            }
            String v2 = "";
            if (value.length() > 0) {
                if (LNumberSystem.isValidDigit(value.charAt(0), LNumberSystem.DIGITS_DECIMAL)) {
                    while ((value.length() > 0) && (v2.length() < 2) && (LNumberSystem.isValidDigit(value.charAt(0), LNumberSystem.DIGITS_DECIMAL))) {
                        v2 = v2 + value.charAt(0);
                        value = value.substring(1);
                    }
                } else {
                    v2 = value.substring(0, 3);
                    value = value.substring(3);
                }
            }

            parseFormat = DateTimeFormatter.ofPattern("uuuu-" + (v2.length() == 3 ? "MMM" : "MM") + "-dd HH:mm:ss");
            this.value = LocalDateTime.parse("2008-" + (v2.length() == 1 ? "0" : "") + v2 + "-01 00:00:00", parseFormat);
            return value;
        }

    }

    private class LDayElement extends LDatetimeElement {

        private char keyword;
        private String prefix, suffix;

        public LDayElement(String p) throws LParseException {
            super(p);
        }

        @Override
        public void parsePattern(String value) throws LParseException {
            int digits = 0;
            while ((value.length() > 0) && ((value.charAt(0) == KEYWORD_DAY) || (value.charAt(0) == KEYWORD_HOUR) || (value.charAt(0) == KEYWORD_MINUTE) || (value.charAt(0) == KEYWORD_SECOND))) {
                keyword = value.charAt(0);
                digits++;
                value = value.substring(1);
            }
            if ((digits >= 1) && (digits <= 2)) {
                switch (keyword) {
                    case KEYWORD_DAY:
                        outputFormat = DateTimeFormatter.ofPattern("dd");
                        prefix = "2008-03-";
                        suffix = "T17:10:00";
                        break;
                    case KEYWORD_HOUR:
                        outputFormat = DateTimeFormatter.ofPattern("HH");
                        prefix = "2008-03-10T";
                        suffix = ":10:00";
                        break;
                    case KEYWORD_MINUTE:
                        outputFormat = DateTimeFormatter.ofPattern("mm");
                        prefix = "2008-03-10T17";
                        suffix = ":00";
                        break;
                    case KEYWORD_SECOND:
                        outputFormat = DateTimeFormatter.ofPattern("ss");
                        prefix = "2008-03-10T17:10:";
                        suffix = "";
                        break;
                }
            } else {
                outputFormat = null;
                throw new LParseException("Month component must be 1 to 2 digits. Illegal length: %s", digits);
            }
        }

        @Override
        public String parseValue(String value) throws LParseException {
            while ((value.length() > 0) && (LNumberSystem.isValidDigit(value.charAt(0), CONTROL_DIGITS))) {
                value = value.substring(1);
            }

            String v2 = "";
            while ((value.length() > 0) && (v2.length() < 4) && (LNumberSystem.isValidDigit(value.charAt(0), LNumberSystem.DIGITS_DECIMAL))) {
                v2 = v2 + value.charAt(0);
                value = value.substring(1);
            }
            this.value = LocalDateTime.parse(prefix + v2 + suffix, parseFormat);
            return value;
        }

    }

    private class LNumberElement extends LPatternElement {

        protected char keyword;
        protected char[] digits;
        protected int valueStringLength, divisor;
        protected boolean lastElement;

        public LNumberElement(String p, char keyword, char[] digits) throws LParseException {
            this.keyword = keyword;
            this.digits = digits;
            this.divisor = 1;
            this.lastElement = false;
            this.parsePattern(p);
        }

        public void setDivisor(int divisor) {
            this.divisor = divisor;
        }

        public int getMaximum() {
            return LNumberSystem.getMaximum(digits, valueStringLength);
        }

        @Override
        public void parsePattern(String value) throws LParseException {
            valueStringLength = 0;
            while ((value.length() > 0) && (value.charAt(0) == keyword)) {
                valueStringLength++;
                value = value.substring(1);
            }
        }

        @Override
        public String toParseableString() {
            if ((lastElement) && (isStrictPattern()) && ((getIntValue() / divisor) > getMaximum())) {
                throw new IllegalStateException("Value is out of bounds. value=" + (getIntValue() / divisor) + "; max=" + (getMaximum() - 1));
            }
            if ((lastElement) && (!isStrictPattern())) {
                return LNumberSystem.intToDigits((getIntValue() / divisor), digits, valueStringLength, isStrictPattern());
            } else {
                return LNumberSystem.intToDigits((getIntValue() / divisor) % getMaximum(), digits, valueStringLength, isStrictPattern());
            }
        }

        @Override
        public String parseValue(String value) throws LParseException {
            while ((value.length() > 0) && (value.charAt(0) == ' ')) {
                value = value.substring(1);
            }
            if (LNumberSystem.isValidDigit(value.charAt(0), digits)) {
                String iS = "";
                while ((value.length() > 0) && (LNumberSystem.isValidDigit(value.charAt(0), digits))) {
                    iS += value.charAt(0);
                    value = value.substring(1);
                }
                try {
                    addIntValue(LNumberSystem.digitsToInt(iS, digits) * divisor);
                    return value;
                } catch (NumberFormatException nfe) {
                    throw new LParseException("Wrong number format: '%s'", iS);
                }
            } else {
                throw new LParseException("No number value: '%s'", value);
            }

        }

    }

}
