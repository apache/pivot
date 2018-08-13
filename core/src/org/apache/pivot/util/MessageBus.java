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
package org.apache.pivot.util;

import org.apache.pivot.collections.HashMap;

/**
 * Provides support for basic intra-application message passing.
 */
public final class MessageBus {
    /**
     * Private constructor for utility class.
     */
    private MessageBus() {
    }

    /** The master list of topics and their listeners. */
    private static HashMap<Class<?>, ListenerList<MessageBusListener<?>>> messageTopics = new HashMap<>();

    /**
     * Subscribes a listener to a message topic.
     *
     * @param <T> The type of the topic.
     * @param topic The topic class to subscribe to.
     * @param messageListener The listener we want to attach.
     */
    public static <T> void subscribe(final Class<? super T> topic, final MessageBusListener<T> messageListener) {
        ListenerList<MessageBusListener<?>> topicListeners = messageTopics.get(topic);

        if (topicListeners == null) {
            topicListeners = new ListenerList<MessageBusListener<?>>() {
                // empty block
            };
            messageTopics.put(topic, topicListeners);
        }

        topicListeners.add(messageListener);
    }

    /**
     * Unsubscribe a listener from a message topic.
     *
     * @param <T> The type of the topic.
     * @param topic The topic class to subscribe to.
     * @param messageListener The listener to unsubscribe.
     * @throws IllegalArgumentException if there are no listeners subscribed.
     */
    public static <T> void unsubscribe(final Class<? super T> topic, final MessageBusListener<T> messageListener) {
        ListenerList<MessageBusListener<?>> topicListeners = messageTopics.get(topic);

        if (topicListeners == null) {
            throw new IllegalArgumentException(topic.getName() + " does not exist.");
        }

        topicListeners.remove(messageListener);
        if (topicListeners.isEmpty()) {
            messageTopics.remove(topic);
        }
    }

    /**
     * Sends a message to subscribed topic listeners.
     *
     * @param <T> The type of the topic.
     * @param message The message to send to all subscribed listeners.
     */
    @SuppressWarnings("unchecked")
    public static <T> void sendMessage(final T message) {
        Class<?> topic = message.getClass();
        ListenerList<MessageBusListener<?>> topicListeners = messageTopics.get(topic);

        if (topicListeners != null) {
            for (MessageBusListener<?> listener : topicListeners) {
                ((MessageBusListener<T>) listener).messageSent(message);
            }
        }
    }
}
