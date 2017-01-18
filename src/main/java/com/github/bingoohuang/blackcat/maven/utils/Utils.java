package com.github.bingoohuang.blackcat.maven.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/17.
 */
public class Utils {

    /**
     * Tokenize the given String into a String array via a StringTokenizer.
     * <p>The given delimiters string is supposed to consist of any number of
     * delimiter characters. Each of those characters can be used to separate
     * tokens. A delimiter is always a single character; for multi-character
     * delimiters, consider using {@code delimitedListToStringArray}
     *
     * @param str               the String to tokenize
     * @param delimiters        the delimiter characters, assembled as String
     *                          (each of those characters is individually considered as delimiter)
     * @param trimTokens        trim the tokens via String's {@code trim}
     * @param ignoreEmptyTokens omit empty tokens from the result array
     *                          (only applies to tokens that are empty after trimming; StringTokenizer
     *                          will not consider subsequent delimiters as token in the first place).
     * @return an array of the tokens ({@code null} if the input String
     * was {@code null})
     * @see StringTokenizer
     * @see String#trim()
     */
    public static String[] tokenizeToStringArray(
            String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {
        if (str == null) return null;

        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (trimTokens) token = token.trim();
            if (!ignoreEmptyTokens || token.length() > 0) {
                tokens.add(token);
            }
        }
        return tokens.toArray(new String[tokens.size()]);
    }

    public static boolean hasLength(CharSequence str) {
        return (str != null && str.length() > 0);
    }

    public static boolean hasText(CharSequence str) {
        if (!hasLength(str)) {
            return false;
        }
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public static boolean isEmpty(Object[] array) {
        return (array == null || array.length == 0);
    }

    private static final int INITIAL_HASH = 7;
    private static final int MULTIPLIER = 31;

    private static final String EMPTY_STRING = "";
    private static final String NULL_STRING = "null";
    private static final String ARRAY_START = "{";
    private static final String ARRAY_END = "}";
    private static final String EMPTY_ARRAY = ARRAY_START + ARRAY_END;
    private static final String ARRAY_ELEMENT_SEPARATOR = ", ";

    /**
     * Return a String representation of the specified Object.
     * <p>Builds a String representation of the contents in case of an array.
     * Returns {@code "null"} if {@code obj} is {@code null}.
     *
     * @param obj the object to build a String representation for
     * @return a String representation of {@code obj}
     */
    public static String nullSafeToString(final Object obj) {
        if (obj == null) return NULL_STRING;
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof Object[]) {
            final Object[] arr = (Object[]) obj;
            return nullSafeToString(arr.length, new ItemGetter() {
                public String item(int index) {
                    return String.valueOf(arr[index]);
                }
            });
        }
        if (obj instanceof boolean[]) {
            final boolean[] arr = (boolean[]) obj;
            return nullSafeToString(arr.length, new ItemGetter() {
                public String item(int index) {
                    return String.valueOf(arr[index]);
                }
            });
        }
        if (obj instanceof byte[]) {
            final byte[] arr = (byte[]) obj;
            return nullSafeToString(arr.length, new ItemGetter() {
                public String item(int index) {
                    return String.valueOf(arr[index]);
                }
            });
        }
        if (obj instanceof char[]) {
            final char[] arr = (char[]) obj;
            return nullSafeToString(arr.length, new ItemGetter() {
                public String item(int index) {
                    return "'" + arr[index] + "'";
                }
            });
        }
        if (obj instanceof double[]) {
            final double[] arr = (double[]) obj;
            return nullSafeToString(arr.length, new ItemGetter() {
                public String item(int index) {
                    return String.valueOf(arr[index]);
                }
            });
        }
        if (obj instanceof float[]) {
            final float[] arr = (float[]) obj;
            return nullSafeToString(arr.length, new ItemGetter() {
                public String item(int index) {
                    return String.valueOf(arr[index]);
                }
            });
        }
        if (obj instanceof int[]) {
            final int[] arr = (int[]) obj;
            return nullSafeToString(arr.length, new ItemGetter() {
                public String item(int index) {
                    return String.valueOf(arr[index]);
                }
            });
        }
        if (obj instanceof long[]) {
            final long[] arr = (long[]) obj;
            return nullSafeToString(arr.length, new ItemGetter() {
                public String item(int index) {
                    return String.valueOf(arr[index]);
                }
            });
        }
        if (obj instanceof short[]) {
            final short[] arr = (short[]) obj;
            return nullSafeToString(arr.length, new ItemGetter() {
                public String item(int index) {
                    return String.valueOf(arr[index]);
                }
            });
        }
        String str = obj.toString();
        return (str != null ? str : EMPTY_STRING);
    }

    public static String nullSafeToString(int length, ItemGetter itemGetter) {
        if (length == 0) return EMPTY_ARRAY;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i == 0) sb.append(ARRAY_START);
            else sb.append(ARRAY_ELEMENT_SEPARATOR);
            sb.append(itemGetter.item(i));
        }
        sb.append(ARRAY_END);
        return sb.toString();
    }

    public interface ItemGetter {
        String item(int i);
    }
}
