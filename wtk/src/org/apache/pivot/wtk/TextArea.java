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
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.LinkedList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSON;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;

/**
 * A component that allows a user to enter multiple lines of unformatted text.
 */
public class TextArea extends Component {
    /**
     * Class representing a paragraph of text.
     */
    public static final class Paragraph {
        private static class ParagraphListenerList extends ListenerList<ParagraphListener>
            implements ParagraphListener {
            @Override
            public void textInserted(Paragraph paragraph, int index, int count) {
                for (ParagraphListener listener : this) {
                    listener.textInserted(paragraph, index, count);
                }
            }

            @Override
            public void textRemoved(Paragraph paragraph, int index, int count) {
                for (ParagraphListener listener : this) {
                    listener.textRemoved(paragraph, index, count);
                }
            }
        }

        private StringBuilder characters = new StringBuilder(INITIAL_PARAGRAPH_CAPACITY);
        private TextArea textArea = null;
        private int offset = -1;

        private ParagraphListenerList paragraphListeners = new ParagraphListenerList();

        public CharSequence getCharacters() {
            return characters;
        }

        public TextArea getTextArea() {
            return textArea;
        }

        public void append(char character) {
            if (textArea != null) {
                throw new IllegalStateException();
            }

            characters.append(character);
        }

        public void clear() {
            if (textArea != null) {
                throw new IllegalStateException();
            }

            characters.delete(0, characters.length());
        }

        public void insertText(CharSequence text, int index) {
            if (text == null) {
                throw new IllegalArgumentException();
            }

            indexBoundsCheck("index", index, 0, characters.length());

            int count = text.length();

            if (textArea != null
                && textArea.characterCount + count > textArea.maximumLength) {
                throw new IllegalArgumentException("Insertion of text would exceed maximum length.");
            }

            characters.insert(index, text);

            if (textArea != null) {
                // Update offsets and character count
                textArea.updateParagraphOffsets(textArea.paragraphs.indexOf(this) + 1, count);
                textArea.characterCount += count;

                // Update selection state
                int previousSelectionStart = textArea.selectionStart;
                int previousSelectionLength = textArea.selectionLength;
                textArea.selectionStart = offset + index + count;
                textArea.selectionLength = 0;

                // Fire change events
                paragraphListeners.textInserted(this, index, count);
                textArea.textAreaContentListeners.textChanged(textArea);

                if (textArea.selectionStart != previousSelectionStart
                    || textArea.selectionLength != previousSelectionLength) {
                    textArea.textAreaSelectionListeners.selectionChanged(textArea,
                        textArea.selectionStart, textArea.selectionLength);
                }
            }
        }

        public void removeText(int index) {
            removeText(index, characters.length() - index);
        }

        public void removeText(int index, int count) {
            if (index < 0
                || index + count > characters.length()) {
                throw new IndexOutOfBoundsException();
            }

            characters.delete(index, index + count);

            if (textArea != null) {
                // Update offsets and character count
                textArea.updateParagraphOffsets(textArea.paragraphs.indexOf(this) + 1, -count);
                textArea.characterCount -= count;

                // Update selection state
                int previousSelectionStart = textArea.selectionStart;
                int previousSelectionLength = textArea.selectionLength;
                textArea.selectionStart = offset + index;
                textArea.selectionLength = 0;

                // Fire change events
                paragraphListeners.textRemoved(this, index, count);
                textArea.textAreaContentListeners.textChanged(textArea);

                if (textArea.selectionStart != previousSelectionStart
                    || textArea.selectionLength != previousSelectionLength) {
                    textArea.textAreaSelectionListeners.selectionChanged(textArea,
                        textArea.selectionStart, textArea.selectionLength);
                }
            }
        }

        public int getOffset() {
            return offset;
        }

        public ListenerList<ParagraphListener> getParagraphListeners() {
            return paragraphListeners;
        }
    }

    /**
     * Paragraph listener interface.
     */
    public interface ParagraphListener {
        /**
         * Paragraph listener interface adapter.
         */
        public static class Adapter implements ParagraphListener {
            @Override
            public void textInserted(Paragraph paragraph, int index, int count) {
                // empty block
            }

            @Override
            public void textRemoved(Paragraph paragraph, int index, int count) {
                // empty block
            }
        }

        /**
         * Called when text has been inserted into a paragraph.
         *
         * @param paragraph
         * The source of the event.
         *
         * @param index
         * The index at which the text was inserted.
         *
         * @param count
         * The number of characters that were inserted.
         */
        public void textInserted(Paragraph paragraph, int index, int count);

        /**
         * Called when characters have been removed from a paragraph.
         *
         * @param paragraph
         * The source of the event.
         *
         * @param index
         * The index from which the text was removed.
         *
         * @param count
         * The number of characters that were removed.
         */
        public void textRemoved(Paragraph paragraph, int index, int count);
    }

    /**
     * Enum representing a scroll direction.
     */
    public enum ScrollDirection {
        UP,
        DOWN
    }

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
         */
        public int getInsertionPoint(int x, int y);

        /**
         * Returns the next insertion point given an x coordinate and a
         * character index.
         *
         * @param x
         * @param from
         * @param direction
         */
        public int getNextInsertionPoint(int x, int from, ScrollDirection direction);

        /**
         * Returns the row index of the character at a given index.
         *
         * @param index
         */
        public int getRowAt(int index);

        /**
         * Returns the index of the first character in the row containing
         * a given character index.
         *
         * @param index
         */
        public int getRowOffset(int index);

        /**
         * Returns the number of characters in the row containing a given
         * character index.
         *
         * @param index
         */
        public int getRowLength(int index);

        /**
         * Returns the total number of rows in the text area.
         */
        public int getRowCount();

        /**
         * Returns the bounds of the character at a given index.
         *
         * @param index
         */
        public Bounds getCharacterBounds(int index);
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

    /**
     * Text area paragraph sequence.
     */
    public final class ParagraphSequence implements Sequence<Paragraph>, Iterable<Paragraph> {
        @Override
        public int add(Paragraph paragraph) {
            int index = getLength();
            insert(paragraph, index);

            return index;
        }

        @Override
        public void insert(Paragraph paragraph, int index) {
            if (paragraph == null) {
                throw new IllegalArgumentException("paragraph is null.");
            }

            if (paragraph.textArea != null) {
                throw new IllegalArgumentException("paragraph is already in use by another text area.");
            }

            // Determine insertion count, including terminator character
            int characterCountLocal = paragraph.characters.length();

            if (getLength() > 0) {
                characterCountLocal++;
            }

            if (TextArea.this.characterCount + characterCountLocal > maximumLength) {
                throw new IllegalArgumentException("Insertion of text would exceed maximum length.");
            }

            // Set the paragraph offset
            if (index == paragraphs.getLength()) {
                paragraph.offset = TextArea.this.characterCount;

                // Include terminator character
                if (index > 0) {
                    paragraph.offset++;
                }
            } else {
                paragraph.offset = paragraphs.get(index).offset;
            }

            // Insert the paragraph
            paragraphs.insert(paragraph, index);
            paragraph.textArea = TextArea.this;

            // Update offsets and character count
            updateParagraphOffsets(index + 1, characterCountLocal);
            TextArea.this.characterCount += characterCountLocal;

            // Update selection state
            int previousSelectionStart = selectionStart;
            int previousSelectionLength = selectionLength;
            selectionStart = paragraph.offset + paragraph.characters.length();
            selectionLength = 0;

            // Fire change events
            textAreaContentListeners.paragraphInserted(TextArea.this, index);
            textAreaContentListeners.textChanged(TextArea.this);

            if (selectionStart != previousSelectionStart
                || selectionLength != previousSelectionLength) {
                textAreaSelectionListeners.selectionChanged(TextArea.this,
                    selectionStart, selectionLength);
            }
        }

        @Override
        public Paragraph update(int index, Paragraph paragraph) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(Paragraph paragraph){
            int index = indexOf(paragraph);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<Paragraph> remove(int index, int count) {
            Sequence<Paragraph> removed = paragraphs.remove(index, count);

            if (count > 0) {
                int characterCountLocal = 0;
                for (int i = 0, n = removed.getLength(); i < n; i++) {
                    Paragraph paragraph = removed.get(i);
                    paragraph.textArea = null;
                    paragraph.offset = -1;
                    characterCountLocal += paragraph.characters.length() + 1;
                }

                // Don't include the implicit final terminator in the character count
                if (getLength() == 0) {
                    characterCountLocal--;
                }

                // Update offsets
                updateParagraphOffsets(index, -characterCountLocal);
                TextArea.this.characterCount -= characterCountLocal;

                // Update selection state
                int previousSelectionStart = selectionStart;
                int previousSelectionLength = selectionLength;
                selectionStart = (index == paragraphs.getLength()) ?
                    TextArea.this.characterCount : paragraphs.get(index).offset;
                selectionLength = 0;

                // Fire change events
                textAreaContentListeners.paragraphsRemoved(TextArea.this, index, removed);
                textAreaContentListeners.textChanged(TextArea.this);

                if (selectionStart != previousSelectionStart
                    || selectionLength != previousSelectionLength) {
                    textAreaSelectionListeners.selectionChanged(TextArea.this,
                        selectionStart, selectionLength);
                }
            }

            return removed;
        }

        @Override
        public Paragraph get(int index) {
            return paragraphs.get(index);
        }

        @Override
        public int indexOf(Paragraph paragraph) {
            return paragraphs.indexOf(paragraph);
        }

        @Override
        public int getLength() {
            return paragraphs.getLength();
        }

        @Override
        public Iterator<Paragraph> iterator() {
            return new ImmutableIterator<Paragraph>(paragraphs.iterator());
        }
    }

    private interface Edit {
        public void undo();
    }

    private class InsertTextEdit implements Edit {
        private final int index;
        private final int count;

        public InsertTextEdit(CharSequence text, int index) {
            this.index = index;
            count = text.length();
        }

        @Override
        public void undo() {
            removeText(index, count, false);
        }
    }

    private class RemoveTextEdit implements Edit {
        private final int index;
        private final CharSequence text;

        public RemoveTextEdit(int index, int count) {
            this.index = index;
            text = getText(index, index + count);
        }

        @Override
        public void undo() {
            insertText(text, index, false);
        }
    }

    private static class TextAreaListenerList extends WTKListenerList<TextAreaListener>
        implements TextAreaListener {
        @Override
        public void maximumLengthChanged(TextArea textArea, int previousMaximumLength) {
            for (TextAreaListener listener : this) {
                listener.maximumLengthChanged(textArea, previousMaximumLength);
            }
        }

        @Override
        public void editableChanged(TextArea textArea) {
            for (TextAreaListener listener : this) {
                listener.editableChanged(textArea);
            }
        }
    }

    private static class TextAreaContentListenerList extends WTKListenerList<TextAreaContentListener>
        implements TextAreaContentListener {
        @Override
        public void paragraphInserted(TextArea textArea, int index) {
            for (TextAreaContentListener listener : this) {
                listener.paragraphInserted(textArea, index);
            }
        }

        @Override
        public void paragraphsRemoved(TextArea textArea, int index, Sequence<TextArea.Paragraph> removed) {
            for (TextAreaContentListener listener : this) {
                listener.paragraphsRemoved(textArea, index, removed);
            }
        }

        @Override
        public void textChanged(TextArea textArea) {
            for (TextAreaContentListener listener : this) {
                listener.textChanged(textArea);
            }
        }
    }

    private static class TextAreaSelectionListenerList extends WTKListenerList<TextAreaSelectionListener>
        implements TextAreaSelectionListener {
        @Override
        public void selectionChanged(TextArea textArea, int previousSelectionStart,
            int previousSelectionLength) {
            for (TextAreaSelectionListener listener : this) {
                listener.selectionChanged(textArea, previousSelectionStart,
                    previousSelectionLength);
            }
        }
    }

    private static class TextAreaBindingListenerList extends WTKListenerList<TextAreaBindingListener>
        implements TextAreaBindingListener {
        @Override
        public void textKeyChanged(TextArea textArea, String previousTextKey) {
            for (TextAreaBindingListener listener : this) {
                listener.textKeyChanged(textArea, previousTextKey);
            }
        }

        @Override
        public void textBindTypeChanged(TextArea textArea, BindType previousTextBindType) {
            for (TextAreaBindingListener listener : this) {
                listener.textBindTypeChanged(textArea, previousTextBindType);
            }
        }

        @Override
        public void textBindMappingChanged(TextArea textArea, TextBindMapping previousTextBindMapping) {
            for (TextAreaBindingListener listener : this) {
                listener.textBindMappingChanged(textArea, previousTextBindMapping);
            }
        }
    }

    private ArrayList<Paragraph> paragraphs = new ArrayList<Paragraph>();
    private ParagraphSequence paragraphSequence = new ParagraphSequence();

    private int characterCount = 0;

    private int selectionStart = 0;
    private int selectionLength = 0;

    private int maximumLength = Integer.MAX_VALUE;
    private boolean editable = true;

    private String textKey = null;
    private BindType textBindType = BindType.BOTH;
    private TextBindMapping textBindMapping = null;

    private LinkedList<Edit> editHistory = new LinkedList<Edit>();

    private TextAreaListenerList textAreaListeners = new TextAreaListenerList();
    private TextAreaContentListenerList textAreaContentListeners = new TextAreaContentListenerList();
    private TextAreaSelectionListenerList textAreaSelectionListeners = new TextAreaSelectionListenerList();
    private TextAreaBindingListenerList textAreaBindingListeners = new TextAreaBindingListenerList();

    private static final int INITIAL_PARAGRAPH_CAPACITY = 256;
    private static final int MAXIMUM_EDIT_HISTORY_LENGTH = 30;

    public TextArea() {
        installSkin(TextArea.class);
        setText("");
    }

    @Override
    protected void setSkin(org.apache.pivot.wtk.Skin skin) {
        if (!(skin instanceof TextArea.Skin)) {
            throw new IllegalArgumentException("Skin class must implement "
                + TextArea.Skin.class.getName());
        }

        super.setSkin(skin);
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
     * Returns a portion of the text content of the text area.
     *
     * @param beginIndex
     * @param endIndex
     *
     * @return
     * A string containing a copy of the text area's text content.
     */
    public String getText(int beginIndex, int endIndex) {
        if (beginIndex > endIndex) {
            throw new IllegalArgumentException();
        }

        if (beginIndex < 0
            || endIndex > characterCount) {
            throw new IndexOutOfBoundsException();
        }

        int count = endIndex - beginIndex;
        if (count == 0) {
            return "";
        }
        StringBuilder textBuilder = new StringBuilder(count);

        // Get paragraph and character offset at beginIndex
        int paragraphIndex = getParagraphAt(beginIndex);
        Paragraph paragraph = paragraphs.get(paragraphIndex);

        int characterOffset = beginIndex - paragraph.offset;

        // Read characters until endIndex is reached, appending to text builder
        // and moving to next paragraph as needed
        int i = 0;
        while (i < count) {
            if (characterOffset == paragraph.characters.length()
                && i < characterCount) {
                textBuilder.append('\n');
                paragraph = paragraphs.get(++paragraphIndex);
                characterOffset = 0;
            } else {
                textBuilder.append(paragraph.characters.charAt(characterOffset++));
            }

            i++;
        }

        return textBuilder.toString();
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
        ArrayList<Paragraph> paragraphsLocal = new ArrayList<Paragraph>();
        int characterCountLocal = 0;

        Paragraph paragraph = new Paragraph();

        int c = textReader.read();
        while (c != -1) {
            if (++characterCountLocal > maximumLength) {
                throw new IllegalArgumentException("Text length is greater than maximum length.");
            }

            if (c == '\n') {
                paragraphsLocal.add(paragraph);
                paragraph = new Paragraph();
            } else {
                paragraph.append((char)c);
            }

            c = textReader.read();
        }

        paragraphsLocal.add(paragraph);

        // Clear the edit history
        editHistory.clear();

        // Update content
        paragraphSequence.remove(0, paragraphSequence.getLength());

        for (int i = 0, n = paragraphsLocal.getLength(); i < n; i++) {
            paragraphSequence.add(paragraphsLocal.get(i));
        }
    }

    public void insertText(CharSequence text, int index) {
        insertText(text, index, true);
    }

    private void insertText(CharSequence text, int index, boolean addToEditHistory) {
        if (text == null) {
            throw new IllegalArgumentException();
        }

        indexBoundsCheck("index", index, 0, characterCount);

        if (text.length() > 0) {
            // Insert the text
            int paragraphIndex = getParagraphAt(index);
            Paragraph paragraph = paragraphs.get(paragraphIndex);

            int characterOffset = index - paragraph.offset;

            StringBuilder textBuilder = new StringBuilder();

            for (int i = 0, n = text.length(); i < n; i++) {
                char c = text.charAt(i);

                if (c == '\n') {
                    // Split paragraph at current offset
                    int count = paragraph.characters.length();

                    CharSequence trailingCharacters = paragraph.characters.subSequence(characterOffset, count);
                    paragraph.removeText(characterOffset, count - characterOffset);
                    paragraph.insertText(textBuilder, characterOffset);

                    paragraph = new Paragraph();
                    paragraph.insertText(trailingCharacters, 0);
                    paragraphSequence.insert(paragraph, ++paragraphIndex);
                    characterOffset = 0;

                    textBuilder = new StringBuilder();
                } else {
                    // Append character
                    textBuilder.append(c);
                }
            }

            paragraph.insertText(textBuilder, characterOffset);

            // Add an insert history item
            if (addToEditHistory) {
                addHistoryItem(new InsertTextEdit(text, index));
            }
        }
    }

    public void removeText(int index, int count) {
        removeText(index, count, true);
    }

    private void removeText(int index, int count, boolean addToEditHistory) {
        if (index < 0
            || index + count > characterCount) {
            throw new IndexOutOfBoundsException();
        }

        if (count > 0) {
            // Add a remove history item
            if (addToEditHistory) {
                addHistoryItem(new RemoveTextEdit(index, count));
            }

            // Identify the leading and trailing paragraph indexes
            int endParagraphIndex = getParagraphAt(index + count);
            Paragraph endParagraph = paragraphs.get(endParagraphIndex);

            int beginParagraphIndex = endParagraphIndex;
            Paragraph beginParagraph = endParagraph;

            while (beginParagraph.offset > index) {
                beginParagraph = paragraphs.get(--beginParagraphIndex);
            }

            if (beginParagraphIndex == endParagraphIndex) {
                // The removal affects only a single paragraph
                beginParagraph.removeText(index - beginParagraph.offset, count);
            } else {
                // The removal spans paragraphs; remove any intervening paragraphs and
                // merge the leading and trailing segments
                String leadingText = beginParagraph.characters.substring(0, index - beginParagraph.offset);
                endParagraph.removeText(0, (index + count) - endParagraph.offset);
                paragraphSequence.remove(beginParagraphIndex, endParagraphIndex - beginParagraphIndex);
                endParagraph.insertText(leadingText, 0);
            }
        }
    }

    private void updateParagraphOffsets(int from, int count) {
        if (count != 0) {
            for (int i = from, n = paragraphs.getLength(); i < n; i++) {
                Paragraph paragraph = paragraphs.get(i);
                paragraph.offset += count;
            }
        }
    }

    /**
     * Returns the text area's paragraph sequence.
     */
    public ParagraphSequence getParagraphs() {
        return paragraphSequence;
    }

    /**
     * Returns the index of the paragraph containing a given character index.
     *
     * @param index
     */
    public int getParagraphAt(int index) {
        indexBoundsCheck("index", index, 0, characterCount);

        int paragraphIndex = paragraphs.getLength() - 1;
        Paragraph paragraph = paragraphs.get(paragraphIndex);

        while (paragraph.offset > index) {
            paragraph = paragraphs.get(--paragraphIndex);
        }

        return paragraphIndex;
    }

    /**
     * Returns the character at a given index.
     *
     * @param index
     */
    public char getCharacterAt(int index) {
        indexBoundsCheck("index", index, 0, characterCount - 1);

        int paragraphIndex = getParagraphAt(index);
        Paragraph paragraph = paragraphs.get(paragraphIndex);

        int characterOffset = index - paragraph.offset;

        return (characterOffset == paragraph.characters.length()) ?
            '\n' : paragraph.characters.charAt(characterOffset);
    }

    /**
     * Returns the number of characters in the text area, including line break
     * characters.
     */
    public int getCharacterCount() {
        return characterCount;
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
                if ((characterCount + text.length()) > maximumLength) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    removeText(selectionStart, selectionLength);
                    insertText(text, selectionStart);
                }
            }
        }
    }

    public void undo() {
        int n = editHistory.getLength();
        if (n > 0) {
            Edit edit = editHistory.remove(n - 1, 1).get(0);
            edit.undo();
        }
    }

    private void addHistoryItem(Edit edit) {
        editHistory.add(edit);

        if (editHistory.getLength() > MAXIMUM_EDIT_HISTORY_LENGTH) {
            editHistory.remove(0, 1);
        }
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
            || selectionStart + selectionLength > characterCount) {
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
        setSelection(0, characterCount);
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
            this.maximumLength = maximumLength;

            // Truncate the text, if necessary
            if (characterCount > maximumLength) {
                removeText(maximumLength, characterCount - maximumLength);
            }

            // Fire change events
            textAreaListeners.maximumLengthChanged(this, previousMaximumLength);
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

    public int getNextInsertionPoint(int x, int from, ScrollDirection direction) {
        TextArea.Skin textAreaSkin = (TextArea.Skin)getSkin();
        return textAreaSkin.getNextInsertionPoint(x, from, direction);
    }

    public int getRowAt(int index) {
        TextArea.Skin textAreaSkin = (TextArea.Skin)getSkin();
        return textAreaSkin.getRowAt(index);
    }

    public int getRowOffset(int index) {
        TextArea.Skin textAreaSkin = (TextArea.Skin)getSkin();
        return textAreaSkin.getRowOffset(index);
    }

    public int getRowLength(int index) {
        TextArea.Skin textAreaSkin = (TextArea.Skin)getSkin();
        return textAreaSkin.getRowLength(index);
    }

    public int getRowCount() {
        TextArea.Skin textAreaSkin = (TextArea.Skin)getSkin();
        return textAreaSkin.getRowCount();
    }

    public Bounds getCharacterBounds(int index) {
        // We need to validate in case we get called from user-code after
        // a user-code initiated modification, but before another layout has run.
        validate();
        TextArea.Skin textAreaSkin = (TextArea.Skin)getSkin();
        return textAreaSkin.getCharacterBounds(index);
    }

    public ListenerList<TextAreaListener> getTextAreaListeners() {
        return textAreaListeners;
    }

    public ListenerList<TextAreaContentListener> getTextAreaContentListeners() {
        return textAreaContentListeners;
    }

    public ListenerList<TextAreaSelectionListener> getTextAreaSelectionListeners() {
        return textAreaSelectionListeners;
    }

    public ListenerList<TextAreaBindingListener> getTextAreaBindingListeners() {
        return textAreaBindingListeners;
    }
}
