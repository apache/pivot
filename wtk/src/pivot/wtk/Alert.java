/*
 * Copyright (c) 2008 VMware, Inc.
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

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.util.ListenerList;
import pivot.util.Resources;

public class Alert extends Dialog {
    public enum Type {
        ERROR,
        WARNING,
        QUESTION,
        INFO,
        APPLICATION;

        public static Type decode(String value) {
            return valueOf(value.toUpperCase());
        }
    }

    private static class AlertListenerList extends ListenerList<AlertListener>
        implements AlertListener {
        public void selectedOptionChanged(Alert alert, int previousSelectedOption) {
            for (AlertListener listener : this) {
                listener.selectedOptionChanged(alert, previousSelectedOption);
            }
        }
    }

    private Type type = null;
    private String message = null;
    private Component body = null;
    private Sequence<?> options = null;
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

    public Alert(Type type, String message, Sequence<?> options) {
        this(type, message, options, null);
    }

    public Alert(Type type, String message, Sequence<?> options, Component body) {
        if (type == null) {
            throw new IllegalArgumentException("type is null.");
        }

        if (message == null) {
            throw new IllegalArgumentException("message is null.");
        }

        if (options == null) {
            throw new IllegalArgumentException("options is null.");
        }

        this.type = type;
        this.message = message;
        this.options = options;
        this.body = body;

        installSkin(Alert.class);
    }

    public Type getType() {
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
        alert(Type.INFO, message, display, null);
    }

    public static void alert(String message, Display display,
        DialogResultListener dialogResultListener) {
        alert(Type.INFO, message, display, dialogResultListener);
    }

    public static void alert(Type type, String message, Display display) {
        alert(type, message, display, null);
    }

    public static void alert(Type type, String message, Display display,
        DialogResultListener dialogResultListener) {
        Alert alert = createAlert(type, message);
        alert.open(display, dialogResultListener);
    }

    public static void alert(String message, Window owner) {
        alert(Type.INFO, message, owner, null);
    }

    public static void alert(String message, Window owner,
        DialogResultListener dialogResultListener) {
        alert(Type.INFO, message, owner, dialogResultListener);
    }

    public static void alert(Type type, String message, Window owner) {
        alert(type, message, owner, null);
    }

    public static void alert(Type type, String message, Window owner,
        DialogResultListener dialogResultListener) {
        Alert alert = createAlert(type, message);
        alert.open(owner, dialogResultListener);
    }

    private static Alert createAlert(Type type, String message) {
        List<Object> options = new ArrayList<Object>();
        options.add(resources.get("defaultOption"));

        Alert alert = new Alert(type, message, options, null);
        alert.setTitle((String)resources.get("defaultTitle"));
        alert.setSelectedOption(0);

        return alert;
    }
}
