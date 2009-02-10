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
import pivot.wtk.text.Document;
import pivot.wtk.text.PlainTextSerializer;

/**
 * Component that allows a user to enter and edit multiple lines of (optionally
 * formatted) text.
 *
 * @author gbrown
 */
public class TextArea extends Component {
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

    private TextAreaListenerList textAreaListeners = new TextAreaListenerList();
    private TextAreaSelectionListenerList textAreaSelectionListeners = new TextAreaSelectionListenerList();

    public TextArea() {
        installSkin(TextArea.class);
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
            this.document = document;
            textAreaListeners.documentChanged(this, previousDocument);

            // TODO We need to be notified of character insertions/removals so
            // we can update the selection state; define a
            // pivot.wtk.text.DocumentListener interface that will fire events
            // when rangeInserted() and rangeRemoved() are called
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
        if (document == null
            || selectionStart < 0
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


    public ListenerList<TextAreaListener> getTextAreaListeners() {
        return textAreaListeners;
    }

    public ListenerList<TextAreaSelectionListener> getTextAreaSelectionListeners() {
        return textAreaSelectionListeners;
    }
}
