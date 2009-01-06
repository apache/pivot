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

import pivot.util.ListenerList;
import pivot.wtk.text.Document;

/**
 * Component that allows a user to enter and edit multiple lines of (optionally
 * formatted) text.
 *
 * @author gbrown
 */
public class TextArea extends Container {
    private static class TextAreaListenerList extends ListenerList<TextAreaListener>
        implements TextAreaListener {
        public void textChanged(TextArea textArea, Document previousText) {
            for (TextAreaListener listener : this) {
                listener.textChanged(textArea, previousText);
            }
        }
    }

    private Document text = null;

    private TextAreaListenerList textAreaListeners = new TextAreaListenerList();

    public TextArea() {
        installSkin(TextArea.class);
    }

    public Document getText() {
        return text;
    }

    public void setText(Document text) {
        Document previousText = this.text;

        if (previousText != text) {
            this.text = text;
            textAreaListeners.textChanged(this, previousText);
        }
    }

    public ListenerList<TextAreaListener> getTextAreaListeners() {
        return textAreaListeners;
    }
}
