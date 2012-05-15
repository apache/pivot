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

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.LinkedList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.text.ComponentNode;
import org.apache.pivot.wtk.text.ComponentNodeListener;
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
@DefaultProperty("document")
public class TextPane extends Container {
    /**
     * Enum representing a scroll direction.
     */
    public enum ScrollDirection {
        UP,
        DOWN
    }

    /**
     * Text pane skin interface. Text pane skins are required to implement
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
        public int getNextInsertionPoint(int x, int from, ScrollDirection direction);

        /**
         * Returns the row index of the character at a given offset within the document.
         *
         * @param offset
         *
         * @return
         * The row index of the character at the given offset.
         */
        public int getRowAt(int offset);

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

    private interface Edit {
        public void undo();
    }

    private static class RangeRemovedEdit implements Edit {
        private final Node node;
        private final int offset;
        private final Sequence<Node> removed;

        public RangeRemovedEdit(Node node, Sequence<Node> removed, int offset) {
            this.node = node;
            this.offset = offset;
            this.removed = removed;
        }

        @Override
        public void undo() {
            Document tmp = new Document();
            for (int i=0; i<removed.getLength(); i++) {
                tmp.add(removed.get(i));
            }
            node.insertRange(tmp, offset);
        }
    }

    private class RangeInsertedEdit implements Edit {
        private final Node node;
        private final int offset;
        private final int characterCount;

        public RangeInsertedEdit(Node node, int offset, int characterCount) {
            this.node = node;
            this.offset = offset;
            this.characterCount = characterCount;
        }

        @Override
        public void undo() {
            node.removeRange(offset, characterCount);
            int newSelectionStart = selectionStart;
            int newSelectionLength = selectionLength;
            if (newSelectionStart >= document.getCharacterCount()) {
                newSelectionStart = document.getCharacterCount() - 1;
            }
            if (newSelectionStart + newSelectionLength > document.getCharacterCount()) {
                newSelectionLength = document.getCharacterCount() - newSelectionStart;
            }
            setSelection(newSelectionStart, newSelectionLength);
        }
    }

    private static class TextPaneListenerList extends WTKListenerList<TextPaneListener>
        implements TextPaneListener {
        @Override
        public void documentChanged(TextPane textPane, Document previousText) {
            for (TextPaneListener listener : this) {
                listener.documentChanged(textPane, previousText);
            }
        }

        @Override
        public void editableChanged(TextPane textPane) {
            for (TextPaneListener listener : this) {
                listener.editableChanged(textPane);
            }
        }
    }

    private static class TextPaneCharacterListenerList extends WTKListenerList<TextPaneCharacterListener>
        implements TextPaneCharacterListener {
        @Override
        public void charactersInserted(TextPane textPane, int index, int count) {
            for (TextPaneCharacterListener listener : this) {
                listener.charactersInserted(textPane, index, count);
            }
        }

        @Override
        public void charactersRemoved(TextPane textPane, int index, int count) {
            for (TextPaneCharacterListener listener : this) {
                listener.charactersRemoved(textPane, index, count);
            }
        }
    }

    private static class TextPaneSelectionListenerList extends WTKListenerList<TextPaneSelectionListener>
        implements TextPaneSelectionListener {
        @Override
        public void selectionChanged(TextPane textPane,
            int previousSelectionStart, int previousSelectionLength) {
            for (TextPaneSelectionListener listener : this) {
                listener.selectionChanged(textPane, previousSelectionStart, previousSelectionLength);
            }
        }
    }

    private Document document = null;

    private int selectionStart = 0;
    private int selectionLength = 0;

    private boolean editable = true;
    private boolean undoingHistory = false;

    private ComponentNodeListener componentNodeListener = new ComponentNodeListener() {
        @Override
        public void componentChanged(ComponentNode componentNode, Component previousComponent) {
            // @TODO need to insert this at the correct index
            TextPane.super.remove(previousComponent);
            TextPane.super.add(componentNode.getComponent());
        }
    };

    private NodeListener documentListener = new NodeListener.Adapter() {
        @Override
        public void rangeInserted(Node node, int offset, int characterCount) {
            if (selectionStart + selectionLength > offset) {
                if (selectionStart > offset) {
                    selectionStart += characterCount;
                } else {
                    selectionLength += characterCount;
                }
            }

            if (!undoingHistory) {
                addHistoryItem(new RangeInsertedEdit(node, offset, characterCount));
            }

            textPaneCharacterListeners.charactersInserted(TextPane.this, offset, characterCount);
        }

        @Override
        public void nodesRemoved(Node node, Sequence<Node> removed, int offset) {

            for (int i = 0; i < removed.getLength(); i++) {
                Node descendant = removed.get(i);
                if (descendant instanceof ComponentNode) {
                    ComponentNode componentNode = (ComponentNode) descendant;
                    componentNode.getComponentNodeListeners().remove(componentNodeListener);
                    TextPane.super.remove(componentNode.getComponent());
                }
            }

            if (!undoingHistory) {
                addHistoryItem(new RangeRemovedEdit(node, removed, offset));
            }
        }

        @Override
        public void nodeInserted(Node node, int offset) {
            Node descendant = document.getDescendantAt(offset);
            if (descendant instanceof ComponentNode) {
                ComponentNode componentNode = (ComponentNode) descendant;
                componentNode.getComponentNodeListeners().add(componentNodeListener);
                TextPane.super.add(componentNode.getComponent());
            }
        }

        @Override
        public void rangeRemoved(Node node, int offset, int characterCount) {
            // if the end of the selection is in or after the range removed
            if (selectionStart + selectionLength > offset) {
                // if the start of the selection is in the range removed
                if (selectionStart > offset) {
                    selectionStart -= characterCount;
                    if (selectionStart < offset) {
                        selectionStart = offset;
                    }
                } else {
                    selectionLength -= characterCount;
                    if (selectionLength < 0) {
                        selectionLength = 0;
                    }
                }
            }

            textPaneCharacterListeners.charactersRemoved(TextPane.this, offset, characterCount);
        }
    };

    private LinkedList<Edit> editHistory = new LinkedList<Edit>();

    private TextPaneListenerList textPaneListeners = new TextPaneListenerList();
    private TextPaneCharacterListenerList textPaneCharacterListeners = new TextPaneCharacterListenerList();
    private TextPaneSelectionListenerList textPaneSelectionListeners = new TextPaneSelectionListenerList();

    private static final int MAXIMUM_EDIT_HISTORY_LENGTH = 30;

    public TextPane() {
        installSkin(TextPane.class);
    }

    @Override
    protected void setSkin(org.apache.pivot.wtk.Skin skin) {
        if (!(skin instanceof TextPane.Skin)) {
            throw new IllegalArgumentException("Skin class must implement "
                + TextPane.Skin.class.getName());
        }

        super.setSkin(skin);
    }

    /**
     * Returns the document that backs the text pane.
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Sets the document that backs the text pane.
     * Documents are not shareable across multiple TextPanes;
     * because a Document may contain Components, and a Component may only be in one Container at a time.
     *
     * @param document
     */
    public void setDocument(Document document) {
        Document previousDocument = this.document;

        if (previousDocument != document) {
            if (previousDocument != null) {
                previousDocument.getNodeListeners().remove(documentListener);
                removeComponentNodes(previousDocument);
            }

            if (document != null) {
                document.getNodeListeners().add(documentListener);
                addComponentNodes(document);
            }

            // Clear the edit history
            editHistory.clear();

            this.document = document;

            selectionStart = 0;
            selectionLength = 0;

            textPaneListeners.documentChanged(this, previousDocument);
        }
    }

    private void removeComponentNodes(Element element) {
        for (Node childNode : element) {
            if (childNode instanceof Element) {
                removeComponentNodes((Element) childNode);
            }
            if (childNode instanceof ComponentNode) {
                remove(((ComponentNode) childNode).getComponent());
            }
        }
    }

    private void addComponentNodes(Element element) {
        for (Node childNode : element) {
            if (childNode instanceof Element) {
                addComponentNodes((Element) childNode);
            }
            if (childNode instanceof ComponentNode) {
                add(((ComponentNode) childNode).getComponent());
            }
        }
    }

    public void insert(char character) {
        // TODO Don't make every character undoable; break at word boundaries?

        insert(Character.toString(character));
    }

    public void insert(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text is null.");
        }

        if (document == null) {
            throw new IllegalStateException();
        }

        if (selectionLength > 0) {
            delete(false);
        }

        if (document.getCharacterCount() == 0) {
            // the document is currently empty
            Paragraph paragraph = new Paragraph();
            paragraph.add(text);
            document.insert(paragraph, 0);
        } else {
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
                        textNode.insertText(text, offset - textNode.getOffset());
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
        }

        // Set the selection start to the character following the insertion
        setSelection(selectionStart + text.length(), 0);
    }

    public void insertImage(Image image) {
        if (image == null) {
            throw new IllegalArgumentException("image is null.");
        }

        if (document == null
            || document.getCharacterCount() == 0) {
            throw new IllegalStateException();
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
        if (document == null
            || document.getCharacterCount() == 0) {
            throw new IllegalStateException();
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

    /**
     * Returns character count of the document.
     *
     * @return
     * The document's character count, or <tt>0</tt> if the document is <tt>null</tt>.
     */
    public int getCharacterCount() {
        return (document == null) ? 0 : document.getCharacterCount();
    }

    public void delete(boolean backspace) {
        if (document == null
            || document.getCharacterCount() == 0) {
            throw new IllegalStateException();
        }

        int offset = selectionStart;

        int characterCount;
        if (selectionLength > 0) {
            characterCount = selectionLength;
        } else {
            if (backspace) {
                offset--;
            }

            characterCount = 1;
        }

        if (offset >= 0
            && offset < document.getCharacterCount()) {
            Node descendant = document.getDescendantAt(offset);

            if (selectionLength == 0
                && descendant instanceof Paragraph) {
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
                }
            } else {
                document.removeRange(offset, characterCount);
            }
        }

        // Ensure that the document remains editable
        if (document.getCharacterCount() == 0) {
            document.add(new Paragraph(""));
        }

        // Move the caret to the merge point
        if (offset >= 0) {
            setSelection(offset, 0);
        }
    }

    public void cut() {
        if (document == null
            || document.getCharacterCount() == 0) {
            throw new IllegalStateException();
        }

        if (selectionLength > 0) {
            // Copy selection to clipboard
            Document selection = (Document)document.removeRange(selectionStart, selectionLength);

            String selectedText = null;
            try {
                PlainTextSerializer serializer = new PlainTextSerializer();
                StringWriter writer = new StringWriter();
                serializer.writeObject(selection, writer);
                selectedText = writer.toString();
            } catch(IOException exception) {
                throw new RuntimeException(exception);
            }

            if (selectedText != null) {
                LocalManifest clipboardContent = new LocalManifest();
                clipboardContent.putText(selectedText);
                Clipboard.setContent(clipboardContent);
            }
        }

        setSelection(selectionStart, 0);
    }

    public void copy() {
        if (document == null
            || document.getCharacterCount() == 0) {
            throw new IllegalStateException();
        }

        String selectedText = getSelectedText();

        if (selectedText != null) {
            LocalManifest clipboardContent = new LocalManifest();
            clipboardContent.putText(selectedText);
            Clipboard.setContent(clipboardContent);
        }
    }

    public void paste() {
        if (document == null
            || document.getCharacterCount() == 0) {
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

            if (text != null
                && text.length() > 0) {
                // Remove any existing selection
                if (selectionLength > 0) {
                    // TODO Make this part of the undoable action (for all such
                    // actions)
                    delete(true);
                }

                // Insert the clipboard contents
                Document documentLocal;
                int n;
                try {
                    PlainTextSerializer serializer = new PlainTextSerializer();
                    StringReader reader = new StringReader(text);
                    documentLocal = serializer.readObject(reader);
                    n = documentLocal.getCharacterCount();

                    this.document.insertRange(documentLocal, selectionStart);
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                }

                setSelection(selectionStart + n, 0);
            }
        }
    }

    public void undo() {
        int n = editHistory.getLength();
        if (n > 0) {
            undoingHistory = true;
            Edit edit = editHistory.remove(n - 1, 1).get(0);
            edit.undo();
            undoingHistory = false;
        }
    }

    private void addHistoryItem(Edit edit) {
        editHistory.add(edit);

        if (editHistory.getLength() > MAXIMUM_EDIT_HISTORY_LENGTH) {
            editHistory.remove(0, 1);
        }
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
        if (document == null
            || document.getCharacterCount() == 0) {
            throw new IllegalStateException();
        }

        if (selectionLength < 0) {
            throw new IllegalArgumentException("selectionLength is negative, selectionLength=" + selectionLength);
        }

        indexBoundsCheck("selectionStart", selectionStart, 0, document.getCharacterCount() - 1);

        if (selectionStart + selectionLength > document.getCharacterCount()) {
            throw new IndexOutOfBoundsException("selectionStart=" + selectionStart + ", selectionLength=" + selectionLength
                + ", document.characterCount=" + document.getCharacterCount());
        }

        int previousSelectionStart = this.selectionStart;
        int previousSelectionLength = this.selectionLength;

        if (previousSelectionStart != selectionStart
            || previousSelectionLength != selectionLength) {
            this.selectionStart = selectionStart;
            this.selectionLength = selectionLength;

            textPaneSelectionListeners.selectionChanged(this,
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
        if (document == null) {
            throw new IllegalStateException();
        }

        setSelection(0, document.getCharacterCount());
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
            Document selection = (Document)document.getRange(selectionStart, selectionLength);

            try {
                PlainTextSerializer serializer = new PlainTextSerializer();
                StringWriter writer = new StringWriter();
                serializer.writeObject(selection, writer);
                selectedText = writer.toString();
            } catch(IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        return selectedText;
    }

    /**
     * Returns the text pane's editable flag.
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Sets the text pane's editable flag.
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

            textPaneListeners.editableChanged(this);
        }
    }

    public int getInsertionPoint(int x, int y) {
        TextPane.Skin textPaneSkin = (TextPane.Skin)getSkin();
        return textPaneSkin.getInsertionPoint(x, y);
    }

    public int getNextInsertionPoint(int x, int from, ScrollDirection direction) {
        TextPane.Skin textPaneSkin = (TextPane.Skin)getSkin();
        return textPaneSkin.getNextInsertionPoint(x, from, direction);
    }

    public int getRowAt(int offset) {
        TextPane.Skin textPaneSkin = (TextPane.Skin)getSkin();
        return textPaneSkin.getRowAt(offset);
    }

    public int getRowCount() {
        TextPane.Skin textPaneSkin = (TextPane.Skin)getSkin();
        return textPaneSkin.getRowCount();
    }

    public Bounds getCharacterBounds(int offset) {
        // We need to validate in case we get called from user-code after
        // a user-code initiated modification, but before another layout has run.
        validate();
        TextPane.Skin textPaneSkin = (TextPane.Skin)getSkin();
        return textPaneSkin.getCharacterBounds(offset);
    }

    public ListenerList<TextPaneListener> getTextPaneListeners() {
        return textPaneListeners;
    }

    public ListenerList<TextPaneCharacterListener> getTextPaneCharacterListeners() {
        return textPaneCharacterListeners;
    }

    public ListenerList<TextPaneSelectionListener> getTextPaneSelectionListeners() {
        return textPaneSelectionListeners;
    }
}
