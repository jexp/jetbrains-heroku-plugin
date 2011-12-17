package com.heroku.idea;

import org.junit.Test;

import java.util.Arrays;

import static com.heroku.idea.Json.format;
import static com.heroku.idea.Json.map;
import static org.junit.Assert.assertEquals;

/**
 * @author mh
 * @since 17.12.11
 */
public class JsonTest {
    @Test
    public void testParse() throws Exception {
        assertEquals(null, Json.parse("null"));
        assertEquals(1, Json.parse("1"));
        assertEquals(1.1, Json.parse("1.1"));
        assertEquals(true, Json.parse("true"));
        assertEquals(false, Json.parse("false"));
    }
    @Test
    public void testParseMap() throws Exception {
        assertEquals(map("a", 1, "b", "2"), Json.parse("{\"a\":1,\"b\":\"2\"}"));
    }
    @Test
    public void testParseList() throws Exception {
        assertEquals(Arrays.<Object>asList("a", 1, "b", "2",true,false,null), Json.parse("[\"a\", 1, \"b\", \"2\",true, false, null]"));
    }

    @Test
    public void testFormatMap() throws Exception {
        assertEquals("{\"a\":1,\"b\":\"2\"}", format(map("a", 1, "b", "2")));
    }
    @Test
    public void testFormatCollection() throws Exception {
        assertEquals("[1,2,\"a\"]", format(Arrays.<Object>asList(1, 2, "a")));
    }

    @Test
    public void testFormat() throws Exception {
        assertEquals("\"a\"", format("a"));
        assertEquals("1", format(1));
        assertEquals("null", format((Object) null));
        assertEquals("true", format(true));
        assertEquals("false", format(false));
        assertEquals("1.1", format(1.1));
    }
}
