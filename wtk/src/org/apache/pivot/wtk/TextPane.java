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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.LinkedStack;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.text.AttributedStringCharacterIterator;
import org.apache.pivot.text.CharSpan;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.StringUtils;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.text.Block;
import org.apache.pivot.wtk.text.ComponentNode;
import org.apache.pivot.wtk.text.ComponentNodeListener;
import org.apache.pivot.wtk.text.Document;
import org.apache.pivot.wtk.text.Element;
import org.apache.pivot.wtk.text.ImageNode;
import org.apache.pivot.wtk.text.Node;
import org.apache.pivot.wtk.text.NodeListener;
import org.apache.pivot.wtk.text.Paragraph;
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
        UP, DOWN
    }

    /**
     * Text pane skin interface. Text pane skins are required to implement this.
     */
    public interface Skin {
        /**
         * Returns the insertion point for a given location.
         *
         * @param x The X-coordinate of the location to check.
         * @param y The Y-coordinate of the location.
         * @return The insertion point for the given location.
         */
        public int getInsertionPoint(int x, int y);

        /**
         * Returns the next insertion point given an x coordinate and a
         * character offset.
         *
         * @param x The current X-coordinate to move from.
         * @param from The current character offset to move from.
         * @param direction The direction to move from the current location.
         * @return The next insertion point.
         */
        public int getNextInsertionPoint(int x, int from, ScrollDirection direction);

        /**
         * Returns the row index of the character at a given offset within the
         * document.
         *
         * @param offset The character offset to check.
         * @return The row index of the character at the given offset.
         */
        public int getRowAt(int offset);

        /**
         * @return The total number of rows in the document.
         */
        public int getRowCount();

        /**
         * Returns the bounds of the character at a given offset within the
         * document.
         *
         * @param offset The index of the character we want the bounds for.
         * @return The bounds of the character at the given offset.
         */
        public Bounds getCharacterBounds(int offset);

        /**
         * @return The current setting of the "tabWidth" style (so "setText"
         * uses the same value as Ctrl-Tab from user).
         */
        public int getTabWidth();
    }

    private interface Edit {
        public void undo();
    }

    private class TextInsertedEdit implements Edit {
        private final Node node;
        private final int offset;
        private final int characterCount;

        public TextInsertedEdit(Node node, int offset, int characterCount) {
            this.node = node;
            this.offset = offset;
            this.characterCount = characterCount;
        }

        @Override
        public void undo() {
            node.removeRange(offset, characterCount);
            changeSelection(offset);
        }
    }

    private class TextRemovedEdit implements Edit {
        private final Node node;
        private final int offset;
        private final CharSequence removedChars;

        public TextRemovedEdit(Node node, int offset, CharSequence removedChars) {
            this.node = node;
            this.offset = offset;
            this.removedChars = removedChars;
        }

        @Override
        public void undo() {
            if (offset != selectionStart) {
                changeSelection(offset);
            }
            insert(removedChars.toString());
            changeSelection(offset + removedChars.length());
        }
    }

    private Document document = null;
    private AttributedStringCharacterIterator composedText = null;

    private int selectionStart = 0;
    private int selectionLength = 0;

    private boolean expandTabs = false;

    private boolean editable = true;
    private boolean undoingHistory = false;
    private boolean bulkOperation = false;

    private ComponentNodeListener componentNodeListener = new ComponentNodeListener() {
        @Override
        public void componentChanged(ComponentNode componentNode, Component previousComponent) {
            // @TODO need to insert this at the correct index
            TextPane.super.remove(previousComponent);
            TextPane.super.add(componentNode.getComponent());
        }
    };

    private NodeListener documentListener = new NodeListener() {
        /**
         * @param offset Offset into the document.
         */
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
                editHistory.push(new TextInsertedEdit(node, offset, characterCount));
            }

            if (!bulkOperation) {
                textPaneCharacterListeners.charactersInserted(TextPane.this, offset, characterCount);
            }
        }

        /**
         * @param offset Offset into the document.
         */
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
        }

        /**
         * @param offset Offset into the document.
         */
        @Override
        public void nodeInserted(Node node, int offset) {
            Node descendant = document.getDescendantAt(offset);
            if (descendant instanceof ComponentNode) {
                ComponentNode componentNode = (ComponentNode) descendant;
                componentNode.getComponentNodeListeners().add(componentNodeListener);
                TextPane.super.add(componentNode.getComponent());
            }
        }

        /**
         * @param offset Offset into the document.
         */
        @Override
        public void rangeRemoved(Node node, int offset, int characterCount, CharSequence removedChars) {
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

            if (!undoingHistory && removedChars != null) {
                editHistory.push(new TextRemovedEdit(node, offset, removedChars));
            }

            if (!bulkOperation) {
                textPaneCharacterListeners.charactersRemoved(TextPane.this, offset, characterCount);
            }
        }
    };

    private LinkedStack<Edit> editHistory = new LinkedStack<>(MAXIMUM_EDIT_HISTORY_LENGTH);

    private TextPaneListener.Listeners textPaneListeners = new TextPaneListener.Listeners();
    private TextPaneCharacterListener.Listeners textPaneCharacterListeners = new TextPaneCharacterListener.Listeners();
    private TextPaneSelectionListener.Listeners textPaneSelectionListeners = new TextPaneSelectionListener.Listeners();

    private static final int MAXIMUM_EDIT_HISTORY_LENGTH = 100;

    public TextPane() {
        installSkin(TextPane.class);
    }

    @Override
    protected void setSkin(org.apache.pivot.wtk.Skin skin) {
        checkSkin(skin, TextPane.Skin.class);

        super.setSkin(skin);
    }

    /**
     * @return The document that backs the text pane.
     */
    public Document getDocument() {
        return document;
    }

    private void checkDocumentExists() {
        if (document == null || document.getCharacterCount() == 0) {
            throw new IllegalStateException("document is null or empty.");
        }
    }

    private void checkDocumentNull() {
        if (document == null) {
            throw new IllegalStateException("document is null.");
        }
    }

    /**
     * Sets the document that backs the text pane. Documents are not shareable
     * across multiple TextPanes; because a Document may contain Components, and
     * a Component may only be in one Container at a time.
     *
     * @param document The new document to be displayed by this text pane.
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

    /**
     * Some document rearrangements might not be suitable for undoing,
     * so allow users to specify when to do so.
     *
     * @param save Whether or not to save history at this time.
     */
    public void setSaveHistory(boolean save) {
        this.undoingHistory = !save;
    }

    private Node getRightmostDescendant(Element element) {
        int n = element.getLength();
        if (n > 0) {
            Node node = element.get(n - 1);
            if (node instanceof Element) {
                return getRightmostDescendant((Element) node);
            }
            return node;
        }
        return element;
    }

    /**
     * Helper function to remove a range of characters from the document and
     * notify the listeners just once (instead of once per node).
     *
     * @param start The starting location (document offset) of the characters
     * to be removed.
     * @param count The number of characters to remove from that location.
     * @return The document node where the characters were removed.
     */
    private Node removeDocumentRange(int start, int count) {
        bulkOperation = true;
        Node node = document.removeRange(start, count);
        bulkOperation = false;

        textPaneCharacterListeners.charactersRemoved(this, start, count);

        return node;
    }

    public void insert(char character) {
        // TODO Don't make every character undoable; break at word boundaries?

        insert(Character.toString(character));
    }

    public void insert(String text) {
        insertText(text, selectionStart);
    }

    public void insertText(String text, int index) {
        Utils.checkNull(text, "text");
        checkDocumentNull();

        if (selectionLength > 0) {
            delete(false);
        }

        if (document.getCharacterCount() == 0) {
            // the document is currently empty
            document.insert(new Paragraph(text), 0);
        } else {
            Node descendant = document.getDescendantAt(index);
            int offset = index - descendant.getDocumentOffset();

            if (descendant instanceof TextNode) {
                // The caret is positioned within an existing text node
                TextNode textNode = (TextNode) descendant;
                textNode.insertText(text, offset);
            } else if (descendant instanceof Paragraph) {
                // The caret is positioned on the paragraph terminator
                // so get to the bottom rightmost descendant and add there
                Paragraph paragraph = (Paragraph) descendant;

                Node node = getRightmostDescendant(paragraph);
                if (node instanceof TextNode) {
                    // Insert the text into the existing node
                    TextNode textNode = (TextNode) node;
                    textNode.insertText(text, index - textNode.getDocumentOffset());
                } else if (node instanceof Element) {
                    // Append a new text node
                    Element element = (Element) node;
                    element.add(new TextNode(text));
                } else {
                    // The paragraph is currently empty
                    paragraph.add(new TextNode(text));
                }
            } else {
                // The caret is positioned on a non-text character node; insert
                // the text into the descendant's parent
                Element parent = descendant.getParent();
                int elemIndex = parent.indexOf(descendant);
                parent.insert(new TextNode(text), elemIndex);
            }
        }

        // Set the selection start to the character following the insertion
        setSelection(index + text.length(), 0);
    }

    public void insertImage(Image image) {
        Utils.checkNull(image, "image");
        checkDocumentExists();

        if (selectionLength > 0) {
            removeDocumentRange(selectionStart, selectionLength);
        }

        // TODO If the caret is placed in the middle of a text node, split it;
        // otherwise, insert an ImageNode immediately following the block
        // containing the caret

        // If the insertion is at the end of the document, then just add
        if (selectionStart >= document.getCharacterCount() - 1) {
            document.add(new ImageNode(image));
        } else {
            // Walk up the tree until we find a block
            Node descendant = document.getDescendantAt(selectionStart);
            while (!(descendant instanceof Block)) {
                descendant = descendant.getParent();
            }
            Element parent = descendant.getParent();
            if (parent != null) {
                int index = parent.indexOf(descendant);
                parent.insert(new ImageNode(image), index + 1);
            }
        }

        // Set the selection start to the character following the insertion
        setSelection(selectionStart + 1, selectionLength);
    }

    public void insertComponent(Component component) {
        Utils.checkNull(component, "component");
        checkDocumentExists();

        if (selectionLength > 0) {
            removeDocumentRange(selectionStart, selectionLength);
        }

        // TODO If the caret is placed in the middle of a text node, split it;
        // otherwise, insert a ComponentNode immediately following the block
        // containing the caret

        // If the insertion is at the end of the document, then just add
        if (selectionStart >= document.getCharacterCount() - 1) {
            document.add(new ComponentNode(component));
        } else {
            // Walk up the tree until we find a block
            Node descendant = document.getDescendantAt(selectionStart);
            while (!(descendant instanceof Block)) {
                descendant = descendant.getParent();
            }
            Element parent = descendant.getParent();
            if (parent != null) {
                int index = parent.indexOf(descendant);
                parent.insert(new ComponentNode(component), index + 1);
            }
        }

        // Set the selection start to the character following the insertion
        setSelection(selectionStart + 1, selectionLength);
    }

    public void insertParagraph() {
        checkDocumentExists();

        if (selectionLength > 0) {
            removeDocumentRange(selectionStart, selectionLength);
        }

        // Walk up the tree until we find a paragraph
        Node descendant = document.getDescendantAt(selectionStart);
        while (!(descendant instanceof Paragraph)) {
            descendant = descendant.getParent();
        }

        // Split the paragraph at the insertion point
        Paragraph leadingSegment = (Paragraph) descendant;
        int offset = selectionStart - leadingSegment.getDocumentOffset();
        int characterCount = leadingSegment.getCharacterCount() - offset;

        Paragraph trailingSegment = (Paragraph) leadingSegment.removeRange(offset, characterCount);

        Element parent = leadingSegment.getParent();
        int index = parent.indexOf(leadingSegment);
        parent.insert(trailingSegment, index + 1);

        // Set the selection start to the character following the insertion
        setSelection(selectionStart + 1, selectionLength);
    }

    /**
     * Returns character count of the document.
     *
     * @return The document's character count, or <tt>0</tt> if the document is
     * <tt>null</tt>.
     */
    public int getCharacterCount() {
        return (document == null) ? 0 : document.getCharacterCount();
    }

    /**
     * Delete the currently selected text (if selection length &gt; 0),
     * or the character before or after the current cursor position,
     * depending on the <tt>backspace</tt> flag.
     * @param backspace {@code true} if the single character delete is
     * the character before the current position, or {@code false} for
     * the character after (at) the current position.
     */
    public void delete(boolean backspace) {
        if (selectionLength > 0) {
            removeText(selectionStart, selectionLength);
        } else {
            if (backspace) {
                removeText(selectionStart - 1, 1);
            } else {
                removeText(selectionStart, 1);
            }
        }
    }

    /**
     * Remove the text from the document starting at the given position
     * for the given number of characters.
     * @param offset Starting location to remove text.
     * @param characterCount The number of characters to remove.
     */
    public void removeText(int offset, int characterCount) {
        checkDocumentExists();

        if (offset >= 0 && offset < document.getCharacterCount()) {
            Node descendant = document.getDescendantAt(offset);

            // Used to be: if (selectionLength == 0 && ...
            if (characterCount <= 1 && descendant instanceof Paragraph) {
                // We are deleting a paragraph terminator
                Paragraph paragraph = (Paragraph) descendant;
                Element parent = paragraph.getParent();
                int index = parent.indexOf(paragraph);

                // Attempt to merge any successive content into the paragraph
                if (index < parent.getLength() - 1) {
                    // TODO This won't always be a paragraph - we'll need to
                    // find the next paragraph by walking the tree, then
                    // remove any empty nodes
                    Sequence<Node> removed = parent.remove(index + 1, 1);
                    Paragraph nextParagraph = (Paragraph) removed.get(0);
                    paragraph.insertRange(nextParagraph, paragraph.getCharacterCount() - 1);
                }
            } else {
                removeDocumentRange(offset, characterCount);
            }
        }

        // Ensure that the document remains editable
        if (document.getCharacterCount() == 0) {
            document.add(new Paragraph());
        }

        // Move the caret to the removal point
        if (offset >= 0) {
            setSelection(offset, 0);
        }
    }

    public void cut() {
        checkDocumentExists();

        if (selectionLength > 0) {
            // Copy selection to clipboard
            String selectedText = getSelectedText();

            if (selectedText != null) {
                delete(false);

                LocalManifest clipboardContent = new LocalManifest();
                clipboardContent.putText(selectedText);
                Clipboard.setContent(clipboardContent);
            }
        }

        setSelection(selectionStart, 0);
    }

    public void copy() {
        checkDocumentExists();

        String selectedText = getSelectedText();

        if (selectedText != null) {
            LocalManifest clipboardContent = new LocalManifest();
            clipboardContent.putText(selectedText);
            Clipboard.setContent(clipboardContent);
        }
    }

    private int insertWithTabSubstitution(String text, int startPos, int offsetInParagraph, int tabWidth) {
        int n = 0;
        int len = text.length();
        int tabIndex = text.indexOf('\t');
        int offset = offsetInParagraph;

        if (tabIndex < 0) {
            // Note: this first insert deletes the selection as well
            insertText(text, startPos);
            n = len;
        } else {
            int tabPos = 0;
            while (tabIndex >= 0) {
                insertText(text.substring(tabPos, tabIndex), startPos);
                int subTextLen = tabIndex - tabPos;
                n += subTextLen;
                startPos += subTextLen;
                offset += subTextLen;
                int tabLen = tabWidth - (offset % tabWidth);
                String spaces = StringUtils.fromNChars(' ', tabLen);
                insertText(spaces, startPos);
                n += tabLen;
                startPos += tabLen;
                offset += tabLen;
                tabPos = tabIndex + 1;
                tabIndex = text.indexOf('\t', tabPos);
            }
            if (tabPos < len) {
                insertText(text.substring(tabPos), startPos);
                n += (len - tabPos);
            }
        }
        return n;
    }

    public void paste() {
        if (document == null) {
            setDocument(new Document());
        }

        Manifest clipboardContent = Clipboard.getContent();

        if (clipboardContent != null && clipboardContent.containsText()) {
            // Paste the string representation of the content
            String text = null;
            try {
                // Replace \r\n with just \n and plain \r with \n instead
                // so we can deal with lines uniformly below.
                text = clipboardContent.getText().replace("\r\n", "\n").replace("\r", "\n");
            } catch (IOException exception) {
                // No-op
            }

            if (text != null && text.length() > 0) {
                // Insert the clipboard contents
                int n = 0;
                int start = selectionStart;
                int tabWidth = ((TextPane.Skin) getSkin()).getTabWidth();
                Node currentNode = document.getDescendantAt(start);
                while (!(currentNode instanceof Paragraph)) {
                    currentNode = currentNode.getParent();
                }
                int paragraphStartOffset = start - currentNode.getDocumentOffset();

                bulkOperation = true;
                // If there is only a line fragment here, then just insert it
                int eolIndex = text.indexOf('\n');
                if (eolIndex < 0) {
                    if (expandTabs) {
                        n = insertWithTabSubstitution(text, start, paragraphStartOffset, tabWidth);
                    } else {
                        insertText(text, start);
                        n = text.length();
                    }
                } else {
                    int textOffset = 0;
                    // Insert each line into place, with a new paragraph following
                    while (eolIndex >= 0) {
                        String fragment = text.substring(textOffset, eolIndex);
                        int len;
                        if (expandTabs) {
                            len = insertWithTabSubstitution(fragment, start, paragraphStartOffset, tabWidth);
                        } else {
                            insertText(fragment, start);
                            len = fragment.length();
                        }
                        insertParagraph();
                        n += len + 1;
                        start += len + 1;
                        paragraphStartOffset = 0;
                        textOffset = eolIndex + 1;
                        eolIndex = text.indexOf('\n', textOffset);
                    }
                    // Now deal with any leftover at end of string
                    if (textOffset < text.length()) {
                        String lastFragment = text.substring(textOffset);
                        if (expandTabs) {
                            n += insertWithTabSubstitution(lastFragment, start, paragraphStartOffset, tabWidth);
                        } else {
                            insertText(lastFragment, start);
                            n += lastFragment.length();
                        }
                    }
                }
                bulkOperation = false;

                textPaneCharacterListeners.charactersInserted(this, start, n);
            }
        }
    }

    public void undo() {
        if (editHistory.getDepth() > 0) {
            undoingHistory = true;
            Edit edit = editHistory.pop();
            edit.undo();
            undoingHistory = false;
        }
    }

    public void redo() {
        // TODO
    }

    /**
     * Add the text from the given element (and its children) to the given buffer,
     * respecting the range of characters to be included.
     *
     * @param text The buffer we're building.
     * @param element The current element in the document.
     * @param includeSpan The range of text to be included (in document-relative
     * coordinates).
     */
    private void addToText(StringBuilder text, Element element, Span includeSpan) {
        Span elementSpan = element.getDocumentSpan();
        Span elementIntersection = elementSpan.intersect(includeSpan);
        if (elementIntersection != null) {
            for (Node node : element) {
                if (node instanceof Element) {
                    addToText(text, (Element) node, includeSpan);
                } else {
                    Span nodeSpan = node.getDocumentSpan();
                    Span nodeIntersection = nodeSpan.intersect(includeSpan);
                    if (nodeIntersection != null) {
                        Span currentSpan = nodeIntersection.offset(-nodeSpan.start);
                        if (node instanceof TextNode) {
                            text.append(((TextNode) node).getCharacters(currentSpan));
                        } else if (node instanceof ComponentNode) {
                            text.append(((ComponentNode) node).getCharacters(currentSpan));
                        }
                        // TODO: anything more that could/should be handled?
                        // lists for instance???
                    }
                }
            }
            if (element instanceof Paragraph && elementIntersection.end == elementSpan.end) {
                // TODO: unclear if this is included in the character count for a paragraph or not
                // or what that means for the intersection range above
                text.append('\n');
            }
        }
    }

    /**
     * Convenience method to get all the text from the current document into a
     * single string.
     *
     * @return The complete text of the document as a string.
     * @see #setText
     */
    public String getText() {
        int count;
        Document doc = getDocument();
        if (doc != null && (count = getCharacterCount()) != 0) {
            StringBuilder text = new StringBuilder(count);
            addToText(text, doc, new Span(0, count - 1));
            return text.toString();
        }
        return null;
    }

    /**
     * Convenience method to get a portion of the document text into a single string.
     *
     * @param beginIndex The 0-based offset where to start retrieving text.
     * @param endIndex The ending offset + 1 of the text to retrieve.
     * @return The specified portion of the document text if there is any, or
     * {@code null} if there is no document.
     */
    public String getText(int beginIndex, int endIndex) {
        Utils.checkTwoIndexBounds(beginIndex, endIndex, 0, getCharacterCount());

        int count = endIndex - beginIndex;
        if (count == 0) {
            return "";
        }
        Document doc = getDocument();
        if (doc != null) {
            StringBuilder text = new StringBuilder(count);
            addToText(text, doc, new Span(beginIndex, endIndex - 1));
            return text.toString();
        }
        return null;
    }

    /**
     * Convenience method to create a text-only document consisting of one
     * paragraph per line of the given text.
     *
     * @param text The new complete text for the document.
     */
    public void setText(String text) {
        Utils.checkNull(text, "text");

        try {
            setText(new StringReader(text));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setText(URL textURL) throws IOException {
        Utils.checkNull(textURL, "text URL");

        try (InputStream inputStream = textURL.openStream()) {
            setText(new InputStreamReader(inputStream));
        }
    }

    public void setText(Reader textReader) throws IOException {
        Utils.checkNull(textReader, "Reader");

        int tabPosition = 0;
        int tabWidth = ((TextPane.Skin) getSkin()).getTabWidth();

        Document doc = new Document();
        StringBuilder text = new StringBuilder();

        int c = textReader.read();
        while (c != -1) {
            // Deal with the various forms of line endings:  CR only, LF only or CR,LF
            if (c == '\r') {
                int c2 = textReader.read();
                if (c2 == -1) {
                    break;
                } else if (c2 == '\n') {
                    // Only add the \n (the paragraph separator)
                    c = c2;
                } else {
                    // Change the paragraph separator to \n instead
                    // but push back the last character read
                    Paragraph paragraph = new Paragraph(text.toString());
                    doc.add(paragraph);
                    text.setLength(0);
                    tabPosition = 0;
                    c = c2;
                    continue;
                }
            }
            if (c == '\n') {
                Paragraph paragraph = new Paragraph(text.toString());
                doc.add(paragraph);
                text.setLength(0);
                tabPosition = 0;
            } else if (c == '\t') {
                if (expandTabs) {
                    int spaces = tabWidth - (tabPosition % tabWidth);
                    for (int i = 0; i < spaces; i++) {
                        text.append(' ');
                    }
                    tabPosition += spaces;
                } else {
                    text.append('\t');
                }
            } else {
                text.append((char) c);
                tabPosition++;
            }

            c = textReader.read();
        }

        if (text.length() != 0) {
            Paragraph paragraph = new Paragraph(text.toString());
            doc.add(paragraph);
        }

        setDocument(doc);
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
    public void setComposedText(AttributedStringCharacterIterator composedText) {
        this.composedText = composedText;
    }

    /**
     * @return The starting index of the selection.
     */
    public int getSelectionStart() {
        return selectionStart;
    }

    /**
     * @return The length of the selection; may be <tt>0</tt>.
     */
    public int getSelectionLength() {
        return selectionLength;
    }

    /**
     * Returns a span representing the current selection.
     *
     * @return A span containing the current selection. Both start and end
     * points are inclusive. Returns <tt>null</tt> if the selection is empty.
     */
    public Span getSelection() {
        return (selectionLength == 0) ? null : new Span(selectionStart, selectionStart
            + selectionLength - 1);
    }

    /**
     * Returns a character span (start, length) representing the current selection.
     *
     * @return A char span with the start and length values.
     */
    public CharSpan getCharSelection() {
        return new CharSpan(selectionStart, selectionLength);
    }

    /**
     * Sets the selection. The sum of the selection start and length must be
     * less than the length of the text input's content.
     *
     * @param selectionStart The starting index of the selection.
     * @param selectionLength The length of the selection.
     */
    public void setSelection(int selectionStart, int selectionLength) {
        checkDocumentExists();

        Utils.checkNonNegative(selectionLength, "selectionLength");

        int composedTextLength = composedText != null ? (composedText.getEndIndex() - composedText.getBeginIndex()) : 0;
        indexBoundsCheck("selectionStart", selectionStart, 0, document.getCharacterCount() - 1 + composedTextLength);

        if (selectionStart + selectionLength > document.getCharacterCount() + composedTextLength) {
            throw new IndexOutOfBoundsException("selectionStart=" + selectionStart
                + ", selectionLength=" + selectionLength + ", document.characterCount="
                + document.getCharacterCount() + ", composedTextLength=" + composedTextLength);
        }

        int previousSelectionStart = this.selectionStart;
        int previousSelectionLength = this.selectionLength;

        if (previousSelectionStart != selectionStart || previousSelectionLength != selectionLength) {
            this.selectionStart = selectionStart;
            this.selectionLength = selectionLength;

            textPaneSelectionListeners.selectionChanged(this, previousSelectionStart, previousSelectionLength);
        }
    }

    /**
     * Sets the selection.
     *
     * @param selection The new span describing the selection.
     * @see #setSelection(int, int)
     * @throws IllegalArgumentException if the span is {@code null}.
     */
    public final void setSelection(Span selection) {
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
    public final void setSelection(CharSpan selection) {
        Utils.checkNull(selection, "selection");

        setSelection(selection.start, selection.length);
    }

    /**
     * Change the selection to the given location (and length 0),
     * with checks to make sure the selection doesn't go out of bounds.
     * <p> Meant to be called from {@link #undo} (that is, internally).
     *
     * @param start The new selection start to check and set.
     * @see #setSelection(int, int)
     */
    private void changeSelection(int start) {
        int docCount = document.getCharacterCount();
        if (start >= 0) {
            if (start >= docCount) {
                setSelection(docCount - 1, 0);
            } else {
                setSelection(start, 0);
            }
        } else {
            setSelection(0, 0);
        }
    }

    /**
     * Selects all text.
     */
    public void selectAll() {
        checkDocumentNull();

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
     * @return A new string containing a copy of the text in the selected range,
     * or <tt>null</tt> if nothing is selected.
     */
    public String getSelectedText() {
        return selectionLength > 0 ? getText(selectionStart, selectionStart + selectionLength) : null;
    }

    /**
     * @return The text pane's editable flag.
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Sets the text pane's editable flag.
     *
     * @param editable Whether or not the text should be editable now.
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

    public boolean getExpandTabs() {
        return expandTabs;
    }

    /**
     * Sets whether tab characters (<code>\t</code>) are expanded to an
     * appropriate number of spaces during {@link #setText} and
     * {@link #paste} operations.  Note: doing this for keyboard input
     * is handled in the skin.
     *
     * @param expandTabs <code>true</code> to replace tab characters with space
     * characters (depending on the setting of the
     * {@link TextPane.Skin#getTabWidth} value) or <code>false</code> to leave
     * tabs alone. Note: this only affects tabs encountered during program
     * operations; tabs entered via the keyboard by the user are always
     * expanded, regardless of this setting.
     */
    public void setExpandTabs(boolean expandTabs) {
        this.expandTabs = expandTabs;
    }

    public int getInsertionPoint(int x, int y) {
        TextPane.Skin textPaneSkin = (TextPane.Skin) getSkin();
        return textPaneSkin.getInsertionPoint(x, y);
    }

    public int getNextInsertionPoint(int x, int from, ScrollDirection direction) {
        TextPane.Skin textPaneSkin = (TextPane.Skin) getSkin();
        return textPaneSkin.getNextInsertionPoint(x, from, direction);
    }

    public int getRowAt(int offset) {
        TextPane.Skin textPaneSkin = (TextPane.Skin) getSkin();
        return textPaneSkin.getRowAt(offset);
    }

    public int getRowCount() {
        TextPane.Skin textPaneSkin = (TextPane.Skin) getSkin();
        return textPaneSkin.getRowCount();
    }

    public Bounds getCharacterBounds(int offset) {
        // We need to validate in case we get called from user-code after
        // a user-code initiated modification, but before another layout has run.
        validate();
        TextPane.Skin textPaneSkin = (TextPane.Skin) getSkin();
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

    private static final String FORMAT = "%1$s%2$s: doc=%3$d, count=%4$d%n";

    public static void dumpNode(String msg, Node node, int indent) {
        if (msg != null && !msg.isEmpty()) {
            System.out.println(msg + ":");
        }
        String indenting = StringUtils.fromNChars(' ', indent * 2);
        System.out.format(FORMAT, indenting, node.getClass().getSimpleName(),
            node.getDocumentOffset(), node.getCharacterCount());
        if (node instanceof Element) {
            for (Node n : (Element) node) {
                dumpNode("", n, indent + 1);
            }
        }
    }
}
