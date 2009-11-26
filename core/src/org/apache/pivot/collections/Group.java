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
 * Interface representing a group of unique elements.
 */
public interface Group<E> {
    /**
     * Adds an element to the group.
     *
     * @param element
     * The element to add to the group.
     *
     * @return
     * <tt>true</tt> if the element was added to the group; <tt>false</tt>,
     * otherwise.
     */
    public boolean add(E element);

    /**
     * Removes an element from the group.
     *
     * @param element
     * The element to remove from the set.
     *
     * @return
     * <tt>true</tt> if the element was removed from the group; <tt>false</tt>,
     * otherwise.
     */
    public boolean remove(E element);

    /**
     * Tests the existence of an element in the group.
     *
     * @param element
     * The element whose presence in the group is to be tested.
     *
     * @return
     * <tt>true</tt> if the element exists in the group; <tt>false</tt>,
     * otherwise.
     */
    public boolean contains(E element);
}
