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
package org.apache.pivot.json;

import java.util.List;
import java.util.Map;

/**
 * JSON serializer listener interface.
 */
public interface JSONSerializerListener {
    /**
     * JSON serializer listener adapter.
     */
    public static class Adapter implements JSONSerializerListener {
        @Override
        public void beginMap(JSONSerializer jsonSerializer, Map<String, ?> value) {
        }

        @Override
        public void endMap(JSONSerializer jsonSerializer) {
        }

        @Override
        public void readKey(JSONSerializer jsonSerializer, String key) {
        }

        @Override
        public void beginList(JSONSerializer jsonSerializer, List<?> value) {
        }

        @Override
        public void endList(JSONSerializer jsonSerializer) {
        }

        @Override
        public void readString(JSONSerializer jsonSerializer, String value) {
        }

        @Override
        public void readNumber(JSONSerializer jsonSerializer, Number value) {
        }

        @Override
        public void readBoolean(JSONSerializer jsonSerializer, Boolean value) {
        }

        @Override
        public void readNull(JSONSerializer jsonSerializer) {
        }
    }

    /**
     * Called when the serializer has begun reading a map value.
     *
     * @param jsonSerializer
     * @param value
     */
    public void beginMap(JSONSerializer jsonSerializer, Map<String, ?> value);

    /**
     * Called when the serializer has finished reading a map value.
     *
     * @param jsonSerializer
     */
    public void endMap(JSONSerializer jsonSerializer);

    /**
     * Called when the serializer has read a map key.
     *
     * @param jsonSerializer
     * @param key
     */
    public void readKey(JSONSerializer jsonSerializer, String key);

    /**
     * Called when the serializer has begun reading a list value.
     *
     * @param jsonSerializer
     * @param value
     */
    public void beginList(JSONSerializer jsonSerializer, List<?> value);

    /**
     * Called when the serializer has finished reading a list value.
     *
     * @param jsonSerializer
     */
    public void endList(JSONSerializer jsonSerializer);

    /**
     * Called when the serializer has read a string value.
     *
     * @param jsonSerializer
     * @param value
     */
    public void readString(JSONSerializer jsonSerializer, String value);

    /**
     * Called when the serializer has read a numeric value.
     *
     * @param jsonSerializer
     * @param value
     */
    public void readNumber(JSONSerializer jsonSerializer, Number value);

    /**
     * Called when the serializer has read a boolean value.
     *
     * @param jsonSerializer
     * @param value
     */
    public void readBoolean(JSONSerializer jsonSerializer, Boolean value);

    /**
     * Called when the serializer has read a null value.
     *
     * @param jsonSerializer
     */
    public void readNull(JSONSerializer jsonSerializer);
}
