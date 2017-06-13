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

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.pivot.util.Utils;


/**
 * A sequence of text along with associated attributes, backed by a
 * {@link AttributedString}, which itself implements all of these
 * methods.
 */
public class AttributedStringCharacterIterator implements AttributedCharacterIterator {
    private AttributedString storage = null;
    private int length = 0;
    private AttributedCharacterIterator.Attribute[] attributes = null;
    private AttributedCharacterIterator iterator = null;

    public AttributedStringCharacterIterator() {
        this.storage = new AttributedString("");
        this.length = 0;
    }

    public AttributedStringCharacterIterator(String text) {
        Utils.checkNull(text, "text");
        this.storage = new AttributedString(text);
    }

    private void addAttribute(AttributedCharacterIterator.Attribute attribute, Object value) {
        if (this.length != 0) {
            this.storage.addAttribute(attribute, value);
        }
    }

    public AttributedStringCharacterIterator(CharSequence charSequence, Font font) {
        this.storage = new AttributedString(charSequence.toString());
        this.length = charSequence.length();
        addAttribute(TextAttribute.FONT, font);
    }

    public AttributedStringCharacterIterator(CharSequence charSequence, int beginIndex, Font font) {
        this.storage = new AttributedString(charSequence.subSequence(beginIndex, charSequence.length()).toString());
        this.length = charSequence.length() - beginIndex;
        addAttribute(TextAttribute.FONT, font);
    }

    public AttributedStringCharacterIterator(String text, int beginIndex, int endIndex) {
        Utils.checkNull(text, "text");
        this.storage = new AttributedString(text.substring(beginIndex, endIndex));
        this.length = endIndex - beginIndex;
    }

    public AttributedStringCharacterIterator(AttributedCharacterIterator iter, int beginIndex, int endIndex) {
        this.storage = new AttributedString(iter, beginIndex, endIndex);
        this.length = endIndex - beginIndex;
    }

    public AttributedStringCharacterIterator(String text, AttributedCharacterIterator.Attribute[] attributes) {
        Utils.checkNull(text, "text");
        this.storage = new AttributedString(text);
        this.length = text.length();
        this.attributes = attributes;
    }

    public AttributedStringCharacterIterator(String text, int beginIndex, int endIndex, AttributedCharacterIterator.Attribute[] attributes) {
        Utils.checkNull(text, "text");
        this.storage = new AttributedString(text.substring(beginIndex, endIndex));
        this.length = endIndex - beginIndex;
        this.attributes = attributes;
    }

    private AttributedStringCharacterIterator(AttributedString attributedString) {
        // TODO: maybe we should make a copy? instead of adding the reference???
        this.storage = attributedString;
    }

    // TODO: many more constructors needed, esp. those with attributes already in place

    public void addUnderlineAttribute(boolean underline) {
        addAttribute(TextAttribute.UNDERLINE,
            underline ? TextAttribute.UNDERLINE_ON : Integer.valueOf(-1));
    }

    public void addStrikethroughAttribute(boolean strikethrough) {
        addAttribute(TextAttribute.STRIKETHROUGH,
            strikethrough ? TextAttribute.STRIKETHROUGH_ON : Boolean.FALSE);
    }

    // TODO: do we need more parameters here?  for start position or anything else?
    private AttributedCharacterIterator getIter() {
        Utils.checkNull(this.storage, "source text");

        if (this.iterator == null) {
            if (this.attributes != null) {
                this.iterator = storage.getIterator(attributes);
            } else {
                this.iterator = storage.getIterator();
            }
        }
        return this.iterator;
    }

    /**
     * Reset this iterator, meaning recreate the underlying iterator
     * on the next call.
     */
    public void reset() {
        this.iterator = null;
    }

    @Override
    public Set<Attribute> getAllAttributeKeys() {
        return getIter().getAllAttributeKeys();
    }

    @Override
    public Object getAttribute(Attribute attribute) {
        return getIter().getAttribute(attribute);
    }

    @Override
    public Map<Attribute, Object> getAttributes() {
        return getIter().getAttributes();
    }

    @Override
    public int getRunLimit() {
        return getIter().getRunLimit();
    }

    @Override
    public int getRunLimit(Attribute attribute) {
        return getIter().getRunLimit(attribute);
    }

    @Override
    public int getRunLimit(Set<? extends Attribute> attributes) {
        return getIter().getRunLimit(attributes);
    }

    @Override
    public int getRunStart() {
        return getIter().getRunStart();
    }

    @Override
    public int getRunStart(Attribute attribute) {
        return getIter().getRunStart(attribute);
    }

    @Override
    public int getRunStart(Set<? extends Attribute> attributes) {
        return getIter().getRunStart(attributes);
    }

    @Override
    public char first() {
        return getIter().first();
    }

    @Override
    public char last() {
        return getIter().last();
    }

    @Override
    public char current() {
        return getIter().current();
    }

    @Override
    public char next() {
        return getIter().next();
    }

    @Override
    public char previous() {
        return getIter().previous();
    }

    @Override
    public char setIndex(int position) {
        return getIter().setIndex(position);
    }

    @Override
    public int getBeginIndex() {
        return getIter().getBeginIndex();
    }

    @Override
    public int getEndIndex() {
        return getIter().getEndIndex();
    }

    @Override
    public int getIndex() {
        return getIter().getIndex();
    }

    @Override
    public Object clone() {
        AttributedStringCharacterIterator obj = new AttributedStringCharacterIterator(this.storage);
        // Copy over the other fields
        obj.length = this.length;
        obj.attributes = this.attributes;
        return obj;
    }

    @Override
    public String toString() {
        AttributedCharacterIterator iter = storage.getIterator();
        StringBuilder buf = new StringBuilder(iter.getEndIndex());
        char ch = iter.first();
        while (ch != DONE) {
            buf.append(ch);
            ch = iter.next();
        }
        return buf.toString();
    }

}
