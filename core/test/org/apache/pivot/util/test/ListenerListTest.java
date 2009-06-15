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

import pivot.util.ListenerList;

public class ListenerListTest {
    public static class TestHandler implements TestListener {
        private int id;

        public TestHandler(int id) {
            this.id = id;
        }

        public void eventFired(TestSource testSource) {
            System.out.println("Event processed by " + id);
        }

        @Override
        public String toString() {
            return Integer.toString(id);
        }
    }

    public static void main(String[] args) {
        TestListener listener1 = new TestHandler(1);

        TestListener listener2 = new TestHandler(2) {
            public void eventFired(TestSource testSource) {
                System.out.println("Removing listener 2");
                testSource.getTestListeners().remove(this);
            }
        };

        TestListener listener3 = new TestHandler(3);
        TestListener listener4 = new TestHandler(4);

        final TestSource testSource = new TestSource();
        testSource.getTestListeners().add(listener1);
        testSource.getTestListeners().add(listener2);
        testSource.getTestListeners().add(listener3);
        testSource.getTestListeners().add(listener4);
        testSource.getTestListeners().add(listener3);
        testSource.getTestListeners().add(listener4);

        // testSource.getTestListeners().remove(listener1);
        // testSource.getTestListeners().remove(listener4);

        testSource.getTestListeners().add(listener1);

        testSource.fireEvent();
    }
}

interface TestListener {
    public void eventFired(TestSource testSource);
}

class TestSource {
    private static class TestListenerList extends ListenerList<TestListener>
        implements TestListener {
        public void eventFired(TestSource testSource) {
            for (TestListener listener : this) {
                listener.eventFired(testSource);
            }
        }
    }

    private TestListenerList testListeners = new TestListenerList();

    public void fireEvent() {
        testListeners.eventFired(this);
    }

    public ListenerList<TestListener> getTestListeners() {
        return testListeners;
    }
}
