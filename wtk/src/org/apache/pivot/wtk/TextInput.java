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

import org.apache.pivot.collections.LinkedStack;
import org.apache.pivot.json.JSON;
import org.apache.pivot.text.AttributedStringCharacterIterator;
import org.apache.pivot.text.CharSpan;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.validation.Validator;

/**
 * A component that allows a user to enter a single line of unformatted text.
 */
public class TextInput extends Component {
    /**
     * Text input skin interface. Text input skins are required to implement this.
     */
    public interface Skin {
        /**
         * @return The insertion point for a given location.
         *
         * @param x The X-position (of the mouse probably).
         */
        public int getInsertionPoint(int x);

        /**
         * @return The bounds of the character at a given index.
         *
         * @param index The location to check.
         */
        public Bounds getCharacterBounds(int index);
    }

    /**
     * Translates between text and context data during data binding.
     */
    public interface TextBindMapping {
        /**
         * Converts a value from the bind context to a text representation
         * during a {@link Component#load(Object)} operation.
         *
         * @param value The value retrieved from the bound object.
         * @return A text representation of this value for display.
         */
        public String toString(Object value);

        /**
         * Converts a text string to a value to be stored in the bind context
         * during a {@link Component#store(Object)} operation.
         *
         * @param text The current text from the control.
         * @return A value suitable for storage in the bound object.
         */
        public Object valueOf(String text);
    }

    private interface Edit {
        public void undo();
    }

    private class InsertTextEdit implements Edit {
        private final int index;
        private final int count;

        public InsertTextEdit(final CharSequence text, final int index) {
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

        public RemoveTextEdit(final int index, final int count) {
            this.index = index;
            text = getText(index, index + count);
        }

        @Override
        public void undo() {
            insertText(text, index, false);
        }
    }

    private StringBuilder characters = new StringBuilder();
    private AttributedStringCharacterIterator composedText = null;

    private int selectionStart = 0;
    private int selectionLength = 0;

    private int textSize = DEFAULT_TEXT_SIZE;
    private int maximumLength = 32767;
    private boolean password = false;
    private String prompt = null;
    private boolean editable = true;

    private String textKey = null;
    private BindType textBindType = BindType.BOTH;
    private TextBindMapping textBindMapping = null;

    private Validator validator = null;
    private boolean strictValidation = false;
    private boolean textValid = true;

    private LinkedStack<Edit> editHistory = new LinkedStack<>(MAXIMUM_EDIT_HISTORY_LENGTH);

    private TextInputListener.Listeners textInputListeners = new TextInputListener.Listeners();
    private TextInputContentListener.Listeners textInputContentListeners = new TextInputContentListener.Listeners();
    private TextInputSelectionListener.Listeners textInputSelectionListeners =
        new TextInputSelectionListener.Listeners();
    private TextInputBindingListener.Listeners textInputBindingListeners = new TextInputBindingListener.Listeners();

    public static final int DEFAULT_TEXT_SIZE = 16;

    private static final int MAXIMUM_EDIT_HISTORY_LENGTH = 30;

    public TextInput() {
        installSkin(TextInput.class);
    }

    @Override
    protected void setSkin(final org.apache.pivot.wtk.Skin skin) {
        checkSkin(skin, TextInput.Skin.class);

        super.setSkin(skin);
    }

    /**
     * Returns the text content of the text input.
     *
     * @return A string containing a copy of the text input's text content.
     */
    public String getText() {
        return characters.toString();
    }

    /**
     * Returns a portion of the text content of the text input.
     *
     * @param beginIndex The starting index of the text to retrieve (inclusive).
     * @param endIndex The ending index of the text (exclusive).
     * @return A string containing a copy of the text area's text content.
     */
    public String getText(final int beginIndex, final int endIndex) {
        return characters.substring(beginIndex, endIndex);
    }

    public void setText(final String text) {
        Utils.checkNull(text, "text");

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

        if (selectionStart != previousSelectionStart || selectionLength != previousSelectionLength) {
            textInputSelectionListeners.selectionChanged(this, selectionStart, selectionLength);
        }
    }

    public void insertText(final CharSequence text, final int index) {
        insertText(text, index, true);
    }

    private void insertText(final CharSequence text, final int index, final boolean addToEditHistory) {
        Utils.checkNull(text, "text");

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
                    editHistory.push(new InsertTextEdit(text, index));
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
                    textInputSelectionListeners.selectionChanged(this, selectionStart,
                        selectionLength);
                }
            } else {
                textInputContentListeners.insertTextVetoed(this, vote);
            }
        }
    }

    public void removeText(final int index, final int count) {
        removeText(index, count, true);
    }

    private void removeText(final int index, final int count, final boolean addToEditHistory) {
        if (count > 0) {
            Vote vote = textInputContentListeners.previewRemoveText(this, index, count);

            if (vote == Vote.APPROVE) {
                // Add a remove history item
                if (addToEditHistory) {
                    editHistory.push(new RemoveTextEdit(index, count));
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
                    textInputSelectionListeners.selectionChanged(this, selectionStart,
                        selectionLength);
                }
            } else {
                textInputContentListeners.removeTextVetoed(this, vote);
            }
        }
    }

    /**
     * Return the current text that is in process of being composed
     * using the Input Method Editor.  This is temporary text that
     * must be displayed, scrolled, etc. but is not a permanent
     * part of what would be returned from {@link #getText} for instance.
     *
     * @return The current composed text or {@code null} if we're not
     * using an IME or we're in English input mode, or user just
     * committed or deleted the composed text.
     */
    public AttributedStringCharacterIterator getComposedText() {
        return composedText;
    }

    /**
     * Called from the Input Method Editor callbacks to set the current
     * composed text (that is, the text currently being composed into something
     * meaningful).
     *
     * @param composedText The current composed text (which can be {@code null}
     * for many different reasons).
     */
    public void setComposedText(final AttributedStringCharacterIterator composedText) {
        this.composedText = composedText;
    }

    /**
     * @return A character sequence representing the text input's content.
     */
    public CharSequence getCharacters() {
        return characters;
    }

    /**
     * @return A (sub) character sequence representing the contents between
     * the given indices.
     * @param start The start of the sequence (inclusive).
     * @param end The end of the sequence (exclusive).
     */
    public CharSequence getCharacters(final int start, final int end) {
        return characters.subSequence(start, end);
    }

    /**
     * @return The character at a given index.
     *
     * @param index Location of the character to retrieve.
     */
    public char getCharacterAt(final int index) {
        return characters.charAt(index);
    }

    /**
     * @return The number of characters in the text input.
     */
    public int getCharacterCount() {
        return characters.length();
    }

    /**
     * Places any selected text on the clipboard and deletes it from the text input.
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

        if (clipboardContent != null && clipboardContent.containsText()) {
            // Paste the string representation of the content
            String text = null;
            try {
                text = clipboardContent.getText();
            } catch (IOException exception) {
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
        if (editHistory.getDepth() > 0) {
            Edit edit = editHistory.pop();
            edit.undo();
        }
    }

    /**
     * @return The starting index of the selection.
     */
    public final int getSelectionStart() {
        return selectionStart;
    }

    /**
     * @return The length of the selection; may be <tt>0</tt>.
     */
    public final int getSelectionLength() {
        return selectionLength;
    }

    /**
     * Returns a span representing the current selection.
     *
     * @return A span containing the current selection. Both start and end
     * points are inclusive. Returns <tt>null</tt> if the selection length is
     * <tt>0</tt>.
     */
    public final Span getSelection() {
        return (selectionLength == 0) ? null : new Span(selectionStart, selectionStart + selectionLength - 1);
    }

    /**
     * Returns a character span (start, length) representing the current selection.
     *
     * @return A char span with the start and length values.
     */
    public final CharSpan getCharSelection() {
        return new CharSpan(selectionStart, selectionLength);
    }

    /**
     * Sets the selection. The sum of the selection start and length must be
     * less than the length of the text input's content.
     *
     * @param selectionStart The starting index of the selection.
     * @param selectionLength The length of the selection.
     */
    public final void setSelection(final int selectionStart, final int selectionLength) {
        Utils.checkNonNegative(selectionLength, "selectionLength");

        int composedTextLength = composedText != null ? (composedText.getEndIndex() - composedText.getBeginIndex()) : 0;
        if (selectionStart < 0 || selectionStart + selectionLength > characters.length() + composedTextLength) {
            throw new IndexOutOfBoundsException("Selection value is out of bounds.");
        }

        int previousSelectionStart = this.selectionStart;
        int previousSelectionLength = this.selectionLength;

        if (previousSelectionStart != selectionStart || previousSelectionLength != selectionLength) {
            this.selectionStart = selectionStart;
            this.selectionLength = selectionLength;

            textInputSelectionListeners.selectionChanged(this, previousSelectionStart,
                previousSelectionLength);
        }
    }

    /**
     * Sets the selection.
     *
     * @param selection The span (start inclusive to end inclusive).
     * @see #setSelection(int, int)
     * @throws IllegalArgumentException if the selection span is {@code null}.
     */
    public final void setSelection(final Span selection) {
        Utils.checkNull(selection, "selection");

        setSelection(Math.min(selection.start, selection.end), (int) selection.getLength());
    }

    /**
     * Sets the selection.
     *
     * @param selection The character span (start and length) for the selection.
     * @see #setSelection(int, int)
     * @throws IllegalArgumentException if the character span is {@code null}.
     */
    public final void setSelection(final CharSpan selection) {
        Utils.checkNull(selection, "selection");

        setSelection(selection.start, selection.length);
    }

    /**
     * Selects all text.
     */
    public final void selectAll() {
        setSelection(0, characters.length());
    }

    /**
     * Clears the selection.
     */
    public final void clearSelection() {
        setSelection(0, 0);
    }

    /**
     * Returns the currently selected text.
     *
     * @return A new string containing a copy of the text in the selected range.
     */
    public final String getSelectedText() {
        return getText(selectionStart, selectionStart + selectionLength);
    }

    /**
     * Returns the text size.
     *
     * @return The number of characters to display in the text input.
     */
    public final int getTextSize() {
        return textSize;
    }

    /**
     * Sets the text size.
     *
     * @param textSize The number of characters to display in the text input.
     * @throws IllegalArgumentException if the size value is negative.
     */
    public final void setTextSize(final int textSize) {
        Utils.checkNonNegative(textSize, "textSize");

        int previousTextSize = this.textSize;

        if (previousTextSize != textSize) {
            this.textSize = textSize;
            textInputListeners.textSizeChanged(this, previousTextSize);
        }
    }

    /**
     * Returns the maximum length of the text input's text content.
     *
     * @return The maximum length of the text input's text content.
     */
    public final int getMaximumLength() {
        return maximumLength;
    }

    /**
     * Sets the maximum length of the text input's text content.
     *
     * @param maximumLength The maximum length of the text input's text content.
     * @throws IllegalArgumentException if the length value is negative.
     */
    public final void setMaximumLength(final int maximumLength) {
        Utils.checkNonNegative(maximumLength, "maximumLength");

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
     * @return <tt>true</tt> if this is a password text input; <tt>false</tt>,
     * otherwise.
     */
    public final boolean isPassword() {
        return password;
    }

    /**
     * Sets or clears the password flag. If the password flag is set, the text
     * input will visually mask its contents.
     *
     * @param password <tt>true</tt> if this is a password text input;
     * <tt>false</tt>, otherwise.
     */
    public final void setPassword(final boolean password) {
        if (this.password != password) {
            this.password = password;
            textInputListeners.passwordChanged(this);
        }
    }

    /**
     * @return The text input's prompt.
     */
    public final String getPrompt() {
        return prompt;
    }

    /**
     * Sets the text input's prompt.
     *
     * @param prompt The prompt text, or <tt>null</tt> for no prompt.
     */
    public final void setPrompt(final String prompt) {
        String previousPrompt = this.prompt;

        if (previousPrompt != prompt) {
            this.prompt = prompt;
            textInputListeners.promptChanged(this, previousPrompt);
        }
    }

    /**
     * Returns the text input's text key.
     *
     * @return The text key, or <tt>null</tt> if no text key is set.
     */
    public final String getTextKey() {
        return textKey;
    }

    /**
     * Sets the text input's text key.
     *
     * @param textKey The text key, or <tt>null</tt> to clear the binding.
     */
    public final void setTextKey(final String textKey) {
        String previousTextKey = this.textKey;

        if (previousTextKey != textKey) {
            this.textKey = textKey;
            textInputBindingListeners.textKeyChanged(this, previousTextKey);
        }
    }

    public final BindType getTextBindType() {
        return textBindType;
    }

    public final void setTextBindType(final BindType textBindType) {
        Utils.checkNull(textBindType, "textBindType");

        BindType previousTextBindType = this.textBindType;

        if (previousTextBindType != textBindType) {
            this.textBindType = textBindType;
            textInputBindingListeners.textBindTypeChanged(this, previousTextBindType);
        }
    }

    public final TextBindMapping getTextBindMapping() {
        return textBindMapping;
    }

    public final void setTextBindMapping(final TextBindMapping textBindMapping) {
        TextBindMapping previousTextBindMapping = this.textBindMapping;

        if (previousTextBindMapping != textBindMapping) {
            this.textBindMapping = textBindMapping;
            textInputBindingListeners.textBindMappingChanged(this, previousTextBindMapping);
        }
    }

    @Override
    public void load(final Object context) {
        if (textKey != null && JSON.containsKey(context, textKey) && textBindType != BindType.STORE) {
            Object value = JSON.get(context, textKey);

            if (textBindMapping == null) {
                value = (value == null) ? "" : value.toString();
            } else {
                value = textBindMapping.toString(value);
            }

            setText((String) value);
        }
    }

    @Override
    public void store(final Object context) {
        if (textKey != null && textBindType != BindType.LOAD) {
            String text = getText();
            JSON.put(context, textKey,
                (textBindMapping == null) ? text : textBindMapping.valueOf(text));
        }
    }

    @Override
    public void clear() {
        if (textKey != null) {
            setText("");
        }
    }

    public final int getInsertionPoint(final int x) {
        TextInput.Skin textInputSkin = (TextInput.Skin) getSkin();
        return textInputSkin.getInsertionPoint(x);
    }

    public final Bounds getCharacterBounds(final int index) {
        TextInput.Skin textInputSkin = (TextInput.Skin) getSkin();
        return textInputSkin.getCharacterBounds(index);
    }

    /**
     * @return The validator associated with this text input.
     */
    public final Validator getValidator() {
        return validator;
    }

    /**
     * Sets the validator associated with this text input.
     *
     * @param validator The validator to use, or <tt>null</tt> to use no
     * validator.
     */
    public final void setValidator(final Validator validator) {
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
     * @return The text input's strict validation flag.
     */
    public final boolean isStrictValidation() {
        return strictValidation;
    }

    /**
     * Sets the text input's strict validation flag. When enabled, only valid
     * text will be accepted by the text input.
     *
     * @param strictValidation The new flag setting.
     */
    public final void setStrictValidation(final boolean strictValidation) {
        if (this.strictValidation != strictValidation) {
            this.strictValidation = strictValidation;
            textInputListeners.strictValidationChanged(this);
        }
    }

    /**
     * Reports whether this text input's text is currently valid as defined by
     * its validator.
     *
     * @return <tt>true</tt> if the text is valid or no validator is installed;
     * <tt>false</tt>, otherwise.
     */
    public final boolean isTextValid() {
        return textValid;
    }

    /**
     * @return The text area's editable flag.
     */
    public final boolean isEditable() {
        return editable;
    }

    /**
     * Sets the text area's editable flag.
     *
     * @param editable The new flag setting.
     */
    public final void setEditable(final boolean editable) {
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
     * @return The text input listener list.
     */
    public ListenerList<TextInputListener> getTextInputListeners() {
        return textInputListeners;
    }

    /**
     * @return The text input content listener list.
     */
    public ListenerList<TextInputContentListener> getTextInputContentListeners() {
        return textInputContentListeners;
    }

    /**
     * @return The text input selection listener list.
     */
    public ListenerList<TextInputSelectionListener> getTextInputSelectionListeners() {
        return textInputSelectionListeners;
    }

    /**
     * @return The text input binding listener list.
     */
    public ListenerList<TextInputBindingListener> getTextInputBindingListeners() {
        return textInputBindingListeners;
    }

}
