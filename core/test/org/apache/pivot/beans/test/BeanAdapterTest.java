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
package org.apache.pivot.beans.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Date;
import java.util.Random;

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Map;
import org.apache.pivot.json.JSONSerializer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BeanAdapterTest {

    private static Random rnd;

    BeanAdapterSampleObject srcTest;
    JSONSerializer jsonSerializer;
    StringWriter writer;
    StringReader reader;
    BeanAdapterSampleObject targetTest;

    @BeforeClass
    public static void setUpClass() {
        System.out.println(BeanAdapterTest.class.getName() + ": Starting tests at " + new Date());
        rnd = new Random();
    }

    @AfterClass
    public static void tearDownClass() {
        rnd = null;
        System.out.println(BeanAdapterTest.class.getName() + ": Ending tests at   " + new Date());
    }

    @Before
    public void setUp() {
        srcTest = new BeanAdapterSampleObject();
        jsonSerializer = new JSONSerializer(BeanAdapterSampleObject.class);
        writer = new StringWriter();
    }

    @After
    public void tearDown() {
        srcTest = null;
        jsonSerializer = null;
        writer = null;
        reader = null;
        targetTest = null;
    }

    @Test
    public void testSerializeBigDecimal() {
        System.out.println("testSerializeBigDecimal");

        double randomDouble = rnd.nextDouble();
        System.out.println("randomDouble = " + randomDouble
            + " (value will be truncated to 4 digits in this test)");

        srcTest.setBd(new BigDecimal(randomDouble, new MathContext(4)));

        try {
            jsonSerializer.writeObject(srcTest, writer);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
            return;
        }

        System.out.println("json string from srcTest = " + writer.toString());
        System.out.println("srcTest.getBd()    = " + srcTest.getBd());

        reader = new StringReader(writer.toString());

        try {
            targetTest = (BeanAdapterSampleObject) jsonSerializer.readObject(reader);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
            return;
        }

        System.out.println("targetTest.getBd() = " + targetTest.getBd());
        assertEquals(srcTest.getBd().doubleValue(), targetTest.getBd().doubleValue(), 0.0001);
    }

    @Test
    public void testSerializeBigInteger() {
        System.out.println("testSerializeBigInteger");

        int randomInt = rnd.nextInt();
        System.out.println("randomInt = " + randomInt);

        srcTest.setBi(new BigInteger(String.valueOf(randomInt)));

        try {
            jsonSerializer.writeObject(srcTest, writer);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
            return;
        }

        System.out.println("json string from srcTest = " + writer.toString());
        System.out.println("srcTest.getBi()    = " + srcTest.getBi());

        reader = new StringReader(writer.toString());

        try {
            targetTest = (BeanAdapterSampleObject) jsonSerializer.readObject(reader);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
            return;
        }

        System.out.println("targetTest.getBi() = " + targetTest.getBi());
        assertEquals(srcTest.getBi(), targetTest.getBi());
    }

    @Test
    public void testSerializeString() {
        System.out.println("testSerializeString");

        String value = "A test String";
        System.out.println("String value = \"" + value + "\"");

        srcTest.setString(value);

        try {
            jsonSerializer.writeObject(srcTest, writer);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
            return;
        }

        System.out.println("json string from srcTest = " + writer.toString());
        System.out.println("srcTest.getString()    = \"" + srcTest.getString() + "\"");

        reader = new StringReader(writer.toString());

        try {
            targetTest = (BeanAdapterSampleObject) jsonSerializer.readObject(reader);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
            return;
        }

        System.out.println("targetTest.getString() = \"" + targetTest.getString() + "\"");
        assertEquals(srcTest.getString(), targetTest.getString());
    }

    @Test
    public void testPutAll() {
        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("bd", BigDecimal.TEN);
        sourceMap.put("bi", BigInteger.ONE);
        sourceMap.put("string", "This is a test of the Emergency Broadcast System");

        BeanAdapter obj = new BeanAdapter(srcTest);
        obj.putAll(sourceMap);

        try {
            jsonSerializer.writeObject(srcTest, writer);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
            return;
        }

        System.out.println("json string from srcTest = " + writer.toString());

        reader = new StringReader(writer.toString());

        try {
            targetTest = (BeanAdapterSampleObject) jsonSerializer.readObject(reader);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
            return;
        }

        System.out.println("targetTest.getString() = \"" + targetTest.getString() + "\"");
        System.out.println("targetTest.getBd() = " + targetTest.getBd());
        System.out.println("targetTest.getBi() = " + targetTest.getBi());
        assertEquals(srcTest.getString(), targetTest.getString());
        assertEquals(srcTest.getBd(), targetTest.getBd());
        assertEquals(srcTest.getBi(), targetTest.getBi());
    }

}
