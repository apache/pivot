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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Resources;

/**
 * Class representing a "prompt", a sheet commonly used to facilitate simple
 * user interaction.
 */
public class Prompt extends Sheet {
    /**
     * Option sequence implementation.
     */
    public final class OptionSequence implements Sequence<Object>, Iterable<Object> {
        private OptionSequence() {
        }

        @Override
        public int add(Object option) {
            int i = getLength();
            insert(option, i);

            return i;
        }

        @Override
        public void insert(Object option, int index) {
            if (option == null) {
                throw new IllegalArgumentException("option is null.");
            }

            options.insert(option, index);

            if (selectedOption >= index) {
                selectedOption++;
            }

            promptListeners.optionInserted(Prompt.this, index);
        }

        @Override
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
                if (selectedOption >= index) {
                    if (selectedOption < index + count) {
                        selectedOption = -1;
                    } else {
                        selectedOption -= count;
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
            return new ImmutableIterator<Object>(options.iterator());
        }
    }

    private static class PromptListenerList extends ListenerList<PromptListener>
        implements PromptListener {
        @Override
        public void messageTypeChanged(Prompt prompt, MessageType previousMessageType) {
            for (PromptListener listener : this) {
                listener.messageTypeChanged(prompt, previousMessageType);
            }
        }

        @Override
        public void messageChanged(Prompt prompt, String previousMessage) {
            for (PromptListener listener : this) {
                listener.messageChanged(prompt, previousMessage);
            }
        }

        @Override
        public void bodyChanged(Prompt prompt, Component previousBody) {
            for (PromptListener listener : this) {
                listener.bodyChanged(prompt, previousBody);
            }
        }

        @Override
        public void optionInserted(Prompt prompt, int index) {
            for (PromptListener listener : this) {
                listener.optionInserted(prompt, index);
            }
        }

        @Override
        public void optionsRemoved(Prompt prompt, int index, Sequence<?> removed) {
            for (PromptListener listener : this) {
                listener.optionsRemoved(prompt, index, removed);
            }
        }

        @Override
        public void selectedOptionChanged(Prompt prompt, int previousSelectedOption) {
            for (PromptListener listener : this) {
                listener.selectedOptionChanged(prompt, previousSelectedOption);
            }
        }
    }

    private MessageType messageType = null;
    private String message = null;
    private Component body = null;

    private ArrayList<Object> options = new ArrayList<Object>();
    private OptionSequence optionSequence = new OptionSequence();
    private int selectedOption = -1;

    private PromptListenerList promptListeners = new PromptListenerList();

    private static Resources resources = null;

    static {
        try {
            resources = new Resources(Prompt.class.getName());
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public Prompt() {
        this(null, null, null);
    }

    public Prompt(MessageType type, String message, Sequence<?> options) {
        this(type, message, options, null);
    }

    public Prompt(MessageType type, String message, Sequence<?> options, Component body) {
        if (messageType == null) {
            messageType = MessageType.INFO;
        }

        setMessageType(messageType);
        setMessage(message);
        setBody(body);

        if (options != null
            && options.getLength() > 0) {
            for (int i = 0, n = options.getLength(); i < n; i++) {
                optionSequence.add(options.get(i));
            }

            setSelectedOption(0);
        }

        setTitle((String)resources.get("defaultTitle"));

        installThemeSkin(Prompt.class);
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        if (messageType == null) {
            throw new IllegalArgumentException();
        }

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

    public void setOptions(String options) {
        optionSequence.remove(0, optionSequence.getLength());

        try {
            for (Object option : JSONSerializer.parseList(options)) {
                optionSequence.add(option);
            }
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(int selectedOption) {
        if (selectedOption < -1
            || selectedOption > options.getLength() - 1) {
            throw new IndexOutOfBoundsException();
        }

        int previousSelectedOption = this.selectedOption;

        if (selectedOption != previousSelectedOption) {
            this.selectedOption = selectedOption;
            promptListeners.selectedOptionChanged(this, previousSelectedOption);
        }
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

    public static void prompt(MessageType messageType, String message, Component body, Window owner,
        SheetCloseListener sheetCloseListener) {
        Prompt prompt = new Prompt(messageType, message,
            new ArrayList<Object>(resources.get("defaultOption")), body);
        prompt.open(owner, sheetCloseListener);
    }
}
