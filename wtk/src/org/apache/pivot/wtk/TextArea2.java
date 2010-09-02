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

import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.text.CharacterIterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.json.JSON;
import org.apache.pivot.util.ListenerList;

/**
 * A component that allows a user to enter multiple lines of unformatted text.
 */
public class TextArea2 extends Component {
    /**
     * Text area skin interface. Text area skins are required to implement
     * this.
     */
    public interface Skin {
        /**
         * Returns the insertion point for a given location.
         *
         * @param x
         * @param y
         *
         * @return
         * The insertion point for the given location.
         */
        public int getInsertionPoint(int x, int y);

        /**
         * Returns the next insertion point given an x coordinate and a character offset.
         *
         * @param x
         * @param from
         * @param direction
         *
         * @return
         * The next insertion point.
         */
        public int getNextInsertionPoint(int x, int from, FocusTraversalDirection direction);

        /**
         * Returns the row index of the character at a given offset within the document.
         *
         * @param offset
         *
         * @return
         * The row index of the character at the given offset.
         */
        public int getRowIndex(int offset);

        /**
         * Returns the total number of rows in the document.
         *
         * @return
         * The number of rows in the document.
         */
        public int getRowCount();

        /**
         * Returns the bounds of the character at a given offset within the
         * document.
         *
         * @param offset
         *
         * @return
         * The bounds of the character at the given offset.
         */
        public Bounds getCharacterBounds(int offset);
    }

    /**
     * Translates between text and context data during data binding.
     */
    public interface TextBindMapping {
        /**
         * Converts a value from the bind context to a text representation during a
         * {@link Component#load(Object)} operation.
         *
         * @param value
         */
        public String toString(Object value);

        /**
         * Converts a text string to a value to be stored in the bind context during a
         * {@link Component#store(Object)} operation.
         *
         * @param text
         */
        public Object valueOf(String text);
    }

    private static class TextAreaListenerList extends ListenerList<TextAreaListener2>
        implements TextAreaListener2 {
        @Override
        public void maximumLengthChanged(TextArea2 textArea, int previousMaximumLength) {
            for (TextAreaListener2 listener : this) {
                listener.maximumLengthChanged(textArea, previousMaximumLength);
            }
        }

        @Override
        public void editableChanged(TextArea2 textArea) {
            for (TextAreaListener2 listener : this) {
                listener.editableChanged(textArea);
            }
        }
    }

    private static class TextAreaContentListenerList extends ListenerList<TextAreaContentListener2>
        implements TextAreaContentListener2 {
        @Override
        public void textInserted(TextArea2 textArea, int index, int count) {
            for (TextAreaContentListener2 listener : this) {
                listener.textInserted(textArea, index, count);
            }
        }

        @Override
        public void textRemoved(TextArea2 textArea, int index, int count) {
            for (TextAreaContentListener2 listener : this) {
                listener.textRemoved(textArea, index, count);
            }
        }

        @Override
        public void textChanged(TextArea2 textArea) {
            for (TextAreaContentListener2 listener : this) {
                listener.textChanged(textArea);
            }
        }
    }

    private static class TextAreaSelectionListenerList extends ListenerList<TextAreaSelectionListener2>
        implements TextAreaSelectionListener2 {
        @Override
        public void selectionChanged(TextArea2 textArea, int previousSelectionStart,
            int previousSelectionLength) {
            for (TextAreaSelectionListener2 listener : this) {
                listener.selectionChanged(textArea, previousSelectionStart,
                    previousSelectionLength);
            }
        }
    }

    private static class TextAreaBindingListenerList extends ListenerList<TextAreaBindingListener2>
        implements TextAreaBindingListener2 {
        @Override
        public void textKeyChanged(TextArea2 textArea, String previousTextKey) {
            for (TextAreaBindingListener2 listener : this) {
                listener.textKeyChanged(textArea, previousTextKey);
            }
        }

        @Override
        public void textBindTypeChanged(TextArea2 textArea, BindType previousTextBindType) {
            for (TextAreaBindingListener2 listener : this) {
                listener.textBindTypeChanged(textArea, previousTextBindType);
            }
        }

        @Override
        public void textBindMappingChanged(TextArea2 textArea, TextBindMapping previousTextBindMapping) {
            for (TextAreaBindingListener2 listener : this) {
                listener.textBindMappingChanged(textArea, previousTextBindMapping);
            }
        }
    }

    private ArrayList<StringBuilder> paragraphs = null;
    private int length = 0;

    private int selectionStart = 0;
    private int selectionLength = 0;

    private int maximumLength = Integer.MAX_VALUE;
    private boolean editable = true;

    private String textKey = null;
    private BindType textBindType = BindType.BOTH;
    private TextBindMapping textBindMapping = null;

    private TextAreaListenerList textAreaListeners = new TextAreaListenerList();
    private TextAreaContentListenerList textAreaContentListeners = new TextAreaContentListenerList();
    private TextAreaSelectionListenerList textAreaSelectionListeners = new TextAreaSelectionListenerList();
    private TextAreaBindingListenerList textAreaBindingListeners = new TextAreaBindingListenerList();

    public TextArea2() {
        setText("");

        installThemeSkin(TextArea2.class);
    }

    @Override
    protected void setSkin(org.apache.pivot.wtk.Skin skin) {
        if (!(skin instanceof TextArea2.Skin)) {
            throw new IllegalArgumentException("Skin class must implement "
                + TextArea.Skin.class.getName());
        }

        super.setSkin(skin);
    }

    public CharacterIterator getCharacters() {
        // TODO
        return null;
    }

    /**
     * Returns the text content of the text area.
     *
     * @return
     * A string containing a copy of the text area's text content.
     */
    public String getText() {
        return getText(0, getCharacterCount());
    }

    /**
     * Returns a portion of the text content of the text input.
     *
     * @param beginIndex
     * @param endIndex
     *
     * @return
     * A string containing a copy of the text area's text content.
     */
    public String getText(int beginIndex, int endIndex) {
        // TODO
        return null;
    }

    /**
     * Sets the text content of the text area.
     *
     * @param text
     */
    public void setText(String text) {
        if (text == null) {
            throw new IllegalArgumentException();
        }

        if (text.length() > maximumLength) {
            throw new IllegalArgumentException("Text length is greater than maximum length.");
        }

        try {
            setText(new StringReader(text));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setText(URL textURL) throws IOException {
        if (textURL == null) {
            throw new IllegalArgumentException();
        }

        InputStream inputStream = null;
        try {
            inputStream = textURL.openStream();
            setText(new InputStreamReader(inputStream));
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public void setText(Reader textReader) throws IOException {
        if (textReader == null) {
            throw new IllegalArgumentException();
        }

        // Construct the paragraph list
        paragraphs = new ArrayList<StringBuilder>();
        length = 0;

        StringBuilder paragraph = new StringBuilder();

        int c = textReader.read();
        while (c != -1) {
            if (c == '\n') {
                paragraphs.add(paragraph);
                paragraph = new StringBuilder();
            } else {
                paragraph.append(c);
            }

            length++;

            c = textReader.read();
        }

        paragraphs.add(paragraph);

        // Update selection
        int previousSelectionStart = selectionStart;
        int previousSelectionLength = selectionLength;
        selectionStart = length;
        selectionLength = 0;

        // Fire change events
        textAreaContentListeners.textChanged(this);

        if (selectionStart != previousSelectionStart
            || selectionLength != previousSelectionLength) {
            textAreaSelectionListeners.selectionChanged(this, selectionStart, selectionLength);
        }
    }

    public void insertText(String text, int index) {
        if (text == null) {
            throw new IllegalArgumentException();
        }

        if (length + text.length() > maximumLength) {
            throw new IllegalArgumentException("Insertion of text would exceed maximum length.");
        }

        // Insert the text
        if (text.length() > 0) {
            // TODO (don't forget to update length)

            // Update selection
            int previousSelectionStart = selectionStart;
            int previousSelectionLength = selectionLength;
            selectionStart = index + text.length();
            selectionLength = 0;

            // Fire change events
            textAreaContentListeners.textInserted(this, previousSelectionStart, text.length());
            textAreaContentListeners.textChanged(this);

            if (selectionStart != previousSelectionStart
                || selectionLength != previousSelectionLength) {
                textAreaSelectionListeners.selectionChanged(this, selectionStart, selectionLength);
            }
        }
    }

    public void removeText(int index, int count) {
        if (count > 0) {
            // TODO (don't forget to update length)

            // Update the selection
            int previousSelectionStart = selectionStart;
            int previousSelectionLength = selectionLength;
            selectionStart = index;
            selectionLength = 0;

            // Fire change events
            textAreaContentListeners.textRemoved(this, selectionStart, count);
            textAreaContentListeners.textChanged(this);

            if (selectionStart != previousSelectionStart
                || selectionLength != previousSelectionLength) {
                textAreaSelectionListeners.selectionChanged(this, selectionStart, selectionLength);
            }
        }
    }

    /**
     * Returns the number of characters in the text area, including line break
     * characters.
     */
    public int getCharacterCount() {
        return length;
    }

    /**
     * Returns a character iterator over the text area's content.
     */
    public CharacterIterator getCharacterIterator() {
        return getCharacterIterator(0, getCharacterCount());
    }

    /**
     * Returns a character iterator over a portion the text area's content.
     */
    public CharacterIterator getCharacterIterator(int beginIndex, int endIndex) {
        // TODO
        return null;
    }

    /**
     * Places any selected text on the clipboard and deletes it from
     * the text input.
     */
    public void cut() {
        copy();
        removeText(selectionStart, selectionLength);
    }

    /**
     * Places any selected text on the clipboard.
     */
    public void copy() {
        // Copy selection to clipboard
        String selectedText = getSelectedText();

        if (selectedText.length() > 0) {
            LocalManifest clipboardContent = new LocalManifest();
            clipboardContent.putText(selectedText);
            Clipboard.setContent(clipboardContent);
        }
    }

    /**
     * Inserts text from the clipboard into the text input.
     */
    public void paste() {
        Manifest clipboardContent = Clipboard.getContent();

        if (clipboardContent != null
            && clipboardContent.containsText()) {
            // Paste the string representation of the content
            String text = null;
            try {
                text = clipboardContent.getText();
            } catch(IOException exception) {
                // No-op
            }

            if (text != null) {
                if ((length + text.length()) > maximumLength) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    insertText(text, selectionStart);
                }
            }
        }
    }

    public void undo() {
        // TODO
    }

    public void redo() {
        // TODO
    }

    /**
     * Returns the starting index of the selection.
     *
     * @return
     * The starting index of the selection.
     */
    public int getSelectionStart() {
        return selectionStart;
    }

    /**
     * Returns the length of the selection.
     *
     * @return
     * The length of the selection; may be <tt>0</tt>.
     */
    public int getSelectionLength() {
        return selectionLength;
    }

    /**
     * Returns a span representing the current selection.
     *
     * @return
     * A span containing the current selection. Both start and end points are
     * inclusive. Returns <tt>null</tt> if the selection is empty.
     */
    public Span getSelection() {
        return (selectionLength == 0) ? null : new Span(selectionStart,
            selectionStart + selectionLength - 1);
    }

    /**
     * Sets the selection. The sum of the selection start and length must be
     * less than the length of the text area's content.
     *
     * @param selectionStart
     * The starting index of the selection.
     *
     * @param selectionLength
     * The length of the selection.
     */
    public void setSelection(int selectionStart, int selectionLength) {
        if (selectionLength < 0) {
            throw new IllegalArgumentException("selectionLength is negative.");
        }

        if (selectionStart < 0
            || selectionStart + selectionLength > length) {
            throw new IndexOutOfBoundsException();
        }

        int previousSelectionStart = this.selectionStart;
        int previousSelectionLength = this.selectionLength;

        if (previousSelectionStart != selectionStart
            || previousSelectionLength != selectionLength) {
            this.selectionStart = selectionStart;
            this.selectionLength = selectionLength;

            textAreaSelectionListeners.selectionChanged(this, previousSelectionStart,
                previousSelectionLength);
        }
    }

    /**
     * Sets the selection.
     *
     * @param selection
     *
     * @see #setSelection(int, int)
     */
    public final void setSelection(Span selection) {
        if (selection == null) {
            throw new IllegalArgumentException("selection is null.");
        }

        setSelection(Math.min(selection.start, selection.end), (int)selection.getLength());
    }

    /**
     * Selects all text.
     */
    public void selectAll() {
        setSelection(0, length);
    }

    /**
     * Clears the selection.
     */
    public void clearSelection() {
        setSelection(0, 0);
    }

    /**
     * Returns the selected text.
     *
     * @return
     * A string containing a copy of the selected text.
     */
    public String getSelectedText() {
        return getText(selectionStart, selectionStart + selectionLength);
    }

    /**
     * Returns the maximum length of the text area's text content.
     *
     * @return
     * The maximum length of the text area's text content.
     */
    public int getMaximumLength() {
        return maximumLength;
    }

    /**
     * Sets the maximum length of the text area's text content.
     *
     * @param maximumLength
     * The maximum length of the text area's text content.
     */
    public void setMaximumLength(int maximumLength) {
        if (maximumLength < 0) {
            throw new IllegalArgumentException("maximumLength is negative.");
        }

        int previousMaximumLength = this.maximumLength;

        if (previousMaximumLength != maximumLength) {
            int previousTextLength = length;

            this.maximumLength = maximumLength;

            // Truncate the text, if necessary
            if (previousTextLength > maximumLength) {
                // TODO (don't forget to update length)
            }

            // Fire change events
            textAreaListeners.maximumLengthChanged(this, previousMaximumLength);

            if (length != previousTextLength) {
                textAreaContentListeners.textChanged(this);
            }
        }
    }

    /**
     * Returns the text area's editable flag.
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Sets the text area's editable flag.
     *
     * @param editable
     */
    public void setEditable(boolean editable) {
        if (this.editable != editable) {
            if (!editable) {
                if (isFocused()) {
                    clearFocus();
                }
            }

            this.editable = editable;

            textAreaListeners.editableChanged(this);
        }
    }

    /**
     * Returns the text area's text key.
     *
     * @return
     * The text key, or <tt>null</tt> if no text key is set.
     */
    public String getTextKey() {
        return textKey;
    }

    /**
     * Sets the text area's text key.
     *
     * @param textKey
     * The text key, or <tt>null</tt> to clear the binding.
     */
    public void setTextKey(String textKey) {
        String previousTextKey = this.textKey;

        if (previousTextKey != textKey) {
            this.textKey = textKey;
            textAreaBindingListeners.textKeyChanged(this, previousTextKey);
        }
    }

    public BindType getTextBindType() {
        return textBindType;
    }

    public void setTextBindType(BindType textBindType) {
        if (textBindType == null) {
            throw new IllegalArgumentException();
        }

        BindType previousTextBindType = this.textBindType;

        if (previousTextBindType != textBindType) {
            this.textBindType = textBindType;
            textAreaBindingListeners.textBindTypeChanged(this, previousTextBindType);
        }
    }

    public final void setTextBindType(String textBindType) {
        if (textBindType == null) {
            throw new IllegalArgumentException();
        }

        setTextBindType(BindType.valueOf(textBindType.toUpperCase()));
    }

    public TextBindMapping getTextBindMapping() {
        return textBindMapping;
    }

    public void setTextBindMapping(TextBindMapping textBindMapping) {
        TextBindMapping previousTextBindMapping = this.textBindMapping;

        if (previousTextBindMapping != textBindMapping) {
            this.textBindMapping = textBindMapping;
            textAreaBindingListeners.textBindMappingChanged(this, previousTextBindMapping);
        }
    }

    @Override
    public void load(Object context) {
        if (textKey != null
            && JSON.containsKey(context, textKey)
            && textBindType != BindType.STORE) {
            Object value = JSON.get(context, textKey);

            if (textBindMapping == null) {
                value = (value == null) ? "" : value.toString();
            } else {
                value = textBindMapping.toString(value);
            }

            setText((String)value);
        }
    }

    @Override
    public void store(Object context) {
        if (textKey != null
            && textBindType != BindType.LOAD) {
            String text = getText();
            JSON.put(context, textKey, (textBindMapping == null) ?
                text : textBindMapping.valueOf(text));
        }
    }

    @Override
    public void clear() {
        if (textKey != null) {
            setText("");
        }
    }

    public int getInsertionPoint(int x, int y) {
        TextArea.Skin textAreaSkin = (TextArea.Skin)getSkin();
        return textAreaSkin.getInsertionPoint(x, y);
    }

    public int getNextInsertionPoint(int x, int from, FocusTraversalDirection direction) {
        TextArea.Skin textAreaSkin = (TextArea.Skin)getSkin();
        return textAreaSkin.getNextInsertionPoint(x, from, direction);
    }

    public int getRowIndex(int offset) {
        TextArea.Skin textAreaSkin = (TextArea.Skin)getSkin();
        return textAreaSkin.getRowIndex(offset);
    }

    public int getRowCount() {
        TextArea.Skin textAreaSkin = (TextArea.Skin)getSkin();
        return textAreaSkin.getRowCount();
    }

    public Bounds getCharacterBounds(int offset) {
        TextArea.Skin textAreaSkin = (TextArea.Skin)getSkin();
        return textAreaSkin.getCharacterBounds(offset);
    }

    public ListenerList<TextAreaListener2> getTextAreaListeners() {
        return textAreaListeners;
    }

    public ListenerList<TextAreaContentListener2> getTextAreaContentListeners() {
        return textAreaContentListeners;
    }

    public ListenerList<TextAreaSelectionListener2> getTextAreaSelectionListeners() {
        return textAreaSelectionListeners;
    }

    public ListenerList<TextAreaBindingListener2> getTextAreaBindingListeners() {
        return textAreaBindingListeners;
    }
}
