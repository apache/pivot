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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.util.ListenerList;


/**
 * Component that displays a string of text.
 */
public class Label extends Component {
    private static class LabelListenerList extends ListenerList<LabelListener>
        implements LabelListener {
        @Override
        public void textChanged(Label label, String previousText) {
            for (LabelListener listener : this) {
                listener.textChanged(label, previousText);
            }
        }

        @Override
        public void textKeyChanged(Label label, String previousTextKey) {
            for (LabelListener listener : this) {
                listener.textKeyChanged(label, previousTextKey);
            }
        }
    }

    private String text = null;
    private String textKey = null;
    private LabelListenerList labelListeners = new LabelListenerList();

    public Label() {
        this(null);
    }

    public Label(String text) {
        this.text = text;

        installSkin(Label.class);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        String previousText = this.text;
        if (previousText != text) {
            this.text = text;
            labelListeners.textChanged(this, previousText);
        }
    }

    /**
     * Returns the label's text key.
     *
     * @return
     * The text key, or <tt>null</tt> if no text key is set.
     */
    public String getTextKey() {
        return textKey;
    }

    /**
     * Sets the label's text key.
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
            labelListeners.textKeyChanged(this, previousTextKey);
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

    public ListenerList<LabelListener> getLabelListeners() {
        return labelListeners;
    }
}
