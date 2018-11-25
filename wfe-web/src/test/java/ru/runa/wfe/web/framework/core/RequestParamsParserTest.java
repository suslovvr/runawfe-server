package ru.runa.wfe.web.framework.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

public class RequestParamsParserTest {

    private RequestParamsParser parser = new RequestParamsParser();

    @AllArgsConstructor
    static class B {
        boolean bbb;
    }

    static class BL extends B {
        long lll;
        BL() {
            super(false);
            lll = 0;
        }
        BL(boolean bbb, long lll) {
            super(bbb);
            this.lll = lll;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof BL) && ((BL)obj).bbb == bbb && ((BL)obj).lll == lll;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    static class C {
        Character c;
        int d;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    static class SI {
        String s;
        Integer i;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    static class Compound {
        ArrayList<BL> bb;
        HashMap<String, Character> cc;
        HashMap<String, C> ccc;
        SI si;
        ArrayList<String> ss;
        ArrayList<ArrayList<String>> sss;
    }

    private String[] toStringArray(String... ss) {
        return ss;
    }

    private void testOne(Map<String, String[]> requestParams, Object expected) throws Exception {
        Object actual = parser.parse(Collections.emptyMap(), requestParams, expected.getClass());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testAll() throws Exception {
        testOne(Collections.emptyMap(), new BL());
        testOne(Collections.emptyMap(), new C());
        testOne(Collections.emptyMap(), new SI());
        testOne(Collections.emptyMap(), new Compound());

        testOne(
                new HashMap<String, String[]>() {{
                    put("bbb", toStringArray("1"));
                }},
                new BL(true, 0)
        );
        testOne(
                new HashMap<String, String[]>() {{
                    put("lll", toStringArray("223344"));
                }},
                new BL(false, 223344)
        );
        testOne(
                new HashMap<String, String[]>() {{
                    put("bbb", toStringArray("1"));
                    put("lll", toStringArray("223344"));
                }},
                new BL(true, 223344)
        );

        testOne(
                new HashMap<String, String[]>() {{
                    put("c", toStringArray("1"));
                    put("d", toStringArray("2"));
                }},
                new C('1', 2)
        );
        testOne(
                new HashMap<String, String[]>() {{
                    put("c", toStringArray("."));
                    put("d", toStringArray("4"));
                }},
                new C('.', 4)
        );

        testOne(
                new HashMap<String, String[]>() {{
                    put("s", toStringArray("my name"));
                    put("i", toStringArray("123"));
                }},
                new SI("my name", 123)
        );

        {
            // No {{}} initialization, since JUnit assertEquals() fails to comparing anonymous subclass with its base class.
            val bb = new ArrayList<BL>();
            bb.add(null);
            bb.add(new BL(true, 123));
            bb.add(null);
            bb.add(null);
            bb.add(new BL(false, 456));

            val cc = new HashMap<String, Character>();
            cc.put("x", '.');
            cc.put("yy", 'A');

            val ccc = new HashMap<String, C>();
            ccc.put("xx", new C(':', 12));
            ccc.put("yyy", new C('B', 45));

            val si = new SI("abcdefgh", 789);

            val ss = new ArrayList<String>();
            ss.add("cruel");
            ss.add("world");

            val sss0 = new ArrayList<String>();
            sss0.add("cruel0");
            sss0.add("world0");
            val sss2 = new ArrayList<String>();
            sss2.add("cruel2");
            sss2.add("world2");
            val sss = new ArrayList<ArrayList<String>>();
            sss.add(sss0);
            sss.add(null);
            sss.add(sss2);

            testOne(
                    new LinkedHashMap<String, String[]>() {{
                        put("bb[1].bbb", toStringArray("1"));
                        put("bb[1].lll", toStringArray("123"));
                        put("bb[4].bbb", toStringArray("0"));
                        put("bb[4].lll", toStringArray("456"));
                        put("cc[x]", toStringArray("."));
                        put("cc[yy]", toStringArray("A"));
                        put("ccc[xx].c", toStringArray(":"));
                        put("ccc[xx].d", toStringArray("12"));
                        put("ccc[yyy].c", toStringArray("B"));
                        put("ccc[yyy].d", toStringArray("45"));
                        put("si.s", toStringArray("abcdefgh"));
                        put("si.i", toStringArray("789"));
                        put("ss", toStringArray("cruel", "world"));
                        put("sss[0]", toStringArray("cruel0", "world0"));
                        put("sss[2]", toStringArray("cruel2", "world2"));
                    }},
                    new Compound(bb, cc, ccc, si, ss, sss)
            );
        }
    }
}
