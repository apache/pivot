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
package org.apache.pivot.functional.monad.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.NoSuchElementException;
import java.util.Random;

import org.apache.pivot.functional.monad.None;
import org.apache.pivot.functional.monad.Option;
import org.apache.pivot.functional.monad.OptionCompanion;
import org.apache.pivot.functional.monad.Some;
import org.junit.Test;

public class OptionTest {
    @Test
    public void companionTest() {
        OptionCompanion<Object> o = OptionCompanion.getInstance();
        assertNotNull(o);
    }

    @Test(expected=NoSuchElementException.class)
    public void companionNoneTest() {
        OptionCompanion<Object> o = OptionCompanion.getInstance();
        assertNotNull(o);

        Option<Object> on = o.fromValue(null);
        assertNotNull(on);
        assertTrue(on instanceof None);
        assertTrue(on.hasValue() == false);
        System.out.println("companionNoneTest(), has value is " + on.hasValue());
        Object onValue = on.getValue();  // throw Exception here
        assertTrue(onValue instanceof None);  // never called
    }

    @Test
    public void companionObjectTest() {
        OptionCompanion<Object> o = OptionCompanion.getInstance();
        assertNotNull(o);

        Option<Object> oo = o.fromValue(new String("Hello"));
        assertNotNull(oo);
        assertTrue(oo instanceof Some);
        assertTrue(oo.hasValue() == true);
        Object ooValue = oo.getValue();
        assertTrue(ooValue instanceof String);
        System.out.println("companionObjectTest(), value stored is " + ooValue);
    }

    @Test
    public void companionStringTest() {
        OptionCompanion<String> o = OptionCompanion.getInstance();
        assertNotNull(o);

        Option<String> os = o.fromValue("Hello");
        assertNotNull(os);
        assertTrue(os instanceof Some);
        assertTrue(os.hasValue() == true);
        Object osValue = os.getValue();
        assertTrue(osValue instanceof String);
        System.out.println("companionStringTest(), value stored is " + osValue);
    }

    @Test
    public void companionNumberTest() {
        OptionCompanion<Number> o = OptionCompanion.getInstance();
        assertNotNull(o);

        Option<Number> on = o.fromValue(new Double(3.14149));
        assertNotNull(on);
        assertTrue(on instanceof Some);
        assertTrue(on.hasValue() == true);
        Object onValue = on.getValue();
        assertTrue(onValue instanceof Number);
        assertTrue(onValue instanceof Double);
        System.out.println("companionNumberTest(), value stored is " + onValue);
    }

    @Test
    public void companionRealUsageRandomTest() {
        OptionCompanion<String> o = OptionCompanion.getInstance();
        assertNotNull(o);

        Option<String> os = null;
        String value;

        // randomizing this test
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(100);

        // store the value in the Option instance (Some if not null, otherwise None)
        // note that try/catch block here are unnecessary, but probably near to a real-world usage
        try {
            // randomizing this test:
            // for even numbers a value will be generated,
            // but for odd numbers the value will be null so a call on it will throw a RuntimeException
            if (randomInt % 2 == 0) {
                value = String.valueOf(randomInt);
            } else {
                value = null;
            }

            os = o.fromValue(value);
        } catch (RuntimeException e) {
            System.err.println("companionRealUsageRandomTest(), got RuntimeException " + e);
            os = o.fromValue(null);
        }

        // verify the value stored
        System.out.println("companionRealUsageRandomTest(), stored element has a value " + os.hasValue());
        try {
            String tsValue;  // = os.getValue();  // this will throw a RuntimeException if os is a None
            // System.out.println("companionRealUsageRandomTest(), value stored is " + tsValue);

            if (randomInt % 2 == 0) {
                assertTrue(os instanceof Some);
                assertTrue(os.hasValue() == true);
                tsValue = os.getValue();
                System.out.println("companionRealUsageRandomTest(), value stored is " + tsValue);
                assertTrue(tsValue != null);
            } else {
                assertTrue(os instanceof None);
                assertTrue(os.hasValue() == false);
                tsValue = os.getValue();  // will throw a RuntimeException when called in the case
                assertTrue(tsValue == null);  // never called
            }

        } catch (RuntimeException e) {
            System.err.println("companionRealUsageRandomTest(), got RuntimeException " + e);
            assertTrue(os.hasValue() == false);
        }
    }

    @Test
    public void optionSomeTest() {
        OptionCompanion<String> o = OptionCompanion.getInstance();
        assertNotNull(o);

        // sample by direct instancing of Some/None classes, but discouraged

        Option<String> os = null;
        String tsValue = null;

        // store the value in the Option instance (Some if not null, otherwise None)
        os = new Some<>("Optional value");
        assertTrue(os != null);

        // verify the value stored
        System.out.println("optionSomeTest(), stored element has a value " + os.hasValue());
        assertTrue(os instanceof Some);
        assertTrue(os.hasValue() == true);
        tsValue = os.getValue();
        System.out.println("optionSomeTest(), value stored is " + tsValue);
        assertTrue(tsValue != null);
    }

    @Test
    public void optionNoneTest() {
        OptionCompanion<String> o = OptionCompanion.getInstance();
        assertNotNull(o);

        // sample by direct instancing of Some/None classes, but discouraged

        Option<String> os = null;
        String tsValue = null;

        // store the value in the Option instance (Some if not null, otherwise None)
        // os = new None<>();  // discouraged
        os = None.getInstance();  // better
        assertTrue(os != null);

        // verify the value stored
        System.out.println("optionNoneTest(), stored element has a value " + os.hasValue());
        assertTrue(os instanceof None);
        assertTrue(os.hasValue() == false);
        try {
            tsValue = os.getValue();  // will throw a RuntimeException when called in the case
            assertTrue(tsValue == null);  // never called
        } catch (RuntimeException e) {
            System.err.println("optionNoneTest(), got RuntimeException " + e);
            assertTrue(os.hasValue() == false);
        }
    }

}
