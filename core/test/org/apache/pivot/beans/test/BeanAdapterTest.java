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
import java.math.MathContext;
import java.util.Random;

import org.apache.pivot.json.JSONSerializer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BeanAdapterTest {

    private static Random rnd = null;


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
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSerializeBigDecimal()
    {
        double random_double = rnd.nextDouble();
        System.out.println("random_double = " + random_double);

        BeanAdapterTestObject src_test = new BeanAdapterTestObject();
        src_test.setBd1(new BigDecimal(random_double, new MathContext(4)));

        Object object = src_test;
        JSONSerializer jsonSerializer = new JSONSerializer(BeanAdapterTestObject.class);
        StringWriter writer = new StringWriter();

        try {
            jsonSerializer.writeObject(object, writer);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
            return;
        }

        System.out.println("s = " + writer.toString());
        System.out.println("src_test.getBd1() = " + src_test.getBd1());

        StringReader reader = new StringReader(writer.toString());
        BeanAdapterTestObject target_test;

        try {
            target_test = (BeanAdapterTestObject) jsonSerializer.readObject(reader);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
            return;
        }

        System.out.println("target_test.getBd1() = " + target_test.getBd1());
        assertEquals(src_test.getBd1(), target_test.getBd1());
    }

}
