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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Random;

import org.apache.pivot.functional.monad.Failure;
import org.apache.pivot.functional.monad.None;
import org.apache.pivot.functional.monad.Option;
import org.apache.pivot.functional.monad.Some;
import org.apache.pivot.functional.monad.Success;
import org.apache.pivot.functional.monad.Try;
import org.apache.pivot.functional.monad.TryCompanion;
import org.junit.Test;

public class TryTest {
    @Test
    public void companionTest() {
        TryCompanion<Object> t = TryCompanion.getInstance();
        assertNotNull(t);
    }

    @Test
    public void companionNullTest() {
        TryCompanion<Object> t = TryCompanion.getInstance();
        assertNotNull(t);

        Try<Object> tn = t.fromValue(null);
        assertNotNull(tn);
        assertTrue(tn instanceof Success);
        assertTrue(tn.isSuccess() == true);
        Object tnValue = tn.getValue();
        assertTrue(tnValue == null);
        System.out.println("companionNullTest(), value stored is " + tnValue);
    }

    @Test
    public void companionObjectTest() {
        TryCompanion<Object> t = TryCompanion.getInstance();
        assertNotNull(t);

        Try<Object> to = t.fromValue(new String("Hello"));
        assertNotNull(to);
        assertTrue(to instanceof Success);
        assertTrue(to.isSuccess() == true);
        Object toValue = to.getValue();
        assertTrue(toValue != null);
        assertTrue(toValue instanceof String);
        assertTrue(toValue.equals("Hello"));
        System.out.println("companionObjectTest(), value stored is " + toValue);
    }

    @Test
    public void companionStringTest() {
        TryCompanion<String> t = TryCompanion.getInstance();
        assertNotNull(t);

        Try<String> ts = t.fromValue("Hello");
        assertNotNull(ts);
        assertTrue(ts instanceof Success);
        assertTrue(ts.isSuccess() == true);
        String tsValue = ts.getValue();
        assertTrue(tsValue != null);
        // assertTrue(tsValue instanceof String);  // unnecessary
        assertTrue(tsValue.equals("Hello"));
        System.out.println("companionStringTest(), value stored is " + tsValue);
    }

    @Test
    public void companionFailureTest() {
        TryCompanion<String> t = TryCompanion.getInstance();
        assertNotNull(t);

        RuntimeException re = new IllegalArgumentException("Sample RuntimeException");
        Try<String> tf = t.fromValue(re);
        assertNotNull(tf);
        assertTrue(tf instanceof Failure);
        assertTrue(tf.isSuccess() == false);
        try {
            String tfValue = tf.getValue();  // this will throw the RuntimeException stored inside the Failure
            assertTrue(tfValue != null);  // never called
        } catch (RuntimeException e) {
            System.err.println("companionFailureTest(), value stored is " + e);
            assertTrue(e != null);  // unnecessary
        }
    }

    @Test
    public void companionRealUsageRandomTest() {
        TryCompanion<String> t = TryCompanion.getInstance();
        assertNotNull(t);

        Try<String> ts = null;
        String value;

        // randomizing this test
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(100);

        // store the value (or the RuntimeException) in the Try instance
        try {
            // randomizing this test:
            // for even numbers a value will be generated,
            // but for odd numbers the value will be null so a call on it will throw a RuntimeException
            if (randomInt % 2 == 0) {
                value = String.valueOf(randomInt);
            } else {
                value = null;
                throw new NullPointerException("Sample RuntimeException");  // simulate an exception here
            }

            ts = t.fromValue(value);
        } catch (RuntimeException e) {
            ts = t.fromValue(e);
        }

        // verify the value stored
        System.out.println("companionRealUsageRandomTest(), value stored is a success " + ts.isSuccess());
        try {
            String tsValue = ts.getValue();  // this will throw the RuntimeException stored inside the Failure
            assertTrue(tsValue != null);  // called only when a real value is stored (and not a RuntimeException)
            System.out.println("companionRealUsageRandomTest(), value stored is " + tsValue);
            assertTrue(ts.isSuccess() == true);
        } catch (RuntimeException e) {
            System.err.println("companionRealUsageRandomTest(), exception stored is " + e);
            assertTrue(ts.isSuccess() == false);
        }
    }

    @Test
    public void trySuccessTest() {
        // sample by direct instancing of Success/Failure classes, but discouraged
        Try<String> ts = null;

        // store the value (or the RuntimeException) in the Try instance
        try {
            ts = new Success<>("Hello with Success");
        } catch (RuntimeException e) {
            // ts = new Success<>(e);  // compile error, ok
            ts = new Failure<>(e);
            assertTrue(e != null);  // unnecessary
        }

        // verify the value stored
        System.out.println("trySuccessTest(), value stored is a success " + ts.isSuccess());
        String tsValue;
        try {
            tsValue = ts.getValue();  // this will throw the RuntimeException stored inside the Failure
            assertTrue(tsValue != null);  // called only when a real value is stored (and not a RuntimeException)
            System.out.println("trySuccessTest(), value stored is " + tsValue);
            assertTrue(ts.isSuccess() == true);
        } catch (RuntimeException e) {
            System.err.println("trySuccessTest(), exception stored is " + e);
            assertTrue(ts.isSuccess() == false);
        }
        // test with alternative value
        tsValue = ts.getValueOrElse("Alternative value");
        assertEquals("Hello with Success", tsValue);
        tsValue = ts.getValueOrNull();
        assertEquals("Hello with Success", tsValue);
    }

    @Test
    public void tryFailureTest() {
        // sample by direct instancing of Success/Failure classes, but discouraged
        Try<String> tf = null;

        // store the value (or the RuntimeException) in the Try instance
        try {
            throw new NullPointerException("Sample RuntimeException");  // simulate an exception here
        } catch (RuntimeException e) {
            // ts = new Success<>(e);  // compile error, ok
            tf = new Failure<>(e);
            assertTrue(e != null);  // unnecessary
        }

        // verify the value stored
        System.out.println("tryFailureTest(), value stored is a success " + tf.isSuccess());
        String tsValue;
        try {
            tsValue = tf.getValue();  // this will throw the RuntimeException stored inside the Failure
            assertTrue(tsValue != null);  // called only when a real value is stored (and not a RuntimeException)
            System.out.println("tryFailureTest(), value stored is " + tsValue);
            assertTrue(tf.isSuccess() == true);
        } catch (RuntimeException e) {
            System.err.println("tryFailureTest(), exception stored is " + e);
            assertTrue(tf.isSuccess() == false);
        }
        // test with alternative value
        tsValue = tf.getValueOrElse("Alternative value");
        assertEquals("Alternative value", tsValue);
        tsValue = tf.getValueOrNull();
        assertEquals(null, tsValue);
    }

    @Test
    public void trySuccessIteratorTest() {
        // sample by direct instancing of Success/Failure classes, but discouraged
        Try<String> ts = new Success<>("Hello with Success");
        System.out.println("trySuccessIteratorTest(), instance variable is " + ts);

        // iterate and verify on the value stored
        Iterator<String> it = ts.iterator();
        assertNotNull(it);
        int i = 0;
        while (it.hasNext()) {
            String value = it.next();
            System.out.println("trySuccessIteratorTest(), value " + i + " from iterator is " + value);
            assertNotNull(value);
            assertEquals("Hello with Success", value);
            i++;
        }
        assertEquals(i, 1);

        // another test
        i = 0;
        System.out.println("trySuccessIteratorTest(), another test");
        for (String value : ts) {
            System.out.println("trySuccessIteratorTest(), value " + i + " from iterator is " + value);
            assertNotNull(value);
            assertEquals("Hello with Success", value);
            i++;
        }
        assertEquals(i, 1);
    }

    @Test
    public void tryFailureIteratorTest() {
        // sample by direct instancing of Success/Failure classes, but discouraged
        Try<String> tf = null;

        // store the value (or the RuntimeException) in the Try instance
        try {
            throw new NullPointerException("Sample RuntimeException");  // simulate an exception here
        } catch (RuntimeException e) {
            tf = new Failure<>(e);
        }
        System.out.println("tryFailureIteratorTest(), instance variable is " + tf);

        // iterate and verify on the value stored
        Iterator<String> it = tf.iterator();
        assertNotNull(it);
        int i = 0;
        while (it.hasNext()) {
            // never executed in this case
            String value = it.next();
            System.out.println("tryFailureIteratorTest(), value " + i + " from iterator is " + value);
            assertEquals(null, value);
            i++;
        }
        assertEquals(i, 0);

        // another test
        i = 0;
        System.out.println("tryFailureIteratorTest(), another test");
        for (String value : tf) {
            // never executed in this case
            System.out.println("tryFailureIteratorTest(), value " + i + " from iterator is " + value);
            assertEquals(null, value);
            i++;
        }
        assertEquals(i, 0);
    }

    @Test
    public void companionSuccessToOptionTest() {
        TryCompanion<String> t = TryCompanion.getInstance();
        assertNotNull(t);

        Try<String> ts = t.fromValue("Hello");
        Option<String> os = t.toOption(ts);

        // verify the value stored
        System.out.println("companionSuccessToOptionTest(), Try instance is " + ts);
        System.out.println("companionSuccessToOptionTest(), Option instance is " + os);

        assertNotNull(ts);
        assertTrue(ts instanceof Success);
        assertTrue(ts.isSuccess() == true);

        assertNotNull(os);
        assertTrue(os instanceof Some);
        assertTrue(os.hasValue() == true);

        String osValue = os.getValue();
        assertTrue(osValue != null);
        assertTrue(osValue.equals("Hello"));
        System.out.println("companionSuccessToOptionTest(), value stored is " + osValue);
    }

    @Test
    public void companionFailureToOptionTest() {
        TryCompanion<String> t = TryCompanion.getInstance();
        assertNotNull(t);

        RuntimeException re = new IllegalArgumentException("Sample RuntimeException");
        Try<String> tf = t.fromValue(re);
        Option<String> on = t.toOption(tf);

        // verify the value stored
        System.out.println("companionFailureToOptionTest(), Try instance is " + tf);
        System.out.println("companionFailureToOptionTest(), Option instance is " + on);

        assertNotNull(tf);
        assertTrue(tf instanceof Failure);
        assertTrue(tf.isSuccess() == false);

        assertNotNull(on);
        assertTrue(on instanceof None);
        assertTrue(on.hasValue() == false);

        try {
            String onValue = on.getValue();  // this will throw a RuntimeException for the non-value of None
            assertTrue(onValue != null);  // never called
        } catch (RuntimeException e) {
            System.err.println("companionFailureToOptionTest(), got RuntimeException " + e);
        }

    }

}
