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
package org.apache.pivot.wtk;

import org.apache.pivot.collections.Sequence;

/**
 * Exposes the contents of a {@link ListSelection} as a sequence.
 *
 * @author gbrown
 */
class ListSelectionSequence implements Sequence<Span> {
    private ListSelection listSelection;

    public ListSelectionSequence(ListSelection listSelection) {
        this.listSelection = listSelection;
    }

    public int add(Span span) {
        throw new UnsupportedOperationException();
    }

    public void insert(Span span, int index) {
        throw new UnsupportedOperationException();
    }

    public Span update(int index, Span span) {
        throw new UnsupportedOperationException();
    }

    public int remove(Span span) {
        throw new UnsupportedOperationException();
    }

    public Sequence<Span> remove(int index, int count) {
        throw new UnsupportedOperationException();
    }

    public Span get(int index) {
        return listSelection.get(index);
    }

    public int indexOf(Span span) {
        return listSelection.indexOf(span);
    }

    public int getLength() {
        return listSelection.getLength();
    }
}
