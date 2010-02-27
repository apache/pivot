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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.text.Element;
import org.apache.pivot.wtk.text.Node;
import org.apache.pivot.wtk.text.NodeListener;
import org.apache.pivot.wtk.text.TextNode;
import org.apache.pivot.wtk.text.validation.Validator;

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
         *
         * @return
         * The insertion point for the given location.
         */
        public int getInsertionPoint(int x);

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
    public interface BindMapping {
        /**
         * Converts a value from the bind context to a text representation.
         *
         * @param value
         */
        public String toString(Object value);

        /**
         * Converts a text string to a value to be stored in the bind context.
         *
         * @param text
         */
        public Object valueOf(String text);
    }

    private static class TextInputListenerList extends ListenerList<TextInputListener>
        implements TextInputListener {
        @Override
        public void textNodeChanged(TextInput textInput, TextNode previousTextNode) {
            for (TextInputListener listener : this) {
                listener.textNodeChanged(textInput, previousTextNode);
            }
        }

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
        public void textKeyChanged(TextInput textInput, String previousTextKey) {
            for (TextInputListener listener : this) {
                listener.textKeyChanged(textInput, previousTextKey);
            }
        }

        @Override
        public void bindMappingChanged(TextInput textInput, TextInput.BindMapping previousBindMapping) {
            for (TextInputListener listener : this) {
                listener.bindMappingChanged(textInput, previousBindMapping);
            }
        }

        @Override
        public void textValidChanged(TextInput textInput) {
            for (TextInputListener listener : this) {
                listener.textValidChanged(textInput);
            }
        }

        @Override
        public void textValidatorChanged(TextInput textInput, Validator previousValidator) {
            for (TextInputListener listener : this) {
                listener.textValidatorChanged(textInput, previousValidator);
            }
        }
    }

    private static class TextInputTextListenerList extends ListenerList<TextInputTextListener>
        implements TextInputTextListener {
        @Override
        public void textChanged(TextInput textInput) {
            for (TextInputTextListener listener : this) {
                listener.textChanged(textInput);
            }
        }
    }

    private static class TextInputCharacterListenerList extends ListenerList<TextInputCharacterListener>
        implements TextInputCharacterListener {
        @Override
        public void charactersInserted(TextInput textInput, int index, int count) {
            for (TextInputCharacterListener listener : this) {
                listener.charactersInserted(textInput, index, count);
            }
        }

        @Override
        public void charactersRemoved(TextInput textInput, int index, int count) {
            for (TextInputCharacterListener listener : this) {
                listener.charactersRemoved(textInput, index, count);
            }
        }
    }

    private static class TextInputSelectionListenerList extends ListenerList<TextInputSelectionListener>
        implements TextInputSelectionListener {
        @Override
        public void selectionChanged(TextInput textInput,
            int previousSelectionStart, int previousSelectionEnd) {
            for (TextInputSelectionListener listener : this) {
                listener.selectionChanged(textInput,
                    previousSelectionStart, previousSelectionEnd);
            }
        }
    }

    private TextNode textNode;

    private int selectionStart = 0;
    private int selectionLength = 0;

    private int textSize = DEFAULT_TEXT_SIZE;
    private int maximumLength = Integer.MAX_VALUE;

    private boolean password = false;
    private String prompt = null;

    private String textKey = null;
    private BindMapping bindMapping = null;

    private Validator validator = null;
    private boolean textValid = true;

    private NodeListener textNodeListener = new NodeListener() {
        @Override
        public void parentChanged(Node node, Element previousParent) {
        }

        @Override
        public void offsetChanged(Node node, int previousOffset) {
        }

        @Override
        public void rangeInserted(Node node, int offset, int characterCount) {
            if (selectionStart + selectionLength > offset) {
                if (selectionStart > offset) {
                    selectionStart += characterCount;
                } else {
                    selectionLength += characterCount;
                }
            }

            textInputCharacterListeners.charactersInserted(TextInput.this, offset, characterCount);
            textInputTextListeners.textChanged(TextInput.this);
            validateText();
        }

        @Override
        public void rangeRemoved(Node node, int offset, int characterCount) {
            if (selectionStart + selectionLength > offset) {
                if (selectionStart > offset) {
                    selectionStart -= characterCount;
                } else {
                    selectionLength -= characterCount;
                }
            }

            textInputCharacterListeners.charactersRemoved(TextInput.this, offset, characterCount);
            textInputTextListeners.textChanged(TextInput.this);
            validateText();
        }
    };

    private TextInputListenerList textInputListeners = new TextInputListenerList();
    private TextInputTextListenerList textInputTextListeners = new TextInputTextListenerList();
    private TextInputCharacterListenerList textInputCharacterListeners = new TextInputCharacterListenerList();
    private TextInputSelectionListenerList textInputSelectionListeners = new TextInputSelectionListenerList();

    public static final int DEFAULT_TEXT_SIZE = 16;

    public TextInput() {
        installThemeSkin(TextInput.class);
        setText("");
    }

    @Override
    protected void setSkin(org.apache.pivot.wtk.Skin skin) {
        if (!(skin instanceof TextInput.Skin)) {
            throw new IllegalArgumentException("Skin class must implement "
                + TextInput.Skin.class.getName());
        }

        super.setSkin(skin);
    }

    public TextNode getTextNode() {
        return textNode;
    }

    public void setTextNode(TextNode textNode) {
        if (textNode != null
            && textNode.getCharacterCount() > maximumLength) {
            throw new IllegalArgumentException("Text length is greater than maximum length.");
        }

        TextNode previousTextNode = this.textNode;

        if (previousTextNode != textNode) {
            if (previousTextNode != null) {
                previousTextNode.getNodeListeners().remove(textNodeListener);
            }

            this.textNode = textNode;

            if (textNode == null) {
                selectionStart = 0;
            } else {
                selectionStart = textNode.getCharacterCount();
                textNode.getNodeListeners().add(textNodeListener);
            }

            selectionLength = 0;

            textInputListeners.textNodeChanged(this, previousTextNode);
            textInputTextListeners.textChanged(this);

            validateText();
        }
    }

    public String getText() {
        return (textNode == null) ? null : textNode.getText();
    }

    public void setText(String text) {
        setTextNode((text == null) ? null : new TextNode(text));
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
        if (textNode == null) {
            throw new IllegalStateException();
        }

        if (text == null) {
            throw new IllegalArgumentException("text is null.");
        }

        if (index < 0
            || index > textNode.getCharacterCount()) {
            throw new IndexOutOfBoundsException();
        }

        if (selectionLength > 0) {
            // TODO Make this part of the undoable action (for all such
            // actions)
            textNode.removeRange(selectionStart, selectionLength);
        }

        // Insert the text
        if (textNode.getCharacterCount() + text.length() > maximumLength) {
            throw new IllegalArgumentException("Insertion of text would exceed maximum length.");
        }

        int length = textNode.getCharacterCount();
        textNode.insertText(text, index);

        // Update the selection only if a listener did not modify the text
        if (length + text.length() == textNode.getCharacterCount()) {
            setSelection(index + text.length(), 0);
        }
    }

    /**
     * Returns the character count of the text node.
     *
     * @return
     * The text node's length, or <tt>0</tt> if the text node is <tt>null</tt>.
     */
    public int getTextLength() {
        return (textNode == null) ? 0 : textNode.getCharacterCount();
    }

    public void delete(Direction direction) {
        if (textNode == null) {
            throw new IllegalStateException();
        }

        if (direction == null) {
            throw new IllegalArgumentException("direction is null.");
        }

        if (selectionLength > 0) {
            // TODO Make this part of the undoable action (for all such
            // actions)
            textNode.removeRange(selectionStart, selectionLength);
        } else {
            int offset = selectionStart;

            if (direction == Direction.BACKWARD) {
                offset--;
            }

            if (offset >= 0
                && offset < textNode.getCharacterCount()) {
                textNode.removeRange(offset, 1);
            }
        }
    }

    public void cut() {
        if (textNode == null) {
            throw new IllegalStateException();
        }

        // Delete any selected text and put it on the clipboard
        if (selectionLength > 0) {
            TextNode removedRange =
                (TextNode)textNode.removeRange(selectionStart, selectionLength);

            LocalManifest clipboardContent = new LocalManifest();
            clipboardContent.putText(removedRange.getText());
            Clipboard.setContent(clipboardContent);
        }
    }

    public void copy() {
        if (textNode == null) {
            throw new IllegalStateException();
        }

        // Copy selection to clipboard
        String selectedText = getSelectedText();

        if (selectedText != null) {
            LocalManifest clipboardContent = new LocalManifest();
            clipboardContent.putText(selectedText);
            Clipboard.setContent(clipboardContent);
        }
    }

    public void paste() {
        if (textNode == null) {
            throw new IllegalStateException();
        }

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
                if ((text.length() + textNode.getCharacterCount()) > maximumLength) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    // Remove any existing selection
                    if (selectionLength > 0) {
                        // TODO Make this part of the undoable action (for all such
                        // actions)
                        textNode.removeRange(selectionStart, selectionLength);
                    }

                    // Insert the clipboard contents
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
            || selectionStart + selectionLength > textNode.getCharacterCount()) {
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
        setSelection(0, textNode.getCharacterCount());
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
     * A new string containing a copy of the text in the selected range, or
     * <tt>null</tt> if nothing is selected.
     */
    public String getSelectedText() {
        String selectedText = null;

        if (selectionLength > 0) {
            TextNode selectedRange = (TextNode)textNode.getRange(selectionStart,
                selectionStart + selectionLength);
            selectedText = selectedRange.getText();
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
            if (textNode != null) {
                int characterCount = textNode.getCharacterCount();
                if (characterCount > maximumLength) {
                    textNode.removeText(maximumLength, characterCount - maximumLength);
                }
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
            textInputListeners.textKeyChanged(this, previousTextKey);
        }
    }

    public BindMapping getBindMapping() {
        return bindMapping;
    }

    public void setBindMapping(BindMapping bindMapping) {
        BindMapping previousBindMapping = this.bindMapping;

        if (previousBindMapping != bindMapping) {
            this.bindMapping = bindMapping;
            textInputListeners.bindMappingChanged(this, previousBindMapping);
        }
    }

    @Override
    public void load(Dictionary<String, ?> context) {
        if (textKey != null
            && JSONSerializer.containsKey(context, textKey)) {
            Object value = JSONSerializer.get(context, textKey);

            if (bindMapping == null
                && value != null) {
                value = value.toString();
            } else {
                value = bindMapping.toString(value);
            }

            setText((String)value);
        }
    }

    @Override
    public void store(Dictionary<String, ?> context) {
        if (isEnabled()
            && textKey != null) {
            String text = getText();
            JSONSerializer.put(context, textKey, (bindMapping == null) ?
                text : bindMapping.valueOf(text));
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

    public Bounds getCharacterBounds(int offset) {
        TextInput.Skin textInputSkin = (TextInput.Skin)getSkin();
        return textInputSkin.getCharacterBounds(offset);
    }

    /**
     * Tells whether or not this text input's text is currently valid as
     * defined by its validator. If there is no validator associated with this
     * text input, the text is assumed to always be valid.
     */
    public boolean isTextValid() {
        return textValid;
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
            textInputListeners.textValidatorChanged(this, previousValidator);
            validateText();
        }
    }

    /**
     * Updates the valid state after the text or the validator has changed.
     */
    private void validateText() {
        String text = getText();
        boolean textValid = (validator == null
            || text == null) ? true : validator.isValid(text);

        if (textValid != this.textValid) {
            this.textValid = textValid;
            textInputListeners.textValidChanged(this);
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
    public ListenerList<TextInputTextListener> getTextInputTextListeners() {
        return textInputTextListeners;
    }

    /**
     * Returns the text input character listener list.
     */
    public ListenerList<TextInputCharacterListener> getTextInputCharacterListeners() {
        return textInputCharacterListeners;
    }

    /**
     * Returns the text input selection listener list.
     */
    public ListenerList<TextInputSelectionListener> getTextInputSelectionListeners() {
        return textInputSelectionListeners;
    }
}
