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
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.util.ListenerList;

/**
 * Component that displays a string of text.
 */
public class Label extends Component {
    /**
     * Translates between text and context data during data binding.
     */
    public interface BindMapping {
        /**
         * Converts a value from the bind context to a text representation.
         *
         * @param value
         */
        public String toString(Object value);

        /**
         * Converts a text string to a value to be stored in the bind context.
         *
         * @param text
         */
        public Object valueOf(String text);
    }

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

        @Override
        public void bindMappingChanged(Label label, Label.BindMapping previousBindMapping) {
            for (LabelListener listener : this) {
                listener.bindMappingChanged(label, previousBindMapping);
            }
        }
    }

    private String text = null;
    private String textKey = null;
    private BindMapping bindMapping = null;

    private LabelListenerList labelListeners = new LabelListenerList();

    public Label() {
        this(null);
    }

    public Label(String text) {
        this.text = text;

        installThemeSkin(Label.class);
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

        if (previousTextKey != textKey) {
            this.textKey = textKey;
            labelListeners.textKeyChanged(this, previousTextKey);
        }
    }

    public BindMapping getBindMapping() {
        return bindMapping;
    }

    public void setBindMapping(BindMapping bindMapping) {
        BindMapping previousBindMapping = this.bindMapping;

        if (previousBindMapping != bindMapping) {
            this.bindMapping = bindMapping;
            labelListeners.bindMappingChanged(this, previousBindMapping);
        }
    }

    @Override
    public void load(Dictionary<String, ?> context) {
        if (textKey != null
            && JSONSerializer.containsKey(context, textKey)) {
            Object value = JSONSerializer.get(context, textKey);

            if (bindMapping == null
                && value != null) {
                value = value.toString();
            } else {
                value = bindMapping.toString(value);
            }

            setText((String)value);
        }
    }

    @Override
    public void store(Dictionary<String, ?> context) {
        if (isEnabled()
            && textKey != null) {
            String text = getText();
            JSONSerializer.put(context, textKey, (bindMapping == null) ?
                text : bindMapping.valueOf(text));
        }
    }

    @Override
    public void clear() {
        if (textKey != null) {
            setText(null);
        }
    }

    public ListenerList<LabelListener> getLabelListeners() {
        return labelListeners;
    }
}
