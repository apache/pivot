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

import java.text.CharacterIterator;

/**
 * Character iterator that is backed by a {@link java.lang.CharSequence}.
 */
public final class CharSequenceCharacterIterator implements CharacterIterator {
    private CharSequence charSequence;
    private int beginIndex;
    private int endIndex;

    private int index = -1;

    public CharSequenceCharacterIterator(CharSequence charSequence) {
        this(charSequence, 0);
    }

    public CharSequenceCharacterIterator(CharSequence charSequence, int beginIndex) {
        this(charSequence, beginIndex, -1);
    }

    public CharSequenceCharacterIterator(CharSequence charSequence, int beginIndex, int endIndex) {
        this(charSequence, beginIndex, endIndex, beginIndex);
    }

    public CharSequenceCharacterIterator(CharSequence charSequence, int beginIndex, int endIndex, int index) {
        if (charSequence == null) {
            throw new IllegalArgumentException("charSequence may not be null");
        }

        int endIndexUpdated = endIndex;

        if (endIndex == -1) {
            endIndexUpdated = charSequence.length();
        }

        if (beginIndex > endIndexUpdated) {
            throw new IllegalArgumentException("beginIndex > endIndex, " + beginIndex + ">" + endIndexUpdated);
        }

        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException("beginIndex < 0, " + beginIndex);
        }

        if (endIndexUpdated > charSequence.length()) {
            throw new IndexOutOfBoundsException("endIndex > char sequence length, " + endIndexUpdated + ">" + charSequence.length());
        }

        if (index < beginIndex) {
            throw new IndexOutOfBoundsException("(index < beginIndex, " + index + "<" + beginIndex);
        }

        if (index > endIndexUpdated) {
            throw new IndexOutOfBoundsException("(index > endIndex, " + index + ">" + endIndexUpdated);
        }

        this.charSequence = charSequence;
        this.beginIndex = beginIndex;
        this.endIndex = endIndexUpdated;

        setIndex(index);
    }

    @Override
    public char first() {
        return setIndex(beginIndex);
    }

    @Override
    public char last() {
        return setIndex(charSequence.length() == 0 ? endIndex : endIndex - 1);
    }

    @Override
    public char next() {
        return setIndex(index < endIndex ? index + 1 : DONE);
    }

    @Override
    public char previous() {
        return setIndex(index > beginIndex ? index - 1 : DONE);
    }

    @Override
    public char current() {
        return (index < endIndex) ? charSequence.charAt(index) : DONE;
    }

    @Override
    public int getBeginIndex() {
        return beginIndex;
    }

    @Override
    public int getEndIndex() {
        return endIndex;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public char setIndex(int index) {
        if (index < beginIndex
            || index > endIndex) {
            throw new IndexOutOfBoundsException();
        }

        this.index = index;

        return current();
    }

    @Override
    public Object clone() {
        return new CharSequenceCharacterIterator(charSequence, beginIndex, endIndex, index);
    }
}
