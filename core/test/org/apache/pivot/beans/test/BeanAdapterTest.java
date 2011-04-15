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
import java.util.Random;

import org.apache.pivot.json.JSONSerializer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BeanAdapterTest {

    private static Random rnd;

    BeanAdapterTestObject src_test;
    JSONSerializer jsonSerializer;
    StringWriter writer;
    StringReader reader;
    BeanAdapterTestObject target_test;

    @BeforeClass
    public static void setUpClass() throws Exception {
        rnd = new Random();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        rnd = null;
    }

    @Before
    public void setUp() {
        src_test = new BeanAdapterTestObject();
        jsonSerializer = new JSONSerializer(BeanAdapterTestObject.class);
        writer = new StringWriter();
    }

    @After
    public void tearDown() {
        src_test = null;
        jsonSerializer = null;
        writer = null;
        reader = null;
        target_test = null;
    }

    @Test
    public void testSerializeBigDecimal()
    {
        System.out.println("testSerializeBigDecimal");

        double random_double = rnd.nextDouble();
        System.out.println("random_double = " + random_double);

        src_test.setBd(new BigDecimal(random_double, new MathContext(4)));

        try {
            jsonSerializer.writeObject(src_test, writer);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
            return;
        }

        System.out.println("json string from src_test = " + writer.toString());
        System.out.println("src_test.getBd() = " + src_test.getBd());

        reader = new StringReader(writer.toString());

        try {
            target_test = (BeanAdapterTestObject) jsonSerializer.readObject(reader);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
            return;
        }

        System.out.println("target_test.getBd() = " + target_test.getBd());
        assertEquals(src_test.getBd(), target_test.getBd());
    }

    @Test
    public void testSerializeBigInteger()
    {
        System.out.println("testSerializeBigInteger");

        int random_int = rnd.nextInt();
        System.out.println("random_int = " + random_int);

        src_test.setBi(new BigInteger(String.valueOf(random_int)));

        try {
            jsonSerializer.writeObject(src_test, writer);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
            return;
        }

        System.out.println("json string from src_test = " + writer.toString());
        System.out.println("src_test.getBi() = " + src_test.getBi());

        reader = new StringReader(writer.toString());

        try {
            target_test = (BeanAdapterTestObject) jsonSerializer.readObject(reader);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
            return;
        }

        System.out.println("target_test.getBi() = " + target_test.getBi());
        assertEquals(src_test.getBi(), target_test.getBi());
    }

    @Test
    public void testSerializeString()
    {
        System.out.println("testSerializeString");

        String value = "A test String";
        System.out.println("value = \"" + value + "\"");

        src_test.setString(value);

        try {
            jsonSerializer.writeObject(src_test, writer);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
            return;
        }

        System.out.println("json string from src_test = " + writer.toString());
        System.out.println("src_test.getString() = \"" + src_test.getString() + "\"");

        reader = new StringReader(writer.toString());

        try {
            target_test = (BeanAdapterTestObject) jsonSerializer.readObject(reader);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
            return;
        }

        System.out.println("target_test.getString() = " + target_test.getString());
        assertEquals(src_test.getString(), target_test.getString());
    }

}
