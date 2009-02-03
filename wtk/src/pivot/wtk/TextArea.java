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

    private Document document = null;

    private TextAreaListenerList textAreaListeners = new TextAreaListenerList();

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

    public ListenerList<TextAreaListener> getTextAreaListeners() {
        return textAreaListeners;
    }
}
