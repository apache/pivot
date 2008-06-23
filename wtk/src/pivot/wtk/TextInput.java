/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk;

import pivot.collections.Dictionary;
import pivot.util.ListenerList;
import pivot.wtk.skin.terra.TextInputSkin;

/**
 * A component that allows a user to enter a single line of unformatted text.
 *
 * @author gbrown
 */
public class TextInput extends Component {
    /**
     * Text input listener list.
     *
     * @author gbrown
     */
    private class TextInputListenerList extends ListenerList<TextInputListener>
        implements TextInputListener {
        public void textKeyChanged(TextInput textInput, String previousTextKey) {
            for (TextInputListener listener : this) {
                listener.textKeyChanged(textInput, previousTextKey);
            }
        }

        public void textSizeChanged(TextInput textInput, int previousTextSize) {
            for (TextInputListener listener : this) {
                listener.textSizeChanged(textInput, previousTextSize);
            }
        }

        public void maximumLengthChanged(TextInput textInput, int previousMaximumLength) {
            for (TextInputListener listener : this) {
                listener.maximumLengthChanged(textInput, previousMaximumLength);
            }
        }

        public void passwordChanged(TextInput textInput) {
            for (TextInputListener listener : this) {
                listener.passwordChanged(textInput);
            }
        }
    }

    /**
     * Text input text listener list.
     *
     * @author gbrown
     */
    private class TextInputTextListenerList extends ListenerList<TextInputTextListener>
        implements TextInputTextListener {
        public void textChanged(TextInput textInput) {
            for (TextInputTextListener listener : this) {
                listener.textChanged(textInput);
            }
        }
    }

    /**
     * Text input character listener list.
     *
     * @author gbrown
     */
    private class TextInputCharacterListenerList extends ListenerList<TextInputCharacterListener>
        implements TextInputCharacterListener {
        public void charactersInserted(TextInput textInput, int index, int count) {
            for (TextInputCharacterListener listener : this) {
                listener.charactersInserted(textInput, index, count);
            }
        }

        public void charactersRemoved(TextInput textInput, int index, int count) {
            for (TextInputCharacterListener listener : this) {
                listener.charactersRemoved(textInput, index, count);
            }
        }

        public void charactersReset(TextInput textInput) {
            for (TextInputCharacterListener listener : this) {
                listener.charactersReset(textInput);
            }
        }
    }

    /**
     * Text input selection listener list.
     *
     * @author gbrown
     */
    private class TextInputSelectionListenerList extends ListenerList<TextInputSelectionListener>
        implements TextInputSelectionListener {
        public void selectionChanged(TextInput textInput,
            int previousSelectionStart, int previousSelectionEnd) {
            for (TextInputSelectionListener listener : this) {
                listener.selectionChanged(textInput,
                    previousSelectionStart, previousSelectionEnd);
            }
        }
    }

    private StringBuilder textBuilder = new StringBuilder();
    private int selectionStart = 0;
    private int selectionLength = 0;
    private int textSize = DEFAULT_TEXT_SIZE;
    private int maximumLength = Integer.MAX_VALUE;
    private boolean password = false;
    private String textKey = null;

    private TextInputListenerList textInputListeners = new TextInputListenerList();
    private TextInputTextListenerList textInputTextListeners = new TextInputTextListenerList();
    private TextInputCharacterListenerList textInputCharacterListeners = new TextInputCharacterListenerList();
    private TextInputSelectionListenerList textInputSelectionListeners = new TextInputSelectionListenerList();

    private static final int DEFAULT_TEXT_SIZE = 20;

    /**
     * Creates a text input that is initially empty.
     */
    public TextInput() {
        this("");
    }

    /**
     * Creates a text input that is initialized with the given text.
     *
     * @param text
     * The initial text content of the text input.
     */
    public TextInput(String text) {
        if (getClass() == TextInput.class) {
            setSkinClass(TextInputSkin.class);
        }

        setText(text);
    }

    /**
     * Returns the text content of the text input.
     *
     * @return
     * A new string containing a copy of the text input's content.
     */
    public String getText() {
        return textBuilder.toString();
    }

    /**
     * Sets the text content of the text input. Clears any existing selection.
     *
     * param text
     * The new content of the text input.
     */
    public void setText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text is null.");
        }

        if (text.length() > maximumLength) {
            throw new IllegalArgumentException("text length is greater than maximum length.");
        }

        setSelection(0, 0);

        textBuilder = new StringBuilder(text);

        textInputCharacterListeners.charactersReset(this);
        textInputTextListeners.textChanged(this);
    }

    /**
     * Inserts a single character into the text input's content.
     *
     * @param character
     * The character to insert.
     *
     * @param index
     * The index of the insertion point within the existing text. If equal to
     * the current character count, the new text is appended to the existing
     * content.
     */
    public void insertText(char character, int index) {
        insertText(Character.toString(character), index);
    }

    /**
     * Inserts text into the text input's content.
     *
     * @param text
     * The text to insert.
     *
     * @param index
     * The index of the insertion point within the existing text. If equal to
     * the current character count, the new text is appended to the existing
     * content.
     */
    public void insertText(String text, int index) {
        if (index < 0
            || index > textBuilder.length()) {
            throw new IndexOutOfBoundsException();
        }

        if (textBuilder.length() + text.length() > maximumLength) {
            throw new IllegalArgumentException("Insertion of text would exceed maximum length.");
        }

        // Insert the text
        textBuilder.insert(index, text);

        // Update the selection
        int previousSelectionStart = this.selectionStart;
        int previousSelectionLength = this.selectionLength;

        int count = text.length();

        if (selectionStart + selectionLength >= index) {
            if (selectionStart >= index) {
                selectionStart += count;
            } else {
                selectionLength += count;
            }
        }

        // Notify listeners
        textInputCharacterListeners.charactersInserted(this, index, count);
        textInputTextListeners.textChanged(this);

        if (previousSelectionStart != selectionStart
            || previousSelectionLength != selectionLength) {
            textInputSelectionListeners.selectionChanged(this,
                previousSelectionStart, previousSelectionLength);
        }
    }

    /**
     * Removes a range of characters from the text input's content.
     *
     * @param index
     * The index of the first character to remove.
     *
     * @param count
     * The number of characters to remove.
     *
     * @return
     * A string containing the text that was removed.
     */
    public String removeText(int index, int count) {
        if (index < 0
            || index + count > textBuilder.length()) {
            throw new IndexOutOfBoundsException();
        }

        // Determine the range of indexes to remove; the interval is defined
        // as [start, end)
        int start = index;
        int end = index + count;

        String text = textBuilder.substring(start, end);
        textBuilder.delete(start, end);

        // Update selection
        int previousSelectionStart = this.selectionStart;
        int previousSelectionLength = this.selectionLength;

        // The selection interval is defined as [selectionStart, selectionEnd]
        int selectionEnd = selectionStart + selectionLength - 1;

        if (selectionEnd >= start) {
            selectionStart = Math.min(start, selectionStart);
            selectionEnd = Math.max(end - 1, selectionEnd) - count;

            selectionLength = selectionEnd - selectionStart + 1;
        }

        textInputCharacterListeners.charactersRemoved(this, index, count);
        textInputTextListeners.textChanged(this);

        if (previousSelectionStart != selectionStart
            || previousSelectionLength != selectionLength) {
            textInputSelectionListeners.selectionChanged(this,
                previousSelectionStart, previousSelectionLength);
        }

        return text;
    }

    /**
     * Returns the character at the given index.
     *
     * @param index
     * The index of the character to return.
     *
     * @return
     * The character at the given index.
     */
    public char getCharacter(int index) {
        if (index < 0
            || index >= textBuilder.length()) {
            throw new IndexOutOfBoundsException();
        }

        return textBuilder.charAt(index);
    }

    /**
     * Returns the total number of characters in the text input's text
     * content.
     *
     * @return
     * The length of the text input's content.
     */
    public int getCharacterCount() {
        return textBuilder.length();
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
     * Sets the selection. The sum of the selection start and length must be
     * less than the length of the text input's content.
     *
     * @param selectionStart
     * The starting index of the selection.
     *
     * @param selectionLength
     * The length of the selection.
     */
    public void setSelection(int selectionStart, int selectionLength) {
        if (selectionStart < 0
            || selectionStart + selectionLength > textBuilder.length()) {
            throw new IndexOutOfBoundsException();
        }

        int previousSelectionStart = this.selectionStart;
        int previousSelectionLength = this.selectionLength;

        if (previousSelectionStart != selectionStart
            || previousSelectionLength != selectionLength) {
            this.selectionStart = selectionStart;
            this.selectionLength = selectionLength;

            textInputSelectionListeners.selectionChanged(this,
                previousSelectionStart, previousSelectionLength);
        }
    }

    /**
     * Returns a span representing the current selection.
     *
     * @return
     * A span containing the current selection. Both start and end points are
     * inclusive. Returns <tt>null</tt> if the selection is empty.
     */
    public Span getSelectionRange() {
        return (selectionLength == 0) ? null : new Span(selectionStart,
            selectionStart + selectionLength - 1);
    }

    /**
     * Returns the currently selected text.
     *
     * @return
     * A new string containing a copy of the text in the selected range, or
     * <tt>null</tt> if nothing is selected.
     */
    public String getSelectedText() {
        String selectedText = null;

        if (selectionLength > 0) {
            selectedText = textBuilder.substring(selectionStart,
                selectionStart + selectionLength);
        }

        return selectedText;
    }

    /**
     * Returns the text size.
     *
     * @return
     * The number of characters to display in the text input.
     */
    public int getTextSize() {
        return textSize;
    }

    /**
     * Sets the text size.
     *
     * @param textSize
     * The number of characters to display in the text input.
     */
    public void setTextSize(int textSize) {
        if (textSize < 0) {
            throw new IllegalArgumentException("textSize is negative.");
        }

        int previousTextSize = this.textSize;

        if (previousTextSize != textSize) {
            this.textSize = textSize;
            textInputListeners.textSizeChanged(this, previousTextSize);
        }
    }

    /**
     * Returns the maximum length of the text input's text content.
     *
     * @return
     * The maximum length of the text input's text content.
     */
    public int getMaximumLength() {
        return maximumLength;
    }

    /**
     * Sets the maximum length of the text input's text content.
     *
     * @param maximumLength
     * The maximum length of the text input's text content.
     */
    public void setMaximumLength(int maximumLength) {
        if (maximumLength < 0) {
            throw new IllegalArgumentException("maximumLength is negative.");
        }

        int previousMaximumLength = this.maximumLength;

        if (previousMaximumLength != maximumLength) {
            // Truncate the text, if necessary
            if (textBuilder.length() > maximumLength) {
                removeText(maximumLength, textBuilder.length() - maximumLength);
            }

            this.maximumLength = maximumLength;
            textInputListeners.maximumLengthChanged(this, previousMaximumLength);
        }
    }

    /**
     * Returns the password flag.
     *
     * @return
     * <tt>true</tt> if this is a password text input; <tt>false</tt>,
     * otherwise.
     */
    public boolean isPassword() {
        return password;
    }

    /**
     * Sets or clears the password flag. If the password flag is set, the text
     * input will visually mask its contents.
     *
     * @param password
     * <tt>true</tt> if this is a password text input; <tt>false</tt>,
     * otherwise.
     */
    public void setPassword(boolean password) {
        if (this.password != password) {
            this.password = password;
            textInputListeners.passwordChanged(this);
        }
    }

    /**
     * Returns the text input's text key.
     *
     * @return
     * The text key, or <tt>null</tt> if no text key is set.
     */
    public String getTextKey() {
        return textKey;
    }

    /**
     * Sets the text input's text key.
     *
     * @param textKey
     * The text key, or <tt>null</tt> to clear the binding.
     */
    public void setTextKey(String textKey) {
        String previousTextKey = this.textKey;

        if ((previousTextKey != null
            && textKey != null
            && !previousTextKey.equals(textKey))
            || previousTextKey != textKey) {
            this.textKey = textKey;
            textInputListeners.textKeyChanged(this, previousTextKey);
        }
    }

    @Override
    public void load(Dictionary<String, Object> context) {
        if (textKey != null
            && context.containsKey(textKey)) {
            Object value = context.get(textKey);
            if (value != null) {
                setText(value.toString());
            }
        }
    }

    @Override
    public void store(Dictionary<String, Object> context) {
        if (textKey != null) {
            context.put(textKey, getText());
        }
    }

    /**
     * Returns the text input listener list.
     *
     * @return
     * The text input listener list.
     */
    public ListenerList<TextInputListener> getTextInputListeners() {
        return textInputListeners;
    }

    /**
     * Returns the text input text listener list.
     *
     * @return
     * The text input text listener list.
     */
    public ListenerList<TextInputTextListener> getTextInputTextListeners() {
        return textInputTextListeners;
    }

    /**
     * Returns the text input selection listener list.
     *
     * @return
     * The text input selection listener list.
     */
    public ListenerList<TextInputSelectionListener> getTextInputSelectionListeners() {
        return textInputSelectionListeners;
    }

    /**
     * Returns the text input character listener list.
     *
     * @return
     * The text input character listener list.
     */
    public ListenerList<TextInputCharacterListener> getTextInputCharacterListeners() {
        return textInputCharacterListeners;
    }
}
