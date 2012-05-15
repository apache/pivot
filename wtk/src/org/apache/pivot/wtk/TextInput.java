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

import org.apache.pivot.collections.LinkedList;
import org.apache.pivot.json.JSON;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.validation.Validator;

/**
 * A component that allows a user to enter a single line of unformatted text.
 */
public class TextInput extends Component {
    /**
     * Text input skin interface. Text input skins are required to implement
     * this.
     */
    public interface Skin {
        /**
         * Returns the insertion point for a given location.
         *
         * @param x
         */
        public int getInsertionPoint(int x);

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
        private final String text;

        public RemoveTextEdit(int index, int count) {
            this.index = index;
            text = getText(index, index + count);
        }

        @Override
        public void undo() {
            insertText(text, index, false);
        }
    }

    private static class TextInputListenerList extends WTKListenerList<TextInputListener>
        implements TextInputListener {
        @Override
        public void textSizeChanged(TextInput textInput, int previousTextSize) {
            for (TextInputListener listener : this) {
                listener.textSizeChanged(textInput, previousTextSize);
            }
        }

        @Override
        public void maximumLengthChanged(TextInput textInput, int previousMaximumLength) {
            for (TextInputListener listener : this) {
                listener.maximumLengthChanged(textInput, previousMaximumLength);
            }
        }

        @Override
        public void passwordChanged(TextInput textInput) {
            for (TextInputListener listener : this) {
                listener.passwordChanged(textInput);
            }
        }

        @Override
        public void promptChanged(TextInput textInput, String previousPrompt) {
            for (TextInputListener listener : this) {
                listener.promptChanged(textInput, previousPrompt);
            }
        }

        @Override
        public void textValidatorChanged(TextInput textInput, Validator previousValidator) {
            for (TextInputListener listener : this) {
                listener.textValidatorChanged(textInput, previousValidator);
            }
        }

        @Override
        public void strictValidationChanged(TextInput textInput) {
            for (TextInputListener listener : this) {
                listener.strictValidationChanged(textInput);
            }
        }

        @Override
        public void textValidChanged(TextInput textInput) {
            for (TextInputListener listener : this) {
                listener.textValidChanged(textInput);
            }
        }

        @Override
        public void editableChanged(TextInput textInput) {
            for (TextInputListener listener : this) {
                listener.editableChanged(textInput);
            }
        }
    }

    private static class TextInputContentListenerList extends WTKListenerList<TextInputContentListener>
        implements TextInputContentListener {
        @Override
        public Vote previewInsertText(TextInput textInput, CharSequence text, int index) {
            Vote vote = Vote.APPROVE;

            for (TextInputContentListener listener : this) {
                vote = vote.tally(listener.previewInsertText(textInput, text, index));
            }

            return vote;
        }

        @Override
        public void insertTextVetoed(TextInput textInput, Vote reason) {
            for (TextInputContentListener listener : this) {
                listener.insertTextVetoed(textInput, reason);
            }
        }

        @Override
        public void textInserted(TextInput textInput, int index, int count) {
            for (TextInputContentListener listener : this) {
                listener.textInserted(textInput, index, count);
            }
        }

        @Override
        public Vote previewRemoveText(TextInput textInput, int index, int count) {
            Vote vote = Vote.APPROVE;

            for (TextInputContentListener listener : this) {
                vote = vote.tally(listener.previewRemoveText(textInput, index, count));
            }

            return vote;
        }

        @Override
        public void removeTextVetoed(TextInput textInput, Vote reason) {
            for (TextInputContentListener listener : this) {
                listener.removeTextVetoed(textInput, reason);
            }
        }

        @Override
        public void textRemoved(TextInput textInput, int index, int count) {
            for (TextInputContentListener listener : this) {
                listener.textRemoved(textInput, index, count);
            }
        }

        @Override
        public void textChanged(TextInput textInput) {
            for (TextInputContentListener listener : this) {
                listener.textChanged(textInput);
            }
        }
    }

    private static class TextInputSelectionListenerList extends WTKListenerList<TextInputSelectionListener>
        implements TextInputSelectionListener {
        @Override
        public void selectionChanged(TextInput textInput, int previousSelectionStart,
            int previousSelectionLength) {
            for (TextInputSelectionListener listener : this) {
                listener.selectionChanged(textInput,
                    previousSelectionStart, previousSelectionLength);
            }
        }
    }

    private static class TextInputBindingListenerList extends WTKListenerList<TextInputBindingListener>
        implements TextInputBindingListener {
        @Override
        public void textKeyChanged(TextInput textInput, String previousTextKey) {
            for (TextInputBindingListener listener : this) {
                listener.textKeyChanged(textInput, previousTextKey);
            }
        }

        @Override
        public void textBindTypeChanged(TextInput textInput, BindType previousTextBindType) {
            for (TextInputBindingListener listener : this) {
                listener.textBindTypeChanged(textInput, previousTextBindType);
            }
        }

        @Override
        public void textBindMappingChanged(TextInput textInput, TextBindMapping previousTextBindMapping) {
            for (TextInputBindingListener listener : this) {
                listener.textBindMappingChanged(textInput, previousTextBindMapping);
            }
        }
    }

    private StringBuilder characters = new StringBuilder();

    private int selectionStart = 0;
    private int selectionLength = 0;

    private int textSize = DEFAULT_TEXT_SIZE;
    private int maximumLength = Integer.MAX_VALUE;
    private boolean password = false;
    private String prompt = null;
    private boolean editable = true;

    private String textKey = null;
    private BindType textBindType = BindType.BOTH;
    private TextBindMapping textBindMapping = null;

    private Validator validator = null;
    private boolean strictValidation = false;
    private boolean textValid = true;

    private LinkedList<Edit> editHistory = new LinkedList<Edit>();

    private TextInputListenerList textInputListeners = new TextInputListenerList();
    private TextInputContentListenerList textInputContentListeners = new TextInputContentListenerList();
    private TextInputSelectionListenerList textInputSelectionListeners = new TextInputSelectionListenerList();
    private TextInputBindingListenerList textInputBindingListeners = new TextInputBindingListenerList();

    public static final int DEFAULT_TEXT_SIZE = 16;

    private static final int MAXIMUM_EDIT_HISTORY_LENGTH = 30;

    public TextInput() {
        installSkin(TextInput.class);
    }

    @Override
    protected void setSkin(org.apache.pivot.wtk.Skin skin) {
        if (!(skin instanceof TextInput.Skin)) {
            throw new IllegalArgumentException("Skin class must implement "
                + TextInput.Skin.class.getName());
        }

        super.setSkin(skin);
    }

    /**
     * Returns the text content of the text input.
     *
     * @return
     * A string containing a copy of the text input's text content.
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
        return characters.substring(beginIndex, endIndex);
    }

    public void setText(String text) {
        if (text == null) {
            throw new IllegalArgumentException();
        }

        if (text.length() > maximumLength) {
            throw new IllegalArgumentException("Text length is greater than maximum length.");
        }

        characters = new StringBuilder(text);

        // Update selection
        int previousSelectionStart = selectionStart;
        int previousSelectionLength = selectionLength;
        selectionStart = text.length();
        selectionLength = 0;

        // Update the valid flag
        boolean previousTextValid = textValid;
        textValid = (validator == null) ? true : validator.isValid(text);

        // Clear the edit history
        editHistory.clear();

        // Fire change events
        textInputContentListeners.textChanged(this);

        if (textValid != previousTextValid) {
            textInputListeners.textValidChanged(this);
        }

        if (selectionStart != previousSelectionStart
            || selectionLength != previousSelectionLength) {
            textInputSelectionListeners.selectionChanged(this, selectionStart, selectionLength);
        }
    }

    public void insertText(CharSequence text, int index) {
        insertText(text, index, true);
    }

    private void insertText(CharSequence text, int index, boolean addToEditHistory) {
        if (text == null) {
            throw new IllegalArgumentException();
        }

        if (characters.length() + text.length() > maximumLength) {
            throw new IllegalArgumentException("Insertion of text would exceed maximum length.");
        }

        if (text.length() > 0) {
            Vote vote = textInputContentListeners.previewInsertText(this, text, index);

            if (vote == Vote.APPROVE) {
                // Insert the text
                characters.insert(index, text);

                // Add an insert history item
                if (addToEditHistory) {
                    addHistoryItem(new InsertTextEdit(text, index));
                }

                // Update selection
                int previousSelectionStart = selectionStart;
                int previousSelectionLength = selectionLength;
                selectionStart = index + text.length();
                selectionLength = 0;

                // Update the valid flag
                boolean previousTextValid = textValid;
                textValid = (validator == null) ? true : validator.isValid(getText());

                // Fire change events
                textInputContentListeners.textInserted(this, index, text.length());
                textInputContentListeners.textChanged(this);

                if (textValid != previousTextValid) {
                    textInputListeners.textValidChanged(this);
                }

                if (selectionStart != previousSelectionStart
                    || selectionLength != previousSelectionLength) {
                    textInputSelectionListeners.selectionChanged(this, selectionStart, selectionLength);
                }
            } else {
                textInputContentListeners.insertTextVetoed(this, vote);
            }
        }
    }

    public void removeText(int index, int count) {
        removeText(index, count, true);
    }

    private void removeText(int index, int count, boolean addToEditHistory) {
        if (count > 0) {
            Vote vote = textInputContentListeners.previewRemoveText(this, index, count);

            if (vote == Vote.APPROVE) {
                // Add a remove history item
                if (addToEditHistory) {
                    addHistoryItem(new RemoveTextEdit(index, count));
                }

                // Remove the text
                characters.delete(index, index + count);

                // Update the selection
                int previousSelectionStart = selectionStart;
                int previousSelectionLength = selectionLength;
                selectionStart = index;
                selectionLength = 0;

                // Update the valid flag
                boolean previousTextValid = textValid;
                textValid = (validator == null) ? true : validator.isValid(getText());

                // Fire change events
                textInputContentListeners.textRemoved(this, index, count);
                textInputContentListeners.textChanged(this);

                if (textValid != previousTextValid) {
                    textInputListeners.textValidChanged(this);
                }

                if (selectionStart != previousSelectionStart
                    || selectionLength != previousSelectionLength) {
                    textInputSelectionListeners.selectionChanged(this, selectionStart, selectionLength);
                }
            } else {
                textInputContentListeners.removeTextVetoed(this, vote);
            }
        }
    }

    /**
     * Returns a character sequence representing the text input's content.
     */
    public CharSequence getCharacters() {
        return characters;
    }

    /**
     * Returns the character at a given index.
     *
     * @param index
     */
    public char getCharacterAt(int index) {
        return characters.charAt(index);
    }

    /**
     * Returns the number of characters in the text input.
     */
    public int getCharacterCount() {
        return characters.length();
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
                if ((characters.length() + text.length()) > maximumLength) {
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
     * inclusive. Returns <tt>null</tt> if the selection length is <tt>0</tt>.
     */
    public Span getSelection() {
        return (selectionLength == 0) ? null : new Span(selectionStart,
            selectionStart + selectionLength - 1);
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
        if (selectionLength < 0) {
            throw new IllegalArgumentException("selectionLength is negative.");
        }

        if (selectionStart < 0
            || selectionStart + selectionLength > characters.length()) {
            throw new IndexOutOfBoundsException();
        }

        int previousSelectionStart = this.selectionStart;
        int previousSelectionLength = this.selectionLength;

        if (previousSelectionStart != selectionStart
            || previousSelectionLength != selectionLength) {
            this.selectionStart = selectionStart;
            this.selectionLength = selectionLength;

            textInputSelectionListeners.selectionChanged(this, previousSelectionStart,
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
        setSelection(0, characters.length());
    }

    /**
     * Clears the selection.
     */
    public void clearSelection() {
        setSelection(0, 0);
    }

    /**
     * Returns the currently selected text.
     *
     * @return
     * A new string containing a copy of the text in the selected range.
     */
    public String getSelectedText() {
        return getText(selectionStart, selectionStart + selectionLength);
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
            this.maximumLength = maximumLength;

            // Truncate the text, if necessary (do not allow listeners to vote on this change)
            int length = characters.length();

            if (length > maximumLength) {
                int count = length - maximumLength;
                characters.delete(maximumLength, length);
                textInputContentListeners.textRemoved(this, maximumLength, count);
                textInputContentListeners.textChanged(this);
            }

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
     * Returns the text input's prompt.
     */
    public String getPrompt() {
      return prompt;
    }

    /**
     * Sets the text input's prompt.
     *
     * @param prompt
     * The prompt text, or <tt>null</tt> for no prompt.
     */
    public void setPrompt(String prompt) {
      String previousPrompt = this.prompt;

      if (previousPrompt != prompt) {
         this.prompt = prompt;
         textInputListeners.promptChanged(this, previousPrompt);
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

        if (previousTextKey != textKey) {
            this.textKey = textKey;
            textInputBindingListeners.textKeyChanged(this, previousTextKey);
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
            textInputBindingListeners.textBindTypeChanged(this, previousTextBindType);
        }
    }

    public TextBindMapping getTextBindMapping() {
        return textBindMapping;
    }

    public void setTextBindMapping(TextBindMapping textBindMapping) {
        TextBindMapping previousTextBindMapping = this.textBindMapping;

        if (previousTextBindMapping != textBindMapping) {
            this.textBindMapping = textBindMapping;
            textInputBindingListeners.textBindMappingChanged(this, previousTextBindMapping);
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

    public int getInsertionPoint(int x) {
        TextInput.Skin textInputSkin = (TextInput.Skin)getSkin();
        return textInputSkin.getInsertionPoint(x);
    }

    public Bounds getCharacterBounds(int index) {
        TextInput.Skin textInputSkin = (TextInput.Skin)getSkin();
        return textInputSkin.getCharacterBounds(index);
    }

    /**
     * Gets the validator associated with this text input.
     */
    public Validator getValidator() {
        return validator;
    }

    /**
     * Sets the validator associated with this text input.
     *
     * @param validator
     * The validator to use, or <tt>null</tt> to use no validator.
     */
    public void setValidator(Validator validator) {
        Validator previousValidator = this.validator;

        if (validator != previousValidator) {
            this.validator = validator;

            // Store previous text valid flag
            boolean previousTextValid = textValid;

            // Update the text valid flag
            textValid = (validator == null) ? true : validator.isValid(getText());

            textInputListeners.textValidatorChanged(this, previousValidator);

            // Fire additional events as needed
            if (textValid != previousTextValid) {
                textInputListeners.textValidChanged(this);
            }
        }
    }

    /**
     * Returns the text input's strict validation flag.
     */
    public boolean isStrictValidation() {
        return strictValidation;
    }

    /**
     * Sets the text input's strict validation flag. When enabled, only valid text will be
     * accepted by the text input.
     *
     * @param strictValidation
     */
    public void setStrictValidation(boolean strictValidation) {
        if (this.strictValidation != strictValidation) {
            this.strictValidation = strictValidation;
            textInputListeners.strictValidationChanged(this);
        }
    }

    /**
     * Reports whether this text input's text is currently valid as defined by
     * its validator.
     *
     * @return
     * <tt>true</tt> if the text is valid or no validator is installed;
     * <tt>false</tt>, otherwise.
     */
    public boolean isTextValid() {
        return textValid;
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

            textInputListeners.editableChanged(this);
        }
    }

    /**
     * Returns the text input listener list.
     */
    public ListenerList<TextInputListener> getTextInputListeners() {
        return textInputListeners;
    }

    /**
     * Returns the text input text listener list.
     */
    public ListenerList<TextInputContentListener> getTextInputContentListeners() {
        return textInputContentListeners;
    }

    /**
     * Returns the text input selection listener list.
     */
    public ListenerList<TextInputSelectionListener> getTextInputSelectionListeners() {
        return textInputSelectionListeners;
    }

    /**
     * Returns the text input binding listener list.
     */
    public ListenerList<TextInputBindingListener> getTextInputBindingListeners() {
        return textInputBindingListeners;
    }
}
