package com.heroku.idea;

import java.util.*;

/**
 * @author mh
 * @since 17.12.11
 */
public class Json {
    public static Object parse(String input) {
        input = input.trim();
        if (input.startsWith("[")) {
            Collection<Object> result = new ArrayList<Object>();
            for (String value : input.substring(1, input.length() - 1).split(",")) {
                result.add(parse(value));
            }
            return result;
        }
        if (input.startsWith("{")) {
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            for (String value : input.substring(1, input.length() - 1).split(",")) {
                final String[] pair = value.split(":");
                result.put((String) parse(pair[0]), parse(pair[1]));
            }
            return result;
        }
        if (input.startsWith("\"")) {
            return input.substring(1, input.length() - 1);
        }
        if (input.equals("null")) return null;
        if (input.equals("true")) return true;
        if (input.equals("false")) return false;
        if (input.contains(".")) return Double.parseDouble(input);
        final long result = Long.parseLong(input);
        if (result <= Integer.MAX_VALUE && result >= Integer.MIN_VALUE) return (int)result;
        return result;
    }

    public static Map<String, Object> map(Object... keyValues) {
        if (keyValues.length % 2 != 0)
            throw new IllegalArgumentException("Must provide even number of arguments to map");
        Map<String, Object> map=new LinkedHashMap<String, Object>(keyValues.length/2);
        for (int i = 0; i < keyValues.length; i += 2) {
            String key = (String) keyValues[i];
            Object value = keyValues[i + 1];
            map.put(key,value);
        }
        return map;
    }
    public static String format(Object data) {
        if (data instanceof Map) {
            final Map<String, Object> map = (Map<String, Object>) data;
            StringBuilder sb = new StringBuilder(map.size() * 20);
            sb.append("{");
            for (Map.Entry entry : map.entrySet()) {
                sb.append(format(entry.getKey())).append(":").append(format(entry.getValue())).append(",");
            }
            return endBuffer(sb, "}");
        }
        if (data instanceof Iterable) {
            Iterable iterable = (Iterable) data;
            StringBuilder sb = new StringBuilder(10 * 20);
            sb.append("[");
            for (Object value : iterable) {
                sb.append(format(value)).append(",");
            }
            return endBuffer(sb, "]");
        }
        return data instanceof String ? "\"" + data + "\"" : "" + data;
    }

    private static String endBuffer(StringBuilder sb, String delim) {
        if (sb.length()>1) {
            sb.replace(sb.length() - 1, sb.length(), delim);
        } else {
            sb.append(delim);
        }
        return sb.toString();
    }
}
