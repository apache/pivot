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
package org.apache.pivot.tests;

import org.apache.pivot.util.MessageBus;
import org.apache.pivot.util.MessageBusListener;

public final class MessageBusTest {
    /** Hide utility class constructor. */
    private MessageBusTest() { }

    public enum TestMessage {
        HELLO, GOODBYE
    }

    public static void main(String[] args) {
        MessageBusListener<TestMessage> testMessageListener = new MessageBusListener<TestMessage>() {
            @Override
            public void messageSent(TestMessage message) {
                System.out.println(message);
            }
        };

        MessageBus.subscribe(TestMessage.class, testMessageListener); // subscribe
        MessageBus.sendMessage(TestMessage.HELLO); // a message will be printed

        MessageBus.unsubscribe(TestMessage.class, testMessageListener); // unsubscribe
        MessageBus.sendMessage(TestMessage.GOODBYE); // the message will not be printed
    }
}
