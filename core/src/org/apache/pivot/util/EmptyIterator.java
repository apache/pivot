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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A no-op iterator, for which {@link #hasNext} always returns <tt>false</tt>
 * and {@link #next} throws {@link NoSuchElementException}.
 * <p> This is used (for instance) for {@link org.apache.pivot.collections.HashMap}
 * when a hash bucket list is empty, so that iteration through the bucket list
 * doesn't have to be special-cased.
 * <p>Note: for Java 8 we have taken out the implementation of the <tt>remove()</tt>
 * method because the interface now implements it as we need it as a default method.
 */
public class EmptyIterator<T> implements Iterator<T> {
    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public T next() {
        throw new NoSuchElementException();
    }

}
