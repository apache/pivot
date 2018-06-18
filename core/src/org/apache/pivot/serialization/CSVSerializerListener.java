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
package org.apache.pivot.serialization;

import org.apache.pivot.collections.List;
import org.apache.pivot.util.ListenerList;

/**
 * CSV serializer listener interface.
 */
public interface CSVSerializerListener {
    /**
     * CSV Serializer listeners.
     */
    public static final class Listeners extends ListenerList<CSVSerializerListener>
        implements CSVSerializerListener {
        @Override
        public void beginList(final CSVSerializer csvSerializer, final List<?> list) {
            forEach(listener -> listener.beginList(csvSerializer, list));
        }

        @Override
        public void endList(final CSVSerializer csvSerializer) {
            forEach(listener -> listener.endList(csvSerializer));
        }

        @Override
        public void readItem(final CSVSerializer csvSerializer, final Object item) {
            forEach(listener -> listener.readItem(csvSerializer, item));
        }
    }

    /**
     * CSV serializer listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements CSVSerializerListener {
        @Override
        public void beginList(final CSVSerializer csvSerializer, final List<?> list) {
            // empty block
        }

        @Override
        public void endList(final CSVSerializer csvSerializer) {
            // empty block
        }

        @Override
        public void readItem(final CSVSerializer csvSerializer, final Object item) {
            // empty block
        }
    }

    /**
     * Called when the serializer has begun reading the list.
     *
     * @param csvSerializer The active serializer.
     * @param list The list just begun.
     */
    default void beginList(CSVSerializer csvSerializer, List<?> list) {
    }

    /**
     * Called when the serializer has finished reading the list.
     *
     * @param csvSerializer The current serializer.
     */
    default void endList(CSVSerializer csvSerializer) {
    }

    /**
     * Called when the serializer has read an item.
     *
     * @param csvSerializer The current serializer.
     * @param item The item just read.
     */
    default void readItem(CSVSerializer csvSerializer, Object item) {
    }
}
