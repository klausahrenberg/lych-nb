package com.ka.lych.observable;

import com.ka.lych.exception.LParseException;
import com.ka.lych.list.LList;
import com.ka.lych.list.LMap;
import com.ka.lych.util.ILConstants;
import com.ka.lych.util.LReflections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 * @author klausahrenberg
 */
public class LString extends LObservable<String, LString>
        implements CharSequence {

    private String localized;

    public LString() {
    }

    public LString(Optional<String> initialValue) {
        super(initialValue.isPresent() ? initialValue.get() : null);
    }

    public LString(String initialValue) {
        super(initialValue);
    }

    @Override
    public LString clone() throws CloneNotSupportedException {
        return new LString(this.get());
    }

    /**
     * Checks, if value is empty
     *
     * @return true, if value is null or value is an empty string
     */
    @Override
    public boolean isEmpty() {
        return LString.isEmpty(get());
    }

    /**
     * Return the string value if present and not empty, otherwise return other.
     *
     * @param other
     * @return
     */
    public String orElseEmpty(String other) {
        return (isPresent() && !isEmpty() ? get() : other);
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || (obj instanceof String)) {
            return LString.equals(get(), (String) obj);
        } else if (obj instanceof LString) {
            return LString.equals(get(), ((LString) obj).get());
        } else {
            return false;
        }
    }

    public boolean equalsIgnoreCase(String anotherValue) {
        return LString.equalsIgnoreCase(get(), anotherValue);
    }

    public boolean contains(String subString) {
        return LString.contains(get(), subString);
    }

    public boolean containsIgnoreCase(String subString) {
        return LString.containsIgnoreCase(get(), subString);
    }

    @Override
    public void parse(String value) throws LParseException {
        this.set(value);
    }

    @Override
    public void parseLocalized(String value) throws LParseException {
        if (localized != null) {
            localized = value;
        }
        this.set(value);
    }

    @Override
    public String toParseableString() {
        return get();
    }

    @Override
    public String toLocalizedString() {
        return (localized != null ? localized : get());
    }

    public void setLocalized(String localized) {
        this.localized = localized;
    }

    public static String toUpperCaseFirstLetter(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    public static LString of(String value) {
        return new LString(value);
    }

    public static LString empty() {
        return of(null);
    }

    /**
     * Check, if string value is empty. Null value is possible.
     *
     * @param value
     * @return true, if value is null or value is empty (equals "")
     */
    public static boolean isEmpty(String value) {
        return ((value == null) || (value.isEmpty()));
    }

    public static boolean contains(String value, String subString) {
        return ((value != null) && (value.contains(subString)));
    }

    public static boolean containsIgnoreCase(String value, String subString) {
        return ((value != null) && (value.toLowerCase().contains(subString.toLowerCase())));
    }

    public static boolean equals(String value1, String value2) {
        return Objects.equals(value1, value2);
    }

    public static boolean equalsIgnoreCase(String value, String anotherValue) {
        return ((value != null) && (value.equalsIgnoreCase(anotherValue)));
    }

    public static String concat(Object... objects) {
        return concatWithSpacer("", "", objects);
    }

    public static String concatWithSpace(Object... objects) {
        return concatWithSpacer(" ", "", objects);
    }

    public static String concatWithComma(Object... objects) {
        return concatWithCommaIf(null, null, objects);
    }

    @SuppressWarnings("unchecked")
    public static <T> String concatWithCommaIf(Predicate<? super T> filter, Function<? super T, String> converter, T... objects) {
        return concatWithSpacerIf(filter, converter, ", ", "", objects);
    }

    @SuppressWarnings("unchecked")
    public static <T> String concatWithCommaIf(Predicate<? super T> filter, Function<? super T, String> converter, List<T> objects) {
        return concatWithSpacerIf(filter, converter, ", ", "", (T[]) objects.toArray());
    }

    public static String concatWithSpacer(String spacer, String nullString, Object... objects) {
        return concatWithSpacerPrefixSuffixIf(null, null, spacer, null, null, nullString, objects);
    }

    @SuppressWarnings("unchecked")
    public static <T> String concatWithSpacer(Function<? super T, String> converter, String spacer, String nullString, T... objects) {
        return concatWithSpacerPrefixSuffixIf(null, converter, spacer, null, null, nullString, objects);
    }

    @SuppressWarnings("unchecked")
    public static <T> String concatWithSpacerIf(Predicate<? super T> filter, Function<? super T, String> converter, String spacer, String nullString, T... objects) {
        return concatWithSpacerPrefixSuffixIf(filter, converter, spacer, null, null, nullString, objects);
    }

    public static String concatWithSpacerPrefixSuffix(String spacer, String prefix, String suffix, String nullString, Object... objects) {
        return concatWithSpacerPrefixSuffixIf(null, null, spacer, prefix, suffix, nullString, objects);
    }

    @SuppressWarnings("unchecked")
    public static <T> String concatWithSpacerPrefixSuffixIf(Predicate<? super T> filter, Function<? super T, String> converter, String spacer, String prefix, String suffix, String nullString, T... objects) {
        if ((objects != null) && (objects.length > 0)) {
            StringBuilder result = new StringBuilder();
            if (prefix != null) {
                result.append(prefix);
            }
            boolean firstEntry = true;
            boolean areAllObjectsNull = true;
            for (T obj : objects) {
                if ((filter == null) || (filter.test(obj))) {
                    if (!firstEntry) {
                        result.append(spacer);
                    } else {
                        firstEntry = false;
                    }
                    if ((converter != null) && (obj != null)) {
                        String s = converter.apply(obj);
                        result.append(s == null ? nullString : s);
                        areAllObjectsNull = areAllObjectsNull && (s == null);
                    } else if (obj instanceof String) {
                        result.append(obj);
                        areAllObjectsNull = false;
                    } else if (obj instanceof LObservable) {
                        LObservable obs = (LObservable) obj;
                        result.append(obs.isPresent() ? obs.toParseableString() : nullString);
                        areAllObjectsNull = areAllObjectsNull && (obs.isAbsent());
                    } else {
                        result.append(obj == null ? nullString : obj.toString());
                        areAllObjectsNull = areAllObjectsNull && (obj == null);
                    }
                }
            }
            if (suffix != null) {
                result.append(suffix);
            }
            return (areAllObjectsNull && LString.isEmpty(nullString) && LString.isEmpty(suffix) && LString.isEmpty(prefix) ? null : result.toString().trim());
        } else {
            return null;
        }
    }

    public static String format(String format, LList<Object> list) {
        return format(format, key -> {
            var i = 0;
            try {
                i = Integer.valueOf(key);
            } catch (NumberFormatException nfe) {
            }
            return (list != null && i < list.size() ? list.get(i) : ILConstants.BRACKET_SQUARE_OPEN + key + ILConstants.BRACKET_SQUARE_CLOSE);
        });
    }

    public static String format(String format, LMap<String, Object> map) {
        return format(format, key -> (map != null ? map.get(key) : null));
    }

    @SuppressWarnings("unchecked")
    public static String format(String format, Object o) {
        if (o instanceof LMap) {
            return LString.format(format, (LMap) o);
        } else if (o instanceof LList) {
            return LString.format(format, (LList) o);
        } else {
            var fields = (o != null ? LReflections.getFieldsOfInstance(o, null) : null);
            return format(format, key -> {
                var field = (fields != null ? fields.get(key) : null);
                if (field != null) {
                    LObservable observable = (field != null && field.isObservable() ? LReflections.observable(o, field) : null);
                    Object value = null;
                    try {
                        value = (observable != null ? observable.toLocalizedString() : field.get(o));
                    } catch (IllegalAccessException iae) {
                    }
                    value = ((value instanceof Optional) ? (((Optional) value).isEmpty() ? null : ((Optional) value).get()) : value);
                    return value;
                } else {
                    return ILConstants.BRACKET_SQUARE_OPEN + key + ILConstants.BRACKET_SQUARE_CLOSE;
                }
            });
        }
    }

    public static String format(String format, Function<String, Object> valueProvider) {        
        int a = format.indexOf(ILConstants.RESOURCE_OPEN);
        int b = format.indexOf(ILConstants.RESOURCE_CLOSE);
        while ((a > -1) && (b > -1) && (b > a)) {
            var key = format.substring(a + 2, b);
            var value = valueProvider.apply(key);
            value = (value == null ? ILConstants.BRACKET_SQUARE_OPEN + key + ILConstants.BRACKET_SQUARE_CLOSE : value);
            format = format.substring(0, a) + value + format.substring(b + 1);
            a = format.indexOf(ILConstants.RESOURCE_OPEN);
            b = format.indexOf(ILConstants.RESOURCE_CLOSE);
        }
        return format;
    }

    public static int compareVersion(String version1, String version2) {
        if ((version1 == null) && (version2 == null)) {
            return 0;
        } else if (version1 == null) {
            return -1;
        } else if (version2 == null) {
            return 1;
        }

        String[] arr1 = version1.split("\\.");
        String[] arr2 = version2.split("\\.");

        if (arr1.length < arr2.length) {
            return -1;
        }
        if (arr1.length > arr2.length) {
            return 1;
        }

        // same number of version "." dots
        for (int i = 0; i < arr1.length; i++) {
            if (Integer.parseInt(arr1[i]) < Integer.parseInt(arr2[i])) {
                return -1;
            }
            if (Integer.parseInt(arr1[i]) > Integer.parseInt(arr2[i])) {
                return 1;
            }
        }
        // went through all version numbers and they are all the same
        return 0;
    }

    public static LList<Integer> indicesOf(String value, String subString) {
        if (!LString.isEmpty(value)) {
            LList<Integer> intArray = LList.empty();
            int i = value.indexOf(subString);
            int offset = 0;
            while (i > -1) {
                intArray.add(i + offset);
                if (value.length() > i + 1) {
                    value = value.substring(i + 1);
                    offset = offset + i + 1;
                    i = value.indexOf(subString);
                } else {
                    i = -1;
                }
            }
            return (!intArray.isEmpty() ? intArray : null);
        } else {
            return null;
        }
    }
    
    public static String format(String message, Object... arguments) {
        try {
            return String.format(message, arguments);
        } catch (Exception ex) {
            return message + " " + ILConstants.BRACKET_SQUARE_OPEN + ex.getMessage() + ILConstants.BRACKET_SQUARE_CLOSE;
        }
    }

    @Override
    public int length() {
        String s = get();
        return (s != null ? s.length() : null);
    }

    @Override
    public char charAt(int index) {
        String s = get();
        return (s != null ? s.charAt(index) : null);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        String s = get();
        return (s != null ? s.subSequence(start, end) : null);
    }

}
