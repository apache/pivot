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
package org.apache.pivot.text;

import java.text.AttributedCharacterIterator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.util.Utils;


/**
 * An {@link AttributedCharacterIterator} that implements iterating over
 * several (typically two or three) underlying iterators.  The assumption
 * is that no attributes will ever cross the underlying iterator boundaries
 * (which is always the case for insertion of composed but uncommitted text
 * in the middle of two pieces of committed text (for instance).
 */
public class CompositeIterator implements AttributedCharacterIterator {

    private List<AttributedCharacterIterator> iterators = new ArrayList<>();
    private int endIndex;
    private int currentIndex;
    private AttributedCharacterIterator currentIterator;
    private int currentIteratorDelta;

    /**
     * Constructs a CompositeIterator that iterates over the concatenation
     * of one or more iterators.
     * @param iterators The base iterators that this composite iterator concatenates.
     */
    public CompositeIterator(AttributedCharacterIterator... iterators) {
        int fullLength = 0;
        for (AttributedCharacterIterator iter : iterators) {
            int beginIndex = iter.getBeginIndex();    // inclusive
            int endIndex   = iter.getEndIndex();      // exclusive
            int range = (endIndex - beginIndex);
            this.iterators.add(iter);
            fullLength += range;
        }
        this.endIndex = fullLength;
        __setIndex(0);
    }

    // CharacterIterator implementations

    public char first() {
        return __setIndex(0);
    }

    public char last() {
        if (endIndex == 0) {
            return __setIndex(endIndex);
        } else {
            return __setIndex(endIndex - 1);
        }
    }

    public char next() {
        if (currentIndex < endIndex) {
            return __setIndex(currentIndex + 1);
        } else {
            return DONE;
        }
    }

    public char previous() {
        if (currentIndex > 0) {
            return __setIndex(currentIndex - 1);
        } else {
            return DONE;
        }
    }

    public char current() {
        return currentIterator.setIndex(currentIndex - currentIteratorDelta);
    }

    public char setIndex(int position) {
        // Note: this is a (0 < position <= endIndex) check, since "endIndex" is a valid value here
        Utils.checkIndexBounds(position, 0, endIndex);

        return __setIndex(position);
    }

    private char __setIndex(int position) {
        currentIndex = position;
        int cumLength = 0;
        for (AttributedCharacterIterator iter : iterators) {
            int beginIndex = iter.getBeginIndex();
            int endIndex   = iter.getEndIndex();
            int range = endIndex - beginIndex;
            if (currentIndex < endIndex + cumLength) {
                currentIterator = iter;
                currentIteratorDelta = beginIndex + cumLength;
                // TODO: not sure this is going to be correct for > 2 iterators
                return currentIterator.setIndex(currentIndex - currentIteratorDelta);
            }
            cumLength += range;
        }
        return DONE;
    }

    public int getBeginIndex() {
        return 0;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public int getIndex() {
        return currentIndex;
    }

    // AttributedCharacterIterator implementations

    public int getRunStart() {
        return currentIterator.getRunStart() + currentIteratorDelta;
    }

    public int getRunLimit() {
        return currentIterator.getRunLimit() + currentIteratorDelta;
    }

    public int getRunStart(Attribute attribute) {
        return currentIterator.getRunStart(attribute) + currentIteratorDelta;
    }

    public int getRunLimit(Attribute attribute) {
        return currentIterator.getRunLimit(attribute) + currentIteratorDelta;
    }

    public int getRunStart(Set<? extends Attribute> attributes) {
        return currentIterator.getRunStart(attributes) + currentIteratorDelta;
    }

    public int getRunLimit(Set<? extends Attribute> attributes) {
        return currentIterator.getRunLimit(attributes) + currentIteratorDelta;
    }

    public Map<Attribute, Object> getAttributes() {
        return currentIterator.getAttributes();
    }

    public Set<Attribute> getAllAttributeKeys() {
        Set<Attribute> keys = new HashSet<>();
        for (AttributedCharacterIterator iter : iterators) {
            keys.addAll(iter.getAllAttributeKeys());
        }
        return keys;
    }

    public Object getAttribute(Attribute attribute) {
        return currentIterator.getAttribute(attribute);
    }

    // Object implementations

    public Object clone() {
        try {
            CompositeIterator other = (CompositeIterator) super.clone();
            return other;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

}
