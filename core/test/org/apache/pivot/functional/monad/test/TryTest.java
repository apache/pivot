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
        // assertTrue(tsValue instanceof String);
        assertTrue(tsValue.equals("Hello"));
        System.out.println("companionStringTest(), value stored is " + tsValue);
    }

    @Test
    public void companionFailureTest() {
        TryCompanion<String> t = TryCompanion.getInstance();
        assertNotNull(t);

        RuntimeException re = new IllegalArgumentException("Illegal argument");
        Try<String> tf = t.fromValue(re);
        assertNotNull(tf);
        assertTrue(tf instanceof Failure);
        assertTrue(tf.isSuccess() == false);
// TODO: fix later ...
//        Exception tfValue = tf.getValue();
//        assertTrue(tfValue != null);
//        // assertTrue(tfValue instanceof Throwable);
//        // assertTrue(tfValue instanceof Exception);
//        System.out.println("companionStringTest(), value stored is " + tfValue);
    }

}
