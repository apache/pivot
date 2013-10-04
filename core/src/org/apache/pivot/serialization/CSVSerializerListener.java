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

/**
 * CSV serializer listener interface.
 */
public interface CSVSerializerListener {
    /**
     * CSV serializer listener adapter.
     */
    public static class Adapter implements CSVSerializerListener {
        @Override
        public void beginList(CSVSerializer csvSerializer, List<?> list) {
            // empty block
        }

        @Override
        public void endList(CSVSerializer csvSerializer) {
            // empty block
        }

        @Override
        public void readItem(CSVSerializer csvSerializer, Object item) {
            // empty block
        }
    }

    /**
     * Called when the serializer has begun reading the list.
     *
     * @param csvSerializer
     * @param list
     */
    public void beginList(CSVSerializer csvSerializer, List<?> list);

    /**
     * Called when the serializer has finished reading the list.
     *
     * @param csvSerializer
     */
    public void endList(CSVSerializer csvSerializer);

    /**
     * Called when the serializer has read an item.
     *
     * @param csvSerializer
     * @param item
     */
    public void readItem(CSVSerializer csvSerializer, Object item);
}
