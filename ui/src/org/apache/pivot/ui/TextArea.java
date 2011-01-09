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
package org.apache.pivot.ui;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;

import org.apache.pivot.scene.Bounds;

public interface TextArea {
    public enum ScrollDirection {
        UP,
        DOWN
    }

    public interface TextBindMapping {
        public String toString(Object value);
        public Object valueOf(String text);
    }

    public String getText();
    public String getText(int start, int length);
    public void setText(String text);
    public void setText(URL textURL) throws IOException;
    public void setText(Reader textReader) throws IOException;

    public void insertText(CharSequence text, int index);
    public void removeText(int start, int length);

    public int getParagraphIndex(int index);
    public CharSequence getParagraphText(int paragraphIndex);

    public char getCharacter(int index);
    public int getCharacterCount();

    public void cut();
    public void copy();
    public void paste();

    public void undo();
    public void redo();

    public int getSelectionStart();
    public int getSelectionLength();
    public Span getSelection();
    public void setSelection(int selectionStart, int selectionLength);
    public void setSelection(Span selection);
    public void selectAll();
    public void clearSelection();
    public String getSelectedText();
    public int getMaximumLength();
    public void setMaximumLength(int maximumLength);

    public boolean isEditable();
    public void setEditable(boolean editable);

    public String getTextKey();
    public void setTextKey(String textKey);
    public BindType getTextBindType();
    public void setTextBindType(BindType textBindType);

    public int getInsertionPoint(int x, int y);
    public int getNextInsertionPoint(int x, int from, ScrollDirection direction);
    public int getRowIndex(int index);
    public int getRowOffset(int index);
    public int getRowLength(int index);
    public int getRowCount();

    public Bounds getCharacterBounds(int index);
}
