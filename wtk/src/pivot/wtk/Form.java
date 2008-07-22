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

import java.util.Iterator;

import pivot.beans.Bean;
import pivot.collections.ArrayList;
import pivot.collections.Map;
import pivot.collections.Sequence;
import pivot.serialization.JSONSerializer;
import pivot.util.ListenerList;
import pivot.wtk.Alert;

/**
 * A container that arranges field components in a form layout. Each field has
 * an optional text label associated with it and may be flagged as requiring
 * attention using one of several flag types and an optional flag message (for
 * use during form validation, for example).
 *
 * @author gbrown
 */
public class Form extends Container {
    /**
     * Represents an alert associated with a form field.
     *
     * @author gbrown
     */
    public static class Flag {
        /**
         * The flag's alert type.
         */
        private Alert.Type alertType = null;

        /**
         * The flag message. May be <tt>null</tt>.
         */
        private String message = null;

        public static final String ALERT_TYPE_KEY = "alertType";
        public static final String MESSAGE_KEY = "message";

        /**
         * Creates a new flag with the given alert type and no message.
         *
         * @param type
         * The type of the flag.
         */
        public Flag(Alert.Type alertType) {
            this(alertType, null);
        }

        /**
         * Creates a new flag with the given type and message.
         *
         * @param type
         * The type of the flag.
         *
         * @param message
         * The message text associated with the flag, or <tt>null</tt> for
         * no message.
         */
        public Flag(Alert.Type alertType, String message) {
            setFlag(alertType, message);
        }

        public Flag(String flag) {
            if (flag == null) {
                throw new IllegalArgumentException("flag is null.");
            }

            Map<String, Object> flagMap = JSONSerializer.parseMap(flag);

            Alert.Type alertType = null;
            if (flagMap.containsKey(ALERT_TYPE_KEY)) {
                alertType = Alert.Type.decode(flagMap.get(ALERT_TYPE_KEY).toString());
            }

            String message = null;
            if (flagMap.containsKey(MESSAGE_KEY)) {
                message = flagMap.get(MESSAGE_KEY).toString();
            }

            setFlag(alertType, message);
        }

        private void setFlag(Alert.Type alertType, String message) {
            if (alertType == null) {
                throw new IllegalArgumentException("alertType is null.");
            }

            this.alertType = alertType;
            this.message = message;
        }

        /**
         * Returns the flag's alert type.
         *
         * @return
         * The alert type of the flag.
         */
        public Alert.Type getAlertType() {
            return alertType;
        }

        /**
         * Returns the flag message.
         *
         * @return
         * The message text associated with the flag, or <tt>null</tt> if
         * there is no message.
         */
        public String getMessage() {
            return message;
        }
    }

    /**
     * Form field sequence.
     *
     * @author gbrown
     */
    public final class FieldSequence implements Sequence<Component>,
        Iterable<Component> {
        private class FieldIterator implements Iterator<Component> {
            Iterator<Component> source = null;

            public FieldIterator(Iterator<Component> source) {
                this.source = source;
            }

            public boolean hasNext() {
                return source.hasNext();
            }

            public Component next() {
                return source.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

        public int add(Component field) {
            int i = getLength();
            insert(field, i);

            return i;
        }

        public void insert(Component field, int index) {
            insertField(field, index);
        }

        public Component update(int index, Component field) {
            throw new UnsupportedOperationException();
        }

        public int remove(Component field) {
            int index = indexOf(field);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        public Sequence<Component> remove(int index, int count) {
            return removeFields(index, count);
        }

        public Component get(int index) {
            return fields.get(index);
        }

        public int indexOf(Component field) {
            return fields.indexOf(field);
        }

        public int getLength() {
            return fields.getLength();
        }

        public Iterator<Component> iterator() {
            return new FieldIterator(fields.iterator());
        }
    }

    /**
     * Form listener list.
     *
     * @author gbrown
     */
    private class FormListenerList extends ListenerList<FormListener>
        implements FormListener {
        public void fieldInserted(Form form, int index) {
            for (FormListener listener : this) {
                listener.fieldInserted(form, index);
            }
        }

        public void fieldsRemoved(Form form, int index, Sequence<Component> fields) {
            for (FormListener listener : this) {
                listener.fieldsRemoved(form, index, fields);
            }
        }
    }

    /**
     * Form attribute listener list.
     */
    private class FormAttributeListenerList extends ListenerList<FormAttributeListener>
        implements FormAttributeListener {
        public void labelChanged(Form form, Component component, String previousLabel) {
            for (FormAttributeListener listener : this) {
                listener.labelChanged(form, component, previousLabel);
            }
        }

        public void flagChanged(Form form, Component component, Form.Flag previousFlag) {
            for (FormAttributeListener listener : this) {
                listener.flagChanged(form, component, previousFlag);
            }
        }
    }

    private static class Attributes extends Bean {
        private String label = null;
        private Flag flag = null;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Flag getFlag() {
            return flag;
        }

        public void setFlag(Flag flag) {
            this.flag = flag;
        }
    }

    private ArrayList<Component> fields = new ArrayList<Component>();
    private FieldSequence fieldSequence = new FieldSequence();

    private FormListenerList formListeners = new FormListenerList();
    private FormAttributeListenerList formAttributeListeners = new FormAttributeListenerList();

    /**
     * Creates a new form.
     */
    public Form() {
        super();

        installSkin(Form.class);
    }

    /**
     * Returns the form's field sequence.
     *
     * @return
     * The form's field sequence.
     */
    public FieldSequence getFields() {
        return fieldSequence;
    }

    /**
     * Returns the number of fields that are flagged with a given flag type.
     *
     * @param alertType
     * The alert type to count, or <tt>null</tt> to return the count of all
     * flagged fields regardless of alert type.
     *
     * @return
     */
    public int getFlaggedFieldCount(Alert.Type alertType) {
        int count = 0;

        for (Component field : fields) {
            Flag flag = getFlag(field);

            if (flag != null
               && (alertType == null
                   || flag.getAlertType() == alertType)) {
                count++;
            }
        }

        return count;
    }

    /**
     * Inserts a field into the field sequence.
     *
     * @param field
     * The field to insert.
     *
     * @param index
     * The insertion index.
     */
    protected void insertField(Component field, int index) {
        if (field == null) {
            throw new IllegalArgumentException("field is null.");
        }

        if (field.getParent() != null) {
            throw new IllegalArgumentException("Field component already has a parent.");
        }

        // Add the field to the component sequence
        getComponents().add(field);
        fields.insert(field, index);

        formListeners.fieldInserted(this, index);
    }

    /**
     * Removes fields from the field sequence.
     *
     * @param index
     * The index of the first field to remove.
     *
     * @param count
     * The number of fields to remove.
     *
     * @return
     * An array containing the field components that were removed.
     */
    protected Sequence<Component> removeFields(int index, int count) {
        // Remove the fields from the field list
        Sequence<Component> removed = fields.remove(index, count);

        formListeners.fieldsRemoved(this, index, removed);

        // Remove the fields from the component list
        Sequence<Component> components = getComponents();
        for (int i = 0, n = removed.getLength(); i < n; i++) {
            Component field = removed.get(i);
            components.remove(field);
        }

        return removed;
    }

    /**
     * Returns the form listener list.
     *
     * @return
     * The form listener list.
     */
    public ListenerList<FormListener> getFormListeners() {
        return formListeners;
    }

    /**
     * Returns the form attribute listener list.
     *
     * @return
     * The form attribute listener list.
     */
    public ListenerList<FormAttributeListener> getFormAttributeListeners() {
        return formAttributeListeners;
    }

    public static String getLabel(Component component) {
        Attributes attributes = (Attributes)component.getAttributes().get(Form.class);
        return (attributes == null) ? null : attributes.getLabel();
    }

    public static void setLabel(Component component, String label) {
        Attributes attributes = (Attributes)component.getAttributes().get(Form.class);
        if (attributes == null) {
            attributes = new Attributes();
            component.getAttributes().put(Form.class, attributes);
        }

        String previousLabel = attributes.getLabel();
        attributes.setLabel(label);

        Form form = (Form)component.getParent();
        if (form != null) {
            form.formAttributeListeners.labelChanged(form, component, previousLabel);
        }
    }

    public static Flag getFlag(Component component) {
        Attributes attributes = (Attributes)component.getAttributes().get(Form.class);
        return (attributes == null) ? null : attributes.getFlag();
    }

    public static void setFlag(Component component, Flag flag) {
        Attributes attributes = (Attributes)component.getAttributes().get(Form.class);
        if (attributes == null) {
            attributes = new Attributes();
            component.getAttributes().put(Form.class, attributes);
        }

        Flag previousFlag = attributes.getFlag();
        attributes.setFlag(flag);

        Form form = (Form)component.getParent();
        if (form != null) {
            form.formAttributeListeners.flagChanged(form, component, previousFlag);
        }
    }

    public static final void setFlag(Component component, String flag) {
        setFlag(component, new Flag(flag));
    }
}
