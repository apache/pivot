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
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.text.Document;
import org.apache.pivot.wtk.text.Element;
import org.apache.pivot.wtk.text.Node;
import org.apache.pivot.wtk.text.NodeListener;
import org.apache.pivot.wtk.text.Paragraph;
import org.apache.pivot.wtk.text.PlainTextSerializer;
import org.apache.pivot.wtk.text.TextNode;


/**
 * Component that allows a user to enter and edit multiple lines of (optionally
 * formatted) text.
 */
public class TextArea extends Component {
    /**
     * Text area skin interface. Text area skins are required to implement
     * this.
     */
    public interface Skin {
        /**
         * Returns the offset of the character at a given location.
         *
         * @param x
         * @param y
         *
         * @return
         * The character offset at the given location.
         */
        public int getCharacterAt(int x, int y);

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

    private static class TextAreaListenerList extends ListenerList<TextAreaListener>
        implements TextAreaListener {
        @Override
        public void documentChanged(TextArea textArea, Document previousText) {
            for (TextAreaListener listener : this) {
                listener.documentChanged(textArea, previousText);
            }
        }

        @Override
        public void editableChanged(TextArea textArea) {
            for (TextAreaListener listener : this) {
                listener.editableChanged(textArea);
            }
        }

        @Override
        public void textKeyChanged(TextArea textArea, String previousTextKey) {
            for (TextAreaListener listener : this) {
                listener.textKeyChanged(textArea, previousTextKey);
            }
        }
    }

    private static class TextAreaCharacterListenerList extends ListenerList<TextAreaCharacterListener>
        implements TextAreaCharacterListener {
        @Override
        public void charactersInserted(TextArea textArea, int index, int count) {
            for (TextAreaCharacterListener listener : this) {
                listener.charactersInserted(textArea, index, count);
            }
        }

        @Override
        public void charactersRemoved(TextArea textArea, int index, int count) {
            for (TextAreaCharacterListener listener : this) {
                listener.charactersRemoved(textArea, index, count);
            }
        }
    }

    private static class TextAreaSelectionListenerList extends ListenerList<TextAreaSelectionListener>
        implements TextAreaSelectionListener {
        @Override
        public void selectionChanged(TextArea textArea,
            int previousSelectionStart, int previousSelectionLength) {
            for (TextAreaSelectionListener listener : this) {
                listener.selectionChanged(textArea, previousSelectionStart, previousSelectionLength);
            }
        }
    }

    private Document document = null;
    private boolean editable = true;
    private String textKey = null;

    private int selectionStart = 0;
    private int selectionLength = 0;

    private NodeListener documentListener = new NodeListener() {
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

            textAreaCharacterListeners.charactersInserted(TextArea.this, offset, characterCount);
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

            textAreaCharacterListeners.charactersRemoved(TextArea.this, offset, characterCount);
        }
    };

    private TextAreaListenerList textAreaListeners = new TextAreaListenerList();
    private TextAreaCharacterListenerList textAreaCharacterListeners = new TextAreaCharacterListenerList();
    private TextAreaSelectionListenerList textAreaSelectionListeners = new TextAreaSelectionListenerList();

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

    @Override
    protected void setParent(Container parent) {
        if (parent != null
            && !(parent instanceof Viewport)) {
            throw new IllegalArgumentException(getClass().getName()
                + " parent must be an instance of "
                + Viewport.class.getName());
        }

        super.setParent(parent);
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        Document previousDocument = this.document;

        if (previousDocument != document) {
            if (previousDocument != null) {
                previousDocument.getNodeListeners().remove(documentListener);
            }

            if (document != null) {
                document.getNodeListeners().add(documentListener);
            }

            this.document = document;

            selectionStart = 0;
            selectionLength = 0;

            textAreaListeners.documentChanged(this, previousDocument);
        }
    }

    public String getText() {
        String text = null;
        Document document = getDocument();

        if (document != null) {
            try {
                PlainTextSerializer serializer = new PlainTextSerializer();
                StringWriter writer = new StringWriter();
                serializer.writeObject(document, writer);
                text = writer.toString();
            } catch(SerializationException exception) {
                throw new RuntimeException(exception);
            } catch(IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        return text;
    }

    public void setText(String text) {
        Document document = null;

        if (text != null
            && text.length() > 0) {
            try {
                PlainTextSerializer serializer = new PlainTextSerializer();
                StringReader reader = new StringReader(text);
                document = serializer.readObject(reader);
            } catch(SerializationException exception) {
                throw new RuntimeException(exception);
            } catch(IOException exception) {
                throw new RuntimeException(exception);
            }
        } else {
            document = new Document();
            document.add(new Paragraph(""));
        }

        setDocument(document);
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

        if ((previousTextKey != null
            && textKey != null
            && !previousTextKey.equals(textKey))
            || previousTextKey != textKey) {
            this.textKey = textKey;
            textAreaListeners.textKeyChanged(this, previousTextKey);
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
        if (document == null) {
            throw new IllegalStateException("No document.");
        }

        if (selectionLength < 0) {
            throw new IllegalArgumentException("selectionLength is negative.");
        }

        if (selectionStart < 0
            || selectionStart + selectionLength > document.getCharacterCount()) {
            throw new IndexOutOfBoundsException();
        }

        int previousSelectionStart = this.selectionStart;
        int previousSelectionLength = this.selectionLength;

        if (previousSelectionStart != selectionStart
            || previousSelectionLength != selectionLength) {
            this.selectionStart = selectionStart;
            this.selectionLength = selectionLength;

            textAreaSelectionListeners.selectionChanged(this,
                previousSelectionStart, previousSelectionLength);
        }
    }

    public void insertText(char character) {
        // TODO Don't make every character undoable; break at word boundaries?

        insertText(Character.toString(character));
    }

    public void insertText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text is null.");
        }

        if (document == null) {
            throw new IllegalStateException("No document.");
        }

        if (selectionLength > 0) {
            // TODO Make this part of the undoable action (for all such
            // actions)
            document.removeRange(selectionStart, selectionLength);
        }

        Node descendant = document.getDescendantAt(selectionStart);
        int offset = selectionStart - descendant.getDocumentOffset();

        if (descendant instanceof TextNode) {
            // The caret is positioned within an existing text node
            TextNode textNode = (TextNode)descendant;
            textNode.insertText(text, offset);
        } else if (descendant instanceof Paragraph) {
            // The caret is positioned on the paragraph terminator
            Paragraph paragraph = (Paragraph)descendant;

            int n = paragraph.getLength();
            if (n > 0) {
                Node node = paragraph.get(n - 1);
                if (node instanceof TextNode) {
                    // Insert the text into the existing node
                    TextNode textNode = (TextNode)node;
                    textNode.insertText(text, offset);
                } else {
                    // Append a new text node
                    paragraph.add(new TextNode(text));
                }
            } else {
                // The paragraph is currently empty
                paragraph.add(new TextNode(text));
            }
        } else {
            // The caret is positioned on a non-text character node; insert
            // the text into the descendant's parent
            Element parent = descendant.getParent();
            int index = parent.indexOf(descendant);
            parent.insert(new TextNode(text), index);
        }

        // Set the selection start to the character following the insertion
        setSelection(selectionStart + text.length(), selectionLength);
    }

    public void insertImage(Image image) {
        if (image == null) {
            throw new IllegalArgumentException("image is null.");
        }

        if (document == null) {
            throw new IllegalStateException("No document.");
        }

        if (selectionLength > 0) {
            document.removeRange(selectionStart, selectionLength);
        }

        // TODO If the caret is placed in the middle of a text node, split it;
        // otherwise, insert an ImageNode immediately following the node
        // containing the caret

        // Set the selection start to the character following the insertion
        setSelection(selectionStart + 1, selectionLength);
    }

    public void insertParagraph() {
        if (document == null) {
            throw new IllegalStateException("No document.");
        }

        if (selectionLength > 0) {
            document.removeRange(selectionStart, selectionLength);
        }

        // Walk up the tree until we find a paragraph
        Node descendant = document.getDescendantAt(selectionStart);
        while (!(descendant instanceof Paragraph)) {
            descendant = descendant.getParent();
        }

        // Split the paragraph at the insertion point
        Paragraph leadingSegment = (Paragraph)descendant;
        int offset = selectionStart - leadingSegment.getDocumentOffset();
        int characterCount = leadingSegment.getCharacterCount() - offset;

        Paragraph trailingSegment = (Paragraph)leadingSegment.removeRange(offset, characterCount);

        Element parent = leadingSegment.getParent();
        int index = parent.indexOf(leadingSegment);
        parent.insert(trailingSegment, index + 1);

        // Set the selection start to the character following the insertion
        setSelection(selectionStart + 1, selectionLength);
    }

    public void delete(Direction direction) {
        if (direction == null) {
            throw new IllegalArgumentException("direction is null.");
        }

        if (document == null) {
            throw new IllegalStateException("No document.");
        }

        if (selectionLength > 0) {
            document.removeRange(selectionStart, selectionLength);
        } else {
            int offset = selectionStart;

            if (direction == Direction.BACKWARD) {
                offset--;
            }

            if (offset >= 0
                && offset < document.getCharacterCount()) {
                Node descendant = document.getDescendantAt(offset);

                if (descendant instanceof Paragraph) {
                    // We are deleting a paragraph terminator
                    Paragraph paragraph = (Paragraph)descendant;

                    Element parent = paragraph.getParent();
                    int index = parent.indexOf(paragraph);

                    // Attempt to merge any successive content into the paragraph
                    if (index < parent.getLength() - 1) {
                        // TODO This won't always be a paragraph - we'll need to
                        // find the next paragraph by walking the tree, then
                        // remove any empty nodes
                        Sequence<Node> removed = parent.remove(index + 1, 1);
                        Paragraph nextParagraph = (Paragraph)removed.get(0);
                        paragraph.insertRange(nextParagraph, paragraph.getCharacterCount() - 1);

                        // Move the caret to the merge point
                        setSelection(offset, 0);
                    }
                } else {
                    document.removeRange(offset, 1);
                }
            }
        }
    }

    public void cut() {
        // TODO
    }

    public void copy() {
        // TODO
    }

    public void paste() {
        // TODO
    }

    public void undo() {
        // TODO
    }

    public void redo() {
        // TODO
    }

    public int getCharacterAt(int x, int y) {
        TextArea.Skin textAreaSkin = (TextArea.Skin)getSkin();
        return textAreaSkin.getCharacterAt(x, y);
    }

    public Bounds getCharacterBounds(int offset) {
        TextArea.Skin textAreaSkin = (TextArea.Skin)getSkin();
        return textAreaSkin.getCharacterBounds(offset);
    }

    public ListenerList<TextAreaListener> getTextAreaListeners() {
        return textAreaListeners;
    }

    public ListenerList<TextAreaCharacterListener> getTextAreaCharacterListeners() {
        return textAreaCharacterListeners;
    }

    public ListenerList<TextAreaSelectionListener> getTextAreaSelectionListeners() {
        return textAreaSelectionListeners;
    }
}
