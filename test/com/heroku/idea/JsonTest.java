package com.heroku.idea;

import com.heroku.idea.rest.RestFormatHelper;
import org.junit.Test;

import java.util.Arrays;

import static com.heroku.idea.rest.RestFormatHelper.format;
import static com.heroku.idea.rest.RestFormatHelper.map;
import static org.junit.Assert.assertEquals;

/**
 * @author mh
 * @since 17.12.11
 */
public class JsonTest {
    @Test
    public void testParse() throws Exception {
        assertEquals(null, RestFormatHelper.parse("null"));
        assertEquals(1, RestFormatHelper.parse("1"));
        assertEquals(1.1, RestFormatHelper.parse("1.1"));
        assertEquals(true, RestFormatHelper.parse("true"));
        assertEquals(false, RestFormatHelper.parse("false"));
    }
    @Test
    public void testParseMap() throws Exception {
        assertEquals(map("a", 1, "b", "2"), RestFormatHelper.parse("{\"a\":1,\"b\":\"2\"}"));
    }
    @Test
    public void testParseList() throws Exception {
        assertEquals(Arrays.<Object>asList("a", 1, "b", "2", true, false, null), RestFormatHelper.parse("[\"a\", 1, \"b\", \"2\",true, false, null]"));
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
