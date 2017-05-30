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
package org.apache.pivot.util.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.apache.pivot.util.BooleanResult;


public class BooleanResultTest {
    private enum Operation {
        OR,
        AND,
        XOR,
        NOT,
        SET
    }

    private void operateAndTest(BooleanResult result, boolean value, Operation op, boolean expectedResult) {
        switch (op) {
            case OR:
                result.or(value);
                break;
            case AND:
                result.and(value);
                break;
            case XOR:
                result.xor(value);
                break;
            case NOT:
                result.not();
                break;
            case SET:
                result.set(value);
                break;
        }
        assertEquals(result.get(), expectedResult);
    }

    @Test
    public void test() {
        BooleanResult result = new BooleanResult();
        operateAndTest(result, false, Operation.OR, false);
        operateAndTest(result, true, Operation.OR, true);
        operateAndTest(result, true, Operation.XOR, false);
        operateAndTest(result, false /* ignored */, Operation.NOT, true);
        operateAndTest(result, true, Operation.AND, true);
        operateAndTest(result, false, Operation.SET, false);

        BooleanResult result2 = new BooleanResult(true);
        assertEquals(result2.get(), true);
    }

}
