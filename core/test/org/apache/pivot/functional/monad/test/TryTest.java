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

import java.util.Random;

import org.apache.pivot.functional.monad.Failure;
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
        TryCompanion<String> t = TryCompanion.getInstance();
        assertNotNull(t);

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
        try {
            String tsValue = ts.getValue();  // this will throw the RuntimeException stored inside the Failure
            assertTrue(tsValue != null);  // called only when a real value is stored (and not a RuntimeException)
            System.out.println("trySuccessTest(), value stored is " + tsValue);
            assertTrue(ts.isSuccess() == true);
        } catch (RuntimeException e) {
            System.err.println("trySuccessTest(), exception stored is " + e);
            assertTrue(ts.isSuccess() == false);
        }
    }

    @Test
    public void tryFailureTest() {
        TryCompanion<String> t = TryCompanion.getInstance();
        assertNotNull(t);

        // sample by direct instancing of Success/Failure classes, but discouraged

        Try<String> ts = null;

        // store the value (or the RuntimeException) in the Try instance
        try {
            throw new NullPointerException("Sample RuntimeException");  // simulate an exception here
        } catch (RuntimeException e) {
            // ts = new Success<>(e);  // compile error, ok
            ts = new Failure<>(e);
            assertTrue(e != null);  // unnecessary
        }

        // verify the value stored
        System.out.println("tryFailureTest(), value stored is a success " + ts.isSuccess());
        try {
            String tsValue = ts.getValue();  // this will throw the RuntimeException stored inside the Failure
            assertTrue(tsValue != null);  // called only when a real value is stored (and not a RuntimeException)
            System.out.println("tryFailureTest(), value stored is " + tsValue);
            assertTrue(ts.isSuccess() == true);
        } catch (RuntimeException e) {
            System.err.println("tryFailureTest(), exception stored is " + e);
            assertTrue(ts.isSuccess() == false);
        }
    }

}
