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

import org.apache.pivot.json.JSON;
import org.apache.pivot.util.ListenerList;

/**
 * Component that displays a string of text.
 */
public class Label extends Component {
    /**
     * Translates between text and context data during data binding.
     */
    public interface TextBindMapping {
        /**
         * Converts a value from the bind context to a text representation
         * during a {@link Component#load(Object)} operation.
         *
         * @param value
         */
        public String toString(Object value);

        /**
         * Converts a text string to a value to be stored in the bind context
         * during a {@link Component#store(Object)} operation.
         *
         * @param text
         */
        public Object valueOf(String text);
    }

    private static class LabelListenerList extends WTKListenerList<LabelListener>
        implements LabelListener {
        @Override
        public void textChanged(Label label, String previousText) {
            for (LabelListener listener : this) {
                listener.textChanged(label, previousText);
            }
        }

        @Override
        public void maximumLengthChanged(Label label, int previousMaximumLength) {
            for (LabelListener listener : this) {
                listener.maximumLengthChanged(label, previousMaximumLength);
            }
        }

    }

    private static class LabelBindingListenerList extends WTKListenerList<LabelBindingListener>
        implements LabelBindingListener {
        @Override
        public void textKeyChanged(Label label, String previousTextKey) {
            for (LabelBindingListener listener : this) {
                listener.textKeyChanged(label, previousTextKey);
            }
        }

        @Override
        public void textBindTypeChanged(Label label, BindType previousTextBindType) {
            for (LabelBindingListener listener : this) {
                listener.textBindTypeChanged(label, previousTextBindType);
            }
        }

        @Override
        public void textBindMappingChanged(Label label, Label.TextBindMapping previousTextBindMapping) {
            for (LabelBindingListener listener : this) {
                listener.textBindMappingChanged(label, previousTextBindMapping);
            }
        }
    }

    private String text = null;
    private int maximumLength = 32767;

    private String textKey = null;
    private BindType textBindType = BindType.BOTH;
    private TextBindMapping textBindMapping = null;

    private LabelListenerList labelListeners = new LabelListenerList();
    private LabelBindingListenerList labelBindingListeners = new LabelBindingListenerList();

    public Label() {
        this("");
    }

    public Label(String text) {
        setText(text);

        installSkin(Label.class);
    }

    /**
     * Returns the label's text.
     *
     * @return
     * The text.
     */
    public String getText() {
        return text;
    }

    /**
     * Set the text of the Label.
     *
     * @param text
     * The text to set, must be not null.
     */
    public void setText(String text) {
        if (text == null) {
            throw new IllegalArgumentException();
        }

        if (text.length() > maximumLength) {
            throw new IllegalArgumentException("Text length is greater than maximum length.");
        }

       String previousText = this.text;
        if (previousText != text) {
            this.text = text;
            labelListeners.textChanged(this, previousText);
        }
    }

    /**
     * Utility method to set text to the given value,
     * or to an empty string if null (to avoid the setText throw an IllegalArgumentException).
     * This is useful to be called by code.
     *
     * @param text
     * The text to set
     *
     * @see #setText
     */
    public void setTextOrEmpty(String text) {
        this.setText(text != null ? text : "");
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
     * Returns the maximum length of the label text.
     *
     * @return
     * The maximum length of the label text.
     */
    public int getMaximumLength() {
        return maximumLength;
    }

    /**
     * Sets the maximum length of the label text.
     *
     * @param maximumLength
     * The maximum length of the label text.
     */
    public void setMaximumLength(int maximumLength) {
        if (maximumLength < 0) {
            throw new IllegalArgumentException("maximumLength is negative.");
        }

        int previousMaximumLength = this.maximumLength;
        if (previousMaximumLength != maximumLength) {
            this.maximumLength = maximumLength;

            // Truncate the text, if necessary (do not allow listeners to vote on this change)
            if (text.length() > maximumLength) {
                setText(text.substring(0, maximumLength));
            }

            labelListeners.maximumLengthChanged(this, previousMaximumLength);
        }
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
            labelBindingListeners.textKeyChanged(this, previousTextKey);
        }
    }

    public BindType getTextBindType() {
        return textBindType;
    }

    public void setTextBindType(BindType textBindType) {
        if (textBindType == null) {
            throw new IllegalArgumentException();
        }

        BindType previousTextBindType = this.textBindType;

        if (previousTextBindType != textBindType) {
            this.textBindType = textBindType;
            labelBindingListeners.textBindTypeChanged(this, previousTextBindType);
        }
    }

    public TextBindMapping getTextBindMapping() {
        return textBindMapping;
    }

    public void setTextBindMapping(TextBindMapping textBindMapping) {
        TextBindMapping previousTextBindMapping = this.textBindMapping;

        if (previousTextBindMapping != textBindMapping) {
            this.textBindMapping = textBindMapping;
            labelBindingListeners.textBindMappingChanged(this, previousTextBindMapping);
        }
    }

    @Override
    public void load(Object context) {
        if (textKey != null
            && JSON.containsKey(context, textKey)
            && textBindType != BindType.STORE) {
            Object value = JSON.get(context, textKey);

            if (textBindMapping == null) {
                value = (value == null) ? null : value.toString();
            } else {
                value = textBindMapping.toString(value);
            }

            setText(value != null ? (String)value : "");
        }
    }

    @Override
    public void store(Object context) {
        if (textKey != null
            && textBindType != BindType.LOAD) {
            String textLocal = getText();
            JSON.put(context, textKey, (textBindMapping == null) ?
                textLocal : textBindMapping.valueOf(textLocal));
        }
    }

    @Override
    public void clear() {
        if (textKey != null) {
            setText("");
        }
    }

    public ListenerList<LabelListener> getLabelListeners() {
        return labelListeners;
    }

    public ListenerList<LabelBindingListener> getLabelBindingListeners() {
        return labelBindingListeners;
    }

    @Override
    public String toString() {
        return getClass().getName() + " [\"" + getText() + "\"]";
    }

}
