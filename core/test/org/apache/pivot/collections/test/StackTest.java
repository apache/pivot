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
package org.apache.pivot.collections.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.pivot.collections.ArrayStack;
import org.apache.pivot.collections.LinkedStack;
import org.apache.pivot.collections.Stack;
import org.junit.Test;

public class StackTest {
    private static final int MAX_STACK_DEPTH = 10;

    @Test
    public void stackTest() {
        testStack(new ArrayStack<String>(MAX_STACK_DEPTH, MAX_STACK_DEPTH));
        testStack(new LinkedStack<String>(MAX_STACK_DEPTH));
    }

    private static void testStack(Stack<String> stack) {
        int i = 0;
        while (i < 5) {
            char c = 'A';
            c += i;
            stack.push(Character.toString(c));
            i++;
        }

        i = 4;
        while (!stack.isEmpty()) {
            String s = stack.pop();
            char c = s.charAt(0);
            c -= i;
            assertTrue(c == 'A');
            i--;
        }

        // Ensure we only get max depth items even if we push more
        for (i = 0; i <= MAX_STACK_DEPTH + 5; i++) {
            stack.push("This is a test");
        }
        assertEquals(stack.getDepth(), MAX_STACK_DEPTH);
    }
}
