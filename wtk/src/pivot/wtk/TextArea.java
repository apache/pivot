/*
 * Copyright (c) 2009 VMware, Inc.
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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import pivot.serialization.SerializationException;
import pivot.util.ListenerList;
import pivot.wtk.media.Image;
import pivot.wtk.text.Document;
import pivot.wtk.text.Element;
import pivot.wtk.text.ImageNode;
import pivot.wtk.text.Node;
import pivot.wtk.text.NodeListener;
import pivot.wtk.text.Paragraph;
import pivot.wtk.text.PlainTextSerializer;
import pivot.wtk.text.TextNode;

/**
 * Component that allows a user to enter and edit multiple lines of (optionally
 * formatted) text.
 *
 * @author gbrown
 */
public class TextArea extends Component {
    /**
     * Text area skin interface. Text area skins are required to implement
     * this.
     *
     * @author gbrown
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
        public void documentChanged(TextArea textArea, Document previousText) {
            for (TextAreaListener listener : this) {
                listener.documentChanged(textArea, previousText);
            }
        }
    }

    private static class TextAreaSelectionListenerList extends ListenerList<TextAreaSelectionListener>
        implements TextAreaSelectionListener {
        public void selectionChanged(TextArea textArea,
            int previousSelectionStart, int previousSelectionLength) {
            for (TextAreaSelectionListener listener : this) {
                listener.selectionChanged(textArea, previousSelectionStart, previousSelectionLength);
            }
        }
    }

    private Document document = null;

    private int selectionStart = 0;
    private int selectionLength = 0;

    private NodeListener documentListener = new NodeListener() {
        public void parentChanged(Node node, Element previousParent) {
        }

        public void offsetChanged(Node node, int previousOffset) {
        }

        public void rangeInserted(Node node, int offset, int characterCount) {
            int previousSelectionStart = selectionStart;
            int previousSelectionLength = selectionLength;

            if (selectionStart + selectionLength > offset) {
                if (selectionStart > offset) {
                    selectionStart += characterCount;
                } else {
                    selectionLength += characterCount;
                }
            }

            if (previousSelectionStart != selectionStart
                || previousSelectionLength != selectionLength) {
                textAreaSelectionListeners.selectionChanged(TextArea.this,
                    previousSelectionStart, previousSelectionLength);
            }
        }

        public void rangeRemoved(Node node, int offset, int characterCount) {
            int previousSelectionStart = selectionStart;
            int previousSelectionLength = selectionLength;

            if (selectionStart + selectionLength > offset) {
                if (selectionStart > offset) {
                    selectionStart -= characterCount;
                } else {
                    selectionLength -= characterCount;
                }
            }

            if (previousSelectionStart != selectionStart
                || previousSelectionLength != selectionLength) {
                textAreaSelectionListeners.selectionChanged(TextArea.this,
                    previousSelectionStart, previousSelectionLength);
            }
        }
    };

    private TextAreaListenerList textAreaListeners = new TextAreaListenerList();
    private TextAreaSelectionListenerList textAreaSelectionListeners = new TextAreaSelectionListenerList();

    public TextArea() {
        installSkin(TextArea.class);
    }

    @Override
    protected void setSkin(pivot.wtk.Skin skin) {
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
            } catch(IOException exception) {
            }
        }

        return text;
    }

    public void setText(String text) {
        Document document = null;

        if (text != null) {
            try {
                PlainTextSerializer serializer = new PlainTextSerializer();
                StringReader reader = new StringReader(text);
                document = (Document)serializer.readObject(reader);
            } catch(SerializationException exception) {
            } catch(IOException exception) {
            }
        }

        setDocument(document);
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
            document.removeRange(selectionStart, selectionLength);
        }

        Node descendant = document.getDescendantAt(selectionStart);
        Element parent = descendant.getParent();

        Element range = (Element)parent.duplicate(false);
        range.add(new TextNode(text));

        int offset = selectionStart - parent.getOffset();
        parent.insertRange(range, offset);

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

        Node descendant = document.getDescendantAt(selectionStart);
        Element parent = descendant.getParent();

        Element range = (Element)parent.duplicate(false);
        range.add(new ImageNode(image));

        int offset = selectionStart - parent.getOffset();
        parent.insertRange(range, offset);

        // Set the selection start to the character following the insertion
        setSelection(selectionStart + 1, selectionLength);
    }

    public void insertParagraph() {
        // TODO Add a flag indicating if the paragraph should be added inline
        // or as a top-level element?

        if (document == null) {
            throw new IllegalStateException("No document.");
        }

        if (selectionLength > 0) {
            document.removeRange(selectionStart, selectionLength);
        }

        Document range = new Document();
        Paragraph paragraph = new Paragraph();
        range.add(paragraph);
        document.insertRange(range, selectionStart);

        // Set the selection start to the character following the insertion
        setSelection(selectionStart + paragraph.getCharacterCount(), selectionLength);
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

            switch (direction) {
                case FORWARD: {
                    offset++;
                    break;
                }

                case BACKWARD: {
                    offset--;
                    break;
                }
            }

            if (offset >= 0
                && offset < document.getCharacterCount()) {
                document.removeRange(offset, 1);
            }
        }
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

    public ListenerList<TextAreaSelectionListener> getTextAreaSelectionListeners() {
        return textAreaSelectionListeners;
    }
}
