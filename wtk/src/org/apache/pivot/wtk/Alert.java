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
 * Class representing an "alert", a dialog commonly used to perform simple
 * user interaction.
 */
public class Alert extends Dialog {
    private static class AlertListenerList extends ListenerList<AlertListener>
        implements AlertListener {
        public void selectedOptionChanged(Alert alert, int previousSelectedOption) {
            for (AlertListener listener : this) {
                listener.selectedOptionChanged(alert, previousSelectedOption);
            }
        }
    }

    private MessageType type = null;
    private String message = null;
    private Component body = null;
    private ArrayList<Object> options = null;
    private int selectedOption = -1;

    private AlertListenerList alertListeners = new AlertListenerList();

    private static Resources resources = null;

    static {
        try {
            resources = new Resources(Alert.class.getName());
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public Alert(MessageType type, String message, Sequence<?> options) {
        this(type, message, options, null);
    }

    @SuppressWarnings("unchecked")
    public Alert(MessageType type, String message, Sequence<?> options, Component body) {
        if (type == null) {
            throw new IllegalArgumentException("type is null.");
        }

        if (options == null) {
            throw new IllegalArgumentException("options is null.");
        }

        this.type = type;
        this.message = message;
        this.options = new ArrayList<Object>((Sequence<Object>)options);
        this.body = body;

        installSkin(Alert.class);
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
            || selectedOption > options.getLength() - 1) {
            throw new IndexOutOfBoundsException();
        }

        int previousSelectedOption = this.selectedOption;

        if (selectedOption != previousSelectedOption) {
            this.selectedOption = selectedOption;
            alertListeners.selectedOptionChanged(this, previousSelectedOption);
        }
    }

    public ListenerList<AlertListener> getAlertListeners() {
        return alertListeners;
    }

    public static void alert(String message, Display display) {
        alert(MessageType.INFO, message, null, display, null);
    }

    public static void alert(MessageType messageType, String message, Display display) {
        alert(messageType, message, null, display, null);
    }

    public static void alert(MessageType messageType, String message, Component body, Display display) {
        alert(messageType, message, body, display, null);
    }

    public static void alert(MessageType messageType, String message, Component body, Display display,
        DialogCloseListener dialogCloseListener) {
        Alert alert = createAlert(messageType, message, body);
        alert.open(display, dialogCloseListener);
    }

    public static void alert(String message, Window owner) {
        alert(MessageType.INFO, message, null, owner, null);
    }

    public static void alert(MessageType messageType, String message, Window owner) {
        alert(messageType, message, null, owner, null);
    }

    public static void alert(MessageType messageType, String message, Component body, Window owner) {
        alert(messageType, message, body, owner, null);
    }

    public static void alert(MessageType messageType, String message, Component body, Window owner,
        DialogCloseListener dialogCloseListener) {
        Alert alert = createAlert(messageType, message, body);
        alert.open(owner, dialogCloseListener);
    }

    private static Alert createAlert(MessageType messageType, String message, Component body) {
        List<Object> options = new ArrayList<Object>();
        options.add(resources.get("defaultOption"));

        Alert alert = new Alert(messageType, message, options, body);
        alert.setTitle((String)resources.get("defaultTitle"));
        alert.setSelectedOption(0);

        return alert;
    }
}
