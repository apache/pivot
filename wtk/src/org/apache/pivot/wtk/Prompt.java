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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Resources;


/**
 * Class representing an "prompt", a sheet commonly used to perform simple
 * user interaction.
 * <p>
 * <tt>Prompt</tt> is a semantic sibling of <tt>Alert</tt>, but whereas
 * alerts are dialogs, prompts are sheets, meaning that an alert will be modal
 * over its entire owner hierarchy (its entire "application", in common usage)
 * but a prompt will be modal only over its owner's content.
 *
 * @author tvolkert
 */
public class Prompt extends Sheet {
    private static class PromptListenerList extends ListenerList<PromptListener>
        implements PromptListener {
        public void selectedOptionChanged(Prompt prompt, int previousSelectedOption) {
            for (PromptListener listener : this) {
                listener.selectedOptionChanged(prompt, previousSelectedOption);
            }
        }
    }

    private MessageType type = null;
    private String message = null;
    private Component body = null;
    private Sequence<?> options = null;
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

    public Prompt(MessageType type, String message, Sequence<?> options) {
        this(type, message, options, null);
    }

    public Prompt(MessageType type, String message, Sequence<?> options, Component body) {
        if (type == null) {
            throw new IllegalArgumentException("type is null.");
        }

        if (options == null) {
            throw new IllegalArgumentException("options is null.");
        }

        this.type = type;
        this.message = message;
        this.options = options;
        this.body = body;

        installSkin(Prompt.class);
    }

    public MessageType getMessageType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Object getOption(int index) {
        return options.get(index);
    }

    public int getOptionCount() {
        return options.getLength();
    }

    public Component getBody() {
        return body;
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(int selectedOption) {
        if (selectedOption < -1
            || selectedOption >= options.getLength()) {
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
        prompt(MessageType.INFO, message, owner, null);
    }

    public static void prompt(String message, Window owner,
        SheetStateListener sheetStateListener) {
        prompt(MessageType.INFO, message, owner, sheetStateListener);
    }

    public static void prompt(MessageType type, String message, Window owner) {
        prompt(type, message, owner, null);
    }

    public static void prompt(MessageType type, String message, Window owner,
        SheetStateListener sheetStateListener) {
        Prompt prompt = createPrompt(type, message);
        prompt.open(owner, sheetStateListener);
    }

    private static Prompt createPrompt(MessageType type, String message) {
        List<Object> options = new ArrayList<Object>();
        options.add(resources.get("defaultOption"));

        Prompt prompt = new Prompt(type, message, options, null);
        prompt.setTitle((String)resources.get("defaultTitle"));
        prompt.setSelectedOption(0);

        return prompt;
    }
}
