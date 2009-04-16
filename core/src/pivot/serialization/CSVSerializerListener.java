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
package pivot.serialization;

/**
 * CSV serializer listener interface.
 *
 * @author gbrown
 */
public interface CSVSerializerListener {
    /**
     * Called when an item has been read by a CSV serializer.
     *
     * @param csvSerializer
     * @param item
     */
    public void itemRead(CSVSerializer csvSerializer, Object item);

    /**
     * Called when all items have been read by a CSV serializer.
     *
     * @param csvSerializer
     */
    public void allItemsRead(CSVSerializer csvSerializer);
}
