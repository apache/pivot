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
package org.apache.pivot.collections;

/**
 * Implements various assert-style checking for the Pivot collections classes.
 * Throws nice descriptive exceptions if something goes wrong.
 * 
 * @author Noel Grandin
 */
public class CollectionArgChecks {

    public static void notNull(String fieldName, Object field) {
        if (field == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }
    
    public static void zeroOrGreater(String fieldName, int field) {
        if (field < 0) {
            throw new IllegalArgumentException(fieldName + " " + field + " cannot be < 0");
        }
    }
    
    public static void indexBounds(int index, int boundStart, int boundEnd) {
        if (index < boundStart || index > boundEnd) {
            throw new IndexOutOfBoundsException("index " + index + " out of bounds");
        }
    }
    
    public static void indexBounds(int index, int count, int boundStart, int boundEnd) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        if (index < boundStart) {
            throw new IndexOutOfBoundsException("index " + index + " out of bounds");
        }
        if (index + count > boundEnd) {
            throw new IndexOutOfBoundsException("index + count " + index + "," + count + " out of range");
        }
    }
}
