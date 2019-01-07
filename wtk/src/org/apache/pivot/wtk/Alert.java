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

import java.util.Iterator;

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.Utils;

/**
 * Class representing an "alert", a dialog commonly used to facilitate simple
 * user interaction.
 */
@DefaultProperty("body")
public class Alert extends Dialog {
    /**
     * Option sequence implementation.
     */
    public final class OptionSequence implements Sequence<Object>, Iterable<Object> {
        private OptionSequence() {
        }

        @Override
        public int add(Object option) {
            int index = getLength();
            insert(option, index);

            return index;
        }

        @Override
        public void insert(Object option, int index) {
            Utils.checkNull(option, "option");

            options.insert(option, index);

            if (selectedOptionIndex >= index) {
                selectedOptionIndex++;
            }

            alertListeners.optionInserted(Alert.this, index);
        }

        @Override
        @UnsupportedOperation
        public Component update(int index, Object option) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(Object option) {
            int index = indexOf(option);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<Object> remove(int index, int count) {
            Sequence<Object> removed = options.remove(index, count);

            if (removed.getLength() > 0) {
                if (selectedOptionIndex >= index) {
                    if (selectedOptionIndex < index + count) {
                        selectedOptionIndex = -1;
                    } else {
                        selectedOptionIndex -= count;
                    }
                }

                alertListeners.optionsRemoved(Alert.this, index, removed);
            }

            return removed;
        }

        @Override
        public Object get(int index) {
            return options.get(index);
        }

        @Override
        public int indexOf(Object option) {
            return options.indexOf(option);
        }

        @Override
        public int getLength() {
            return options.getLength();
        }

        @Override
        public Iterator<Object> iterator() {
            return new ImmutableIterator<>(options.iterator());
        }
    }

    private MessageType messageType = null;
    private String message = null;
    private Component body = null;

    private ArrayList<Object> options = new ArrayList<>();
    private OptionSequence optionSequence = new OptionSequence();
    private int selectedOptionIndex = -1;

    private AlertListener.Listeners alertListeners = new AlertListener.Listeners();

    private static Resources resources = null;

    static {
        try {
            resources = new Resources(Alert.class.getName());
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public Alert() {
        this(null, "", null);
    }

    public Alert(String message) {
        this(null, message, null, true);
    }

    public Alert(MessageType messageType, String message, Sequence<?> options) {
        this(messageType, message, options, true);
    }

    public Alert(MessageType messageType, String message, Sequence<?> options, boolean modal) {
        this(messageType, message, options, null, modal);
    }

    public Alert(MessageType messageType, String message, Sequence<?> options, Component body) {
        this(messageType, message, options, body, true);
    }

    public Alert(MessageType messageType, String message, Sequence<?> options, Component body,
        boolean modal) {
        super(modal);

        setMessageType((messageType == null) ? MessageType.INFO : messageType);
        setMessage(message);
        setOptions((options == null) ? new ArrayList<>(resources.get("defaultOption")) : options);
        setBody(body);

        String titleKey = "defaultTitle";
        if (messageType != null) {
            switch (messageType) {
                case ERROR:
                    titleKey = "defaultErrorTitle";
                    break;
                case WARNING:
                    titleKey = "defaultWarningTitle";
                    break;
                case QUESTION:
                    titleKey = "defaultQuestionTitle";
                    break;
                case INFO:
                    titleKey = "defaultInfoTitle";
                    break;
                default:
                    break;
            }
        }
        setTitle(resources.getString(titleKey));

        installSkin(Alert.class);
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        Utils.checkNull(messageType, "messageType");

        MessageType previousMessageType = this.messageType;
        if (previousMessageType != messageType) {
            this.messageType = messageType;
            alertListeners.messageTypeChanged(this, previousMessageType);
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        String previousMessage = this.message;
        if (previousMessage != message) {
            this.message = message;
            alertListeners.messageChanged(this, previousMessage);
        }
    }

    public Component getBody() {
        return body;
    }

    public void setBody(Component body) {
        Component previousBody = this.body;
        if (previousBody != body) {
            this.body = body;
            alertListeners.bodyChanged(this, previousBody);
        }
    }

    public OptionSequence getOptions() {
        return optionSequence;
    }

    public void setOptions(Sequence<?> options) {
        optionSequence.remove(0, optionSequence.getLength());

        if (options != null) {
            for (int i = 0, n = options.getLength(); i < n; i++) {
                optionSequence.add(options.get(i));
            }

            setSelectedOptionIndex(0);
        }
    }

    public void setOptions(String options) {
        try {
            setOptions(JSONSerializer.parseList(options));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    public int getSelectedOptionIndex() {
        return selectedOptionIndex;
    }

    public void setSelectedOptionIndex(int selectedOption) {
        indexBoundsCheck("selectedOption", selectedOption, -1, options.getLength() - 1);

        int previousSelectedOption = this.selectedOptionIndex;

        if (selectedOption != previousSelectedOption) {
            this.selectedOptionIndex = selectedOption;
            alertListeners.selectedOptionChanged(this, previousSelectedOption);
        }
    }

    public Object getSelectedOption() {
        return (selectedOptionIndex == -1) ? null : options.get(selectedOptionIndex);
    }

    public void setSelectedOption(Object selectedOption) {
        setSelectedOptionIndex(options.indexOf(selectedOption));
    }

    public ListenerList<AlertListener> getAlertListeners() {
        return alertListeners;
    }

    public static void alert(String message, Window owner) {
        alert(MessageType.INFO, message, null, null, owner, null);
    }

    /**
     * An alert with a type of {@link MessageType#INFO}.
     *
     * @param message The main message of this alert.
     * @param owner   The owner window.
     * @param width   The preferred width of the alert.
     */
    public static void alert(String message, Window owner, int width) {
        alert(MessageType.INFO, message, null, null, owner, width, null);
    }

    public static void error(String message, Window owner) {
        alert(MessageType.ERROR, message, null, null, owner, null);
    }

    /**
     * An alert with a type of {@link MessageType#ERROR}.
     *
     * @param message The main message of this error.
     * @param owner   The owner window.
     * @param width   The preferred width of the alert.
     */
    public static void error(String message, Window owner, int width) {
        alert(MessageType.ERROR, message, null, null, owner, width, null);
    }

    public static void alert(MessageType messageType, String message, Window owner) {
        alert(messageType, message, null, null, owner, null);
    }

    public static void alert(MessageType messageType, String message, Window owner,
        DialogCloseListener dialogCloseListener) {
        alert(messageType, message, null, null, owner, dialogCloseListener);
    }

    public static void alert(MessageType messageType, String message, Component body, Window owner) {
        alert(messageType, message, null, body, owner, null);
    }

    public static void alert(MessageType messageType, String message, Component body, Window owner,
        int width) {
        alert(messageType, message, null, body, owner, width, null);
    }

    public static void alert(MessageType messageType, String message, Component body, Window owner,
        DialogCloseListener dialogCloseListener) {
        alert(messageType, message, null, body, owner, dialogCloseListener);
    }

    public static void alert(MessageType messageType, String message, String title, Component body,
        Window owner, DialogCloseListener dialogCloseListener) {
        alert(messageType, message, title, body, owner, -1, dialogCloseListener);
    }

    public static void alert(MessageType messageType, String message, String title, Component body,
        Window owner, int width, DialogCloseListener dialogCloseListener) {
        Alert alert = new Alert(messageType, message, null, body);

        if (title != null) {
            alert.setTitle(title);
        }
        if (width > 0) {
            alert.setPreferredWidth(width);
        }

        alert.open(owner.getDisplay(), owner, dialogCloseListener);
    }

}
