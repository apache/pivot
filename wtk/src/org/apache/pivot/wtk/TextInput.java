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

import java.io.IOException;

import org.apache.pivot.collections.Dictionary;
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
     * Text input listener list.
     */
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

    /**
     * Text input text listener list.
     */
    private static class TextInputTextListenerList extends ListenerList<TextInputTextListener>
        implements TextInputTextListener {
        @Override
        public void textChanged(TextInput textInput) {
            for (TextInputTextListener listener : this) {
                listener.textChanged(textInput);
            }
        }
    }

    /**
     * Text input character listener list.
     */
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

    /**
     * Text input selection listener list.
     */
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

    private TextNode textNode = null;

    private int selectionStart = 0;
    private int selectionLength = 0;
    private int textSize = DEFAULT_TEXT_SIZE;
    private int maximumLength = Integer.MAX_VALUE;
    private boolean password = false;
    private String prompt = null;
    private String textKey = null;
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
            updateTextValid();
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
            updateTextValid();
        }
    };

    private TextInputListenerList textInputListeners = new TextInputListenerList();
    private TextInputTextListenerList textInputTextListeners = new TextInputTextListenerList();
    private TextInputCharacterListenerList textInputCharacterListeners = new TextInputCharacterListenerList();
    private TextInputSelectionListenerList textInputSelectionListeners = new TextInputSelectionListenerList();

    private static final int DEFAULT_TEXT_SIZE = 20;

    public TextInput() {
        setTextNode(new TextNode());
        installSkin(TextInput.class);
    }

    public TextNode getTextNode() {
        return textNode;
    }

    public void setTextNode(TextNode textNode) {
        if (textNode == null) {
            throw new IllegalArgumentException("textNode is null.");
        }

        if (textNode.getCharacterCount() > maximumLength) {
            throw new IllegalArgumentException("Text length is greater than maximum length.");
        }

        TextNode previousTextNode = this.textNode;

        if (previousTextNode != textNode) {
            if (previousTextNode != null) {
                previousTextNode.getNodeListeners().remove(textNodeListener);
            }

            if (textNode != null) {
                textNode.getNodeListeners().add(textNodeListener);
            }

            // Clear the selection
            this.textNode = textNode;

            selectionStart = 0;
            selectionLength = 0;

            textInputListeners.textNodeChanged(this, previousTextNode);
            textInputTextListeners.textChanged(this);
            updateTextValid();
        }
    }

    public String getText() {
        return textNode.getText();
    }

    public void setText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text is null.");
        }

        setTextNode(new TextNode(text));
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
            || index > textNode.getCharacterCount()) {
            throw new IndexOutOfBoundsException();
        }

        if (textNode.getCharacterCount() + text.length() > maximumLength) {
            throw new IllegalArgumentException("Insertion of text would exceed maximum length.");
        }

        if (selectionLength > 0) {
            // TODO Make this part of the undoable action (for all such
            // actions)
            textNode.removeRange(selectionStart, selectionLength);
        }

        // Insert the text
        int length = textNode.getCharacterCount();
        textNode.insertText(text, index);

        // Update the selection only if a listener did not modify the text
        if (length + text.length() == textNode.getCharacterCount()) {
            setSelection(index + text.length(), 0);
        }
    }

    public int getTextLength() {
        return textNode.getCharacterCount();
    }

    public void delete(Direction direction) {
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
        // Copy selection to clipboard
        String selectedText = getSelectedText();

        if (selectedText != null) {
            LocalManifest clipboardContent = new LocalManifest();
            clipboardContent.putText(selectedText);
            Clipboard.setContent(clipboardContent);
        }
    }

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
                if ((text.length() + textNode.getCharacterCount()) > maximumLength) {
                    ApplicationContext.beep();
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
            int characterCount = textNode.getCharacterCount();
            if (characterCount > maximumLength) {
                textNode.removeText(maximumLength, characterCount - maximumLength);
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

        if ((previousTextKey != null
            && textKey != null
            && !previousTextKey.equals(textKey))
            || previousTextKey != textKey) {
            this.textKey = textKey;
            textInputListeners.textKeyChanged(this, previousTextKey);
        }
    }

    @Override
    public void load(Dictionary<String, ?> context) {
        if (textKey != null
            && context.containsKey(textKey)) {
            Object value = context.get(textKey);
            if (value != null) {
               value = value.toString();
            }

         setText((String)value);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void store(Dictionary<String, ?> context) {
        if (isEnabled()
            && textKey != null) {
            ((Dictionary<String, String>)context).put(textKey, getText());
        }
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
     * Updates the <tt>textValid</tt> flag and notifies listeners if the flag's
     * value has changed. It is the responsibility of methods to call this
     * method when the validity of the text may have changed.
     */
    private void updateTextValid() {
        boolean textValid = (validator == null ? true : validator.isValid(getText()));

        if (textValid != this.textValid) {
            this.textValid = textValid;
            textInputListeners.textValidChanged(this);
        }
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
            updateTextValid();
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
