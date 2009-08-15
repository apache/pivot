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
package org.apache.pivot.collections.concurrent;

import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.collections.Collection;
import org.apache.pivot.util.ImmutableIterator;


/**
 * Abstract base class for synchronized collection wrappers.
 * <p>
 * NOTE In order to guarantee thread-safe access, all access to the backing
 * collection must be via the synchronized wrapper.
 *
 */
public abstract class SynchronizedCollection<T> implements Collection<T> {
    protected Collection<T> collection = null;

    public SynchronizedCollection(Collection<T> collection) {
        if (collection == null) {
            throw new IllegalArgumentException("collection is null.");
        }

        this.collection = collection;
    }

    public synchronized void clear() {
        collection.clear();
    }

    public synchronized Comparator<T> getComparator() {
        return collection.getComparator();
    }

    public synchronized void setComparator(Comparator<T> comparator) {
        collection.setComparator(comparator);
    }

    /**
     * NOTE Callers must manually synchronize on the SynchronizedCollection
     * instance to ensure thread safety during iteration.
     */
    public Iterator<T> iterator() {
        return new ImmutableIterator<T>(collection.iterator());
    }
}
