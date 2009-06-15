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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pivot.collections.ArrayStack;
import pivot.collections.LinkedStack;
import pivot.collections.Stack;

public class StackTest {
    @Test
    public void stackTest() {
        testStack(new ArrayStack<String>(5));
        testStack(new LinkedStack<String>());
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
    }
}
