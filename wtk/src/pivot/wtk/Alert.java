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

import java.util.Comparator;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ListenerList;

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

    /**
     * List event handler.
     *
     * @author tvolkert
     */
    private class ListHandler implements ListListener<String> {
        public void itemInserted(List<String> list, int index) {
            insertOption(index);
        }

        public void itemsRemoved(List<String> list, int index, Sequence<String> items) {
            removeOptions(index, (items == null) ? -1 : items.getLength());
        }

        public void itemUpdated(List<String> list, int index, String previousItem) {
            alertOptionListeners.optionUpdated(Alert.this, index);
        }

        public void comparatorChanged(List<String> list,
                                      Comparator<String> previousComparator) {
            if (list.getComparator() != null) {
                alertOptionListeners.optionsSorted(Alert.this);
            }
        }
    }

    /**
     * Alert listener list.
     *
     * @author tvolkert
     */
    private class AlertListenerList
        extends ListenerList<AlertListener>
        implements AlertListener {
        public void typeChanged(Alert alert, Type previousType) {
            for (AlertListener listener : this) {
                listener.typeChanged(alert, previousType);
            }
        }

        public void subjectChanged(Alert alert, String previousSubject) {
            for (AlertListener listener : this) {
                listener.subjectChanged(alert, previousSubject);
            }
        }

        public void bodyChanged(Alert alert, Component previousBody) {
            for (AlertListener listener : this) {
                listener.bodyChanged(alert, previousBody);
            }
        }

        public void optionDataChanged(Alert alert, List<String> previousOptionData) {
            for (AlertListener listener : this) {
               listener.optionDataChanged(alert, previousOptionData);
            }
        }
    }

    /**
     * Alert option listener list.
     *
     * @author tvolkert
     */
    private class AlertOptionListenerList extends ListenerList<AlertOptionListener>
        implements AlertOptionListener {
        public void optionInserted(Alert alert, int index) {
            for (AlertOptionListener listener : this) {
                listener.optionInserted(alert, index);
            }
        }

        public void optionsRemoved(Alert alert, int index, int count) {
            for (AlertOptionListener listener : this) {
                listener.optionsRemoved(alert, index, count);
            }
        }

        public void optionUpdated(Alert alert, int index) {
            for (AlertOptionListener listener : this) {
                listener.optionUpdated(alert, index);
            }
        }

        public void optionsSorted(Alert alert) {
            for (AlertOptionListener listener : this) {
                listener.optionsSorted(alert);
            }
        }
    }

    private class AlertSelectionListenerList
        extends ListenerList<AlertSelectionListener>
        implements AlertSelectionListener {
        public void selectedOptionChanged(Alert alert, int previousSelectedOption) {
            for (AlertSelectionListener listener : this) {
                listener.selectedOptionChanged(alert, previousSelectedOption);
            }
        }
    }

    private Type type = null;
    private String subject = null;
    private Component body = null;
    private List<String> optionData = null;
    private int selectedOption = -1;

    private ListHandler listHandler = new ListHandler();

    private AlertListenerList alertListeners = new AlertListenerList();
    private AlertOptionListenerList alertOptionListeners =
        new AlertOptionListenerList();
    private AlertSelectionListenerList alertSelectionListeners =
        new AlertSelectionListenerList();

    public Alert(Type type, String subject) {
        this(type, subject, new ArrayList<String>());
    }

    public Alert(Type type, String subject, List<String> optionData) {
        super();

        if (optionData == null) {
            throw new IllegalArgumentException("No option data specified");
        }

        setType(type);
        setSubject(subject);
        setOptionData(optionData);

        installSkin(Alert.class);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        Type previousType = this.type;

        if (type != previousType) {
            this.type = type;
            alertListeners.typeChanged(this, previousType);
        }
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        String previousSubject = this.subject;

        if ((previousSubject == null ^ subject == null)
            || (subject != null && !subject.equals(previousSubject))) {
            this.subject = subject;
            alertListeners.subjectChanged(this, previousSubject);
        }
    }

    public Component getBody() {
        return body;
    }

    public void setBody(Component body) {
        Component previousBody = this.body;

        if (body != previousBody) {
            this.body = body;
            alertListeners.bodyChanged(this, previousBody);
        }
    }

    public List<String> getOptionData() {
        return optionData;
    }

    public void setOptionData(List<String> optionData) {
        if (optionData == null) {
            throw new IllegalArgumentException("No option data specified");
        }

        List<String> previousOptionData = this.optionData;

        if (optionData != previousOptionData) {
            if (previousOptionData != null) {
                // Clear any existing selection
                setSelectedOption(-1);

                previousOptionData.getListListeners().remove(listHandler);
            }

            optionData.getListListeners().add(listHandler);

            // Update the option data and fire change event
            this.optionData = optionData;
            alertListeners.optionDataChanged(this, previousOptionData);
        }
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(int selectedOption) {
        if (selectedOption < -1 || selectedOption >= optionData.getLength()) {
            throw new IndexOutOfBoundsException
                (selectedOption + " is not a valid selection.");
        }

        int previousSelectedOption = this.selectedOption;

        if (selectedOption != previousSelectedOption) {
            this.selectedOption = selectedOption;
            alertSelectionListeners.selectedOptionChanged(this, previousSelectedOption);
        }
    }

    /**
     * Inserts an option into the options and notifies option listeners that
     * an option was added to the list. Increments the selection option if it is
     * greater than or equal to the inserted index.
     *
     * @param option
     * The index of the option that was inserted.
     */
    protected void insertOption(int option) {
        int previousSelectedOption = selectedOption;

        if (selectedOption >= option) {
            selectedOption++;
        }

        // Notify listeners that option was inserted
        alertOptionListeners.optionInserted(this, option);

        if (previousSelectedOption != selectedOption) {
            // Notify selection listeners that the selection changed
            alertSelectionListeners.selectedOptionChanged(this, previousSelectedOption);
        }
    }

    /**
     * Notifies option listeners that an option was removed from the list. If
     * the selected option index is within the range of the removed indices,
     * the selection is cleared. Otherwise, the selection index is decremented.
     *
     * @param option
     * The index of the item that was removed.
     *
     * @param count
     * The count of items that were removed, or <tt>-1</tt> if all items were
     * removed.
     */
    protected void removeOptions(int option, int count) {
        int previousSelectedOption = selectedOption;

        if (selectedOption >= option) {
            selectedOption = (selectedOption < option + count) ?
                -1 : selectedOption - count;
        }

        // Notify listeners that options were removed
        alertOptionListeners.optionsRemoved(this, option, count);

        if (previousSelectedOption != selectedOption) {
            // Notify selection listeners that the selection changed
            alertSelectionListeners.selectedOptionChanged
                (this, previousSelectedOption);
        }
    }

    public ListenerList<AlertListener> getAlertListeners() {
        return alertListeners;
    }

    public ListenerList<AlertSelectionListener> getAlertSelectionListeners() {
        return alertSelectionListeners;
    }

    public ListenerList<AlertOptionListener> getAlertOptionListeners() {
        return alertOptionListeners;
    }

    public static void alert(Type type, String message) {
        alert(type, message, null);
    }

    public static void alert(Type type, String message, Window owner) {
        // TODO i18n
        List<String> optionData = new ArrayList<String>();
        optionData.add("OK");

        Alert alert = new Alert(type, message, optionData);
        alert.setTitle("Alert");
        alert.setSelectedOption(0);

        alert.open(owner);
    }
}
