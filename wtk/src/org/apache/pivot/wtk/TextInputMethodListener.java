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

import java.awt.Rectangle;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.font.TextHitInfo;
import java.awt.im.InputMethodRequests;
import java.text.AttributedCharacterIterator;


/**
 * An interface expected to be implemented by every (text) component
 * that can interace with the Input Method Editors for on-the-spot
 * editing, esp. of Far Eastern languages (Chinese, Japanese, etc.).
 * <p> This interface encapsulates both the {@link InputMethodRequests}
 * and {@link InputMethodListener} standard interfaces to reduce the
 * number of objects needed.
 */
public interface TextInputMethodListener extends InputMethodRequests, InputMethodListener {

    /**
     * A default implementation of the {@link TextInputMethodListener} interface that can be used
     * to provide the minimum necessary functionality.
     */
    public static class Adapter implements TextInputMethodListener {
        @Override
        public AttributedCharacterIterator cancelLatestCommittedText(AttributedCharacterIterator.Attribute[] attributes) {
            return null;
        }

        @Override
        public AttributedCharacterIterator getCommittedText(int beginIndex, int endIndex, AttributedCharacterIterator.Attribute[] attributes) {
            return null;
        }

        @Override
        public int getCommittedTextLength() {
            return 0;
        }

        @Override
        public int getInsertPositionOffset() {
            return 0;
        }

        @Override
        public TextHitInfo getLocationOffset(int x, int y) {
            return null;
        }

        @Override
        public AttributedCharacterIterator getSelectedText(AttributedCharacterIterator.Attribute[] attributes) {
            return null;
        }

        @Override
        public Rectangle getTextLocation(TextHitInfo offset) {
            return new Rectangle();
        }

        @Override
        public void inputMethodTextChanged(InputMethodEvent event) {
            // empty block
        }

        @Override
        public void caretPositionChanged(InputMethodEvent event) {
            // empty block
        }
    }

    AttributedCharacterIterator cancelLatestCommittedText(AttributedCharacterIterator.Attribute[] attributes);

    AttributedCharacterIterator getCommittedText(int beginIndex, int endIndex, AttributedCharacterIterator.Attribute[] attributes);

    int getCommittedTextLength();

    int getInsertPositionOffset();

    TextHitInfo getLocationOffset(int x, int y);

    AttributedCharacterIterator getSelectedText(AttributedCharacterIterator.Attribute[] attributes);

    Rectangle getTextLocation(TextHitInfo offset);

    void inputMethodTextChanged(InputMethodEvent event);

    void caretPositionChanged(InputMethodEvent event);

}
