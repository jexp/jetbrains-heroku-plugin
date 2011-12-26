package com.jetbrains.heroku.rest;

import com.intellij.openapi.util.JDOMUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;

/**
 * @author mh
 * @since 17.12.11
 */
public class RestFormatHelper {
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

    public static <T> Map<String, T> map(Object... keyValues) {
        if (keyValues.length % 2 != 0)
            throw new IllegalArgumentException("Must provide even number of arguments to map");
        Map<String, T> map=new LinkedHashMap<String, T>(keyValues.length/2);
        for (int i = 0; i < keyValues.length; i += 2) {
            String key = (String) keyValues[i];
            Object value = keyValues[i + 1];
            map.put(key, (T) value);
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

    public static Object parseJson(String result) {
        try {
            return new JSONParser().parse(result);
        } catch (ParseException e) {
            throw new RuntimeException("error parsing: "+result,e);
        }
    }

    public static Map<String, Object> parseXml(String info) {
        try {
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            final Document doc = JDOMUtil.loadDocument(info);
            for (Element element : JDOMUtil.getChildren(doc.getRootElement())) {
                result.put(element.getName(), element.getText());
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing xml " + info, e);
        }
    }
    public static List<Map<String, Object>> parseNestedXml(String info) {
        try {
            List<Map<String, Object>> result=new ArrayList<Map<String, Object>>();
            final Document doc = JDOMUtil.loadDocument(info);
            for (Element element : JDOMUtil.getChildren(doc.getRootElement())) {
                Map<String, Object> row = new LinkedHashMap<String, Object>();
                result.add(row);
                for (Element child : (Iterable<Element>)element.getChildren()) {
                    row.put(child.getName(),child.getText());
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing xml " + info, e);
        }
    }
}
