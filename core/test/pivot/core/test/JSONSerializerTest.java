/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.core.test;

import java.io.InputStream;
// import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import pivot.collections.HashMap;
import pivot.collections.List;
import pivot.collections.Map;
import pivot.serialization.JSONSerializer;

public class JSONSerializerTest {
    public static String[] testStrings = {
        "'hey\there'",
        "'hey\\there'",
        "'hey\\\\there'",
        "  null",
        "\"Hello\\\" World\"",
        "'Hello\\\' \"World'",
        "\"ABCD",
        " 10",
        "+10",
        " -10",
        "10.1",
        "+10.1",
        "-1s0.1",
        "true",
        "false",
        " [  0, 1, 2, [3, 4]",
        " [ \"A\", \"B\", \t\"C\", [\t0, 1, 2, 'abc', true]]",
        "['A', 'B', 'C']",
        "{   }",
        "{null: 'foo'}",
        "{: 'foo'}",
        "{\"\": \"foo\"}",
        "{ my0: 'ABCD\"ABCD' , 'my' : '\"My \\ example 3\"', null: null}",
        "{a:null}",
        "{a:''}",
        "{a:1, b:2",
        "{\"1a\" : 0, bc : 'hello', n:-100.56, c:true, d:{e:10, f:20}, g:{aa:10, bb:20, cc:30}, m:[1,2, 4]}",
        "{\"a#b\" : '#ff'}"
    };

    public static void main(String[] args) {
        // test0();
        // test1();
        // test2();
        // test3();
        // test4();
        test5();
    }

    public static void test0() {
        HashMap<String, Object> a = new HashMap<String, Object>();
        a.put("b", 100);

        HashMap<String, Object> c = new HashMap<String, Object>();
        c.put("d", "Hello World");
        a.put("c", c);

        StringWriter writer = new StringWriter();
        JSONSerializer jsonSerializer = new JSONSerializer();
        try {
            jsonSerializer.writeObject(a, writer);
        } catch(Exception exception) {
            System.out.println(exception);
        }

        System.out.println("Output: " + writer);
    }

    public static void test1() {
        JSONSerializer jsonSerializer = new JSONSerializer();

        for (int i = 0, n = testStrings.length; i < n; i++) {
            try {
                System.out.println("Input: " + testStrings[i]);
                Object object = jsonSerializer.readObject(new StringReader(testStrings[i]));
                System.out.println("Object: " + object);
                StringWriter writer = new StringWriter();
                jsonSerializer.writeObject(object, writer);

                System.out.println("Output: " + writer);
            } catch(Exception exception) {
                System.out.println(exception);
            }
        }

        int i = Integer.MAX_VALUE;
        long l1 = (long)i + 1;
        long l2 = Long.MAX_VALUE;
        float f = Float.MAX_VALUE;
        double d1 = (double)f + 1;
        double d2 = Double.MAX_VALUE;
        String listString = "[" + i + ", " + l1 + ", " + l2 + ", "
            + f + ", " + d1 + ", " + d2 + "]";
        List<?> list = JSONSerializer.parseList(listString);
        for (Object item : list) {
            System.out.println(item);
        }

        Map<String, ?> map = JSONSerializer.parseMap("{a:100, b:200, c:300}");
        for (String key : map) {
            System.out.println(key + ":" + map.get(key));
        }
    }

    public static void test2() {
        testMap("{a: {b: [{cd:'hello'}, {c:'world'}]}}", "a.b[0].cd");
        testMap("{a: {b: [{c:'hello'}, {c:'world'}]}}", "['a'].b[0].c");
        testMap("{a: {b: [{c:'hello'}, {c:'world'}]}}", "a[\"b\"][0]['c']");
        testMap("{a: {b: [{c:'hello'}, {c:'world'}]}}", "a.");
        testMap("{a: {b: [{c:'hello', d:[0, 1, 2, 3, 4]}, {c:'world'}]}}", "a.b[0].d[2]");
        testMap("{a: {b: [{c:'hello', d:[0, 1, 2, 3, 4]}, {c:'world'}]}}", "a.....");
        testMap("{abc: {def: [{ghi:'hello', d:[0, 1, 2, 3, 4]}, {c:'world'}]}}", "abc.def[0].ghi");

        testList("[[0, 1, 2], [3, 4, 5]]", "[1]");
        testList("[[0, 1, 2], [3, 4, 5]]", "[1][0]");
        testList("[[0, 1, 2], [3, 4, 5]]", "[1][0].c");
        testList("[[0, 1, 2], [3, 4, 5]]", "[1][]");
        testList("[[0, 1, 2], [3, 4, 5]]", "[1][0][0]");
    }

    public static void test3() {
        JSONSerializer serializer = new JSONSerializer("ISO-8859-1");
        InputStream inputStream = JSONSerializerTest.class.getResourceAsStream("json_serializer_test.json");

        Object root = null;
        try {
            root = serializer.readObject(inputStream);
        } catch(Exception exception) {
            System.out.println(exception);
        }

        if (root != null) {
            System.out.println(JSONSerializer.getString(root, "foo"));
            System.out.println(JSONSerializer.getString(root, "bar"));
        }

        try {
            serializer.writeObject(root, System.out);
        } catch(Exception exception) {
            System.out.println(exception);
        }
    }

    private static void testList(String list, String path) {
        JSONSerializer jsonSerializer = new JSONSerializer();

        try {
            jsonSerializer.writeObject(JSONSerializer.get(JSONSerializer.parseList(list), path),
                System.out);
            System.out.println();
        } catch(Exception exception) {
            System.out.println(exception);
        }
    }

    private static void testMap(String map, String path) {
        JSONSerializer jsonSerializer = new JSONSerializer();

        try {
            jsonSerializer.writeObject(JSONSerializer.get(JSONSerializer.parseMap(map), path),
                System.out);
            System.out.println();
        } catch(Exception exception) {
            System.out.println(exception);
        }
    }

    public static void test4() {
        Object root = JSONSerializer.parse("{a:{b:{c:'hello', d:'world'}, e:[1, 2, 3], f:false}, h:null}");
        testGet(root, "a");
        testGet(root, "a.b");
        testGet(root, "a.b.c");
        testGet(root, "a.b.d");
        testGet(root, "a['e']");
        testGet(root, "a['e'][1]");
        testGet(root, "a['f']");
        testGet(root, "a['h']");

        JSONSerializer.put(root, "a['h']", 100);
        testGet(root, "a['h']");

        JSONSerializer.remove(root, "a['h']");

        System.out.println("a['h'] exists: " + JSONSerializer.containsKey(root, "a['h']"));
    }

    public static void test5() {
        JSONSerializer jsonSerializer = new JSONSerializer();

        try {
            jsonSerializer.writeObject(JSONSerializer.parse("// This is a comment\n\n['a', /*FOO*/ //dfsdf\n 'b' // FSKJHJKDSF\r /*ASDKHASD*/]"), System.out);
        } catch(Exception exception) {
            System.err.println(exception);
        }
    }

    private static void testGet(Object root, String path) {
        Object value = JSONSerializer.get(root, path);
        System.out.println(path + ": " + value);
    }
}
