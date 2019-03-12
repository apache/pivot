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
 * Class representing a "prompt", a sheet commonly used to facilitate simple
 * user interaction.
 */
@DefaultProperty("body")
public class Prompt extends Sheet {
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

            promptListeners.optionInserted(Prompt.this, index);
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

                promptListeners.optionsRemoved(Prompt.this, index, removed);
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

    private PromptListener.Listeners promptListeners = new PromptListener.Listeners();

    private static Resources resources = null;

    static {
        try {
            resources = new Resources(Prompt.class.getName());
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public Prompt() {
        this(null, null, null);
    }

    public Prompt(String message) {
        this(null, message, null, null);
    }

    public Prompt(MessageType messageType, String message, Sequence<?> options) {
        this(messageType, message, options, null);
    }

    public Prompt(MessageType messageType, String message, Sequence<?> options, Component body) {
        setMessageType((messageType == null) ? MessageType.INFO : messageType);
        setMessage(message);
        setOptions((options == null) ? new ArrayList<>(resources.get("defaultOption")) : options);
        setBody(body);

        installSkin(Prompt.class);
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        Utils.checkNull(messageType, "messageType");

        MessageType previousMessageType = this.messageType;
        if (previousMessageType != messageType) {
            this.messageType = messageType;
            promptListeners.messageTypeChanged(this, previousMessageType);
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        String previousMessage = this.message;
        if (previousMessage != message) {
            this.message = message;
            promptListeners.messageChanged(this, previousMessage);
        }
    }

    public Component getBody() {
        return body;
    }

    public void setBody(Component body) {
        Component previousBody = this.body;
        if (previousBody != body) {
            this.body = body;
            promptListeners.bodyChanged(this, previousBody);
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
            promptListeners.selectedOptionChanged(this, previousSelectedOption);
        }
    }

    public Object getSelectedOption() {
        return (selectedOptionIndex == -1) ? null : options.get(selectedOptionIndex);
    }

    public void setSelectedOption(Object selectedOption) {
        setSelectedOptionIndex(options.indexOf(selectedOption));
    }

    public ListenerList<PromptListener> getPromptListeners() {
        return promptListeners;
    }

    public static void prompt(String message, Window owner) {
        prompt(MessageType.INFO, message, null, owner, null);
    }

    public static void prompt(MessageType messageType, String message, Window owner) {
        prompt(messageType, message, null, owner, null);
    }

    public static void prompt(MessageType messageType, String message, Window owner,
        SheetCloseListener sheetCloseListener) {
        prompt(messageType, message, null, owner, sheetCloseListener);
    }

    public static void prompt(MessageType messageType, String message, Component body, Window owner) {
        prompt(messageType, message, body, owner, null);
    }

    public static void prompt(MessageType messageType, String message, Component body,
        Window owner, SheetCloseListener sheetCloseListener) {
        Prompt prompt = new Prompt(messageType, message, null, body);
        prompt.open(owner, sheetCloseListener);
    }
}
