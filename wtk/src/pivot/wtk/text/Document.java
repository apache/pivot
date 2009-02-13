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
package pivot.wtk.text;

import pivot.util.ListenerList;

/**
 * Node representing the root of an element hierarchy.
 *
 * @author gbrown
 */
public class Document extends Block {
    private static class DocumentListenerList extends ListenerList<DocumentListener>
        implements DocumentListener {
        public void rangeInserted(Document document, int offset, int span) {
            for (DocumentListener listener : this) {
                listener.rangeInserted(document, offset, span);
            }
        }

        public void rangeRemoved(Document document, int offset, int span) {
            for (DocumentListener listener : this) {
                listener.rangeRemoved(document, offset, span);
            }
        }
    }

    private DocumentListenerList documentListeners = new DocumentListenerList();

    public Document() {
        super();
    }

    public Document(Document document, boolean recursive) {
        super(document, recursive);

        // TODO?
    }

    @Override
    protected void setParent(Element parent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node duplicate(boolean recursive) {
        return new Document(this, recursive);
    }

    @Override
    public void insert(Node node, int index) {
        if (!(node instanceof Block)) {
            throw new IllegalArgumentException("Child node must be an instance of "
                + Block.class.getName());
        }

        super.insert(node, index);
    }

    @Override
    protected void rangeInserted(int offset, int characterCount) {
        super.rangeInserted(offset, characterCount);

        documentListeners.rangeInserted(this, offset, characterCount);
    }

    @Override
    protected void rangeRemoved(int offset, int characterCount) {
        super.rangeRemoved(offset, characterCount);

        documentListeners.rangeRemoved(this, offset, characterCount);
    }

    public ListenerList<DocumentListener> getDocumentListeners() {
        return documentListeners;
    }
}
