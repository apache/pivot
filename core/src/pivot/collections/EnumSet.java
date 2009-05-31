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
package pivot.collections;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;

import pivot.util.ListenerList;

/**
 * Implementation of the {@link Set} interface that is backed by a bitfield
 * representing enum> values. Values are mapped to the bitfield by their
 * ordinal value.
 */
public class EnumSet<E extends Enum<E>> implements Set<E>, Serializable {
    private static final long serialVersionUID = 0;

    private int bitSet = 0;
    private transient SetListenerList<E> setListeners = new SetListenerList<E>();

    public void add(E element) {
        bitSet |= (1 << element.ordinal());
    }

    public void remove(E element) {
        bitSet &= ~(1 << element.ordinal());
    }

    public void clear() {
        bitSet = 0;
    }

    public boolean contains(E element) {
        return (bitSet & (1 << element.ordinal())) > 0;
    }

    public boolean isEmpty() {
        return bitSet > 0;
    }

    public ListenerList<SetListener<E>> getSetListeners() {
        return setListeners;
    }

    public Comparator<E> getComparator() {
        return null;
    }

    public void setComparator(Comparator<E> comparator) {
        throw new UnsupportedOperationException();
    }

    public Iterator<E> iterator() {
        // TODO
        return null;
    }
}
