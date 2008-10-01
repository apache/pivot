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
import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.Sequence;
import pivot.serialization.JSONSerializer;
import pivot.util.ImmutableIterator;
import pivot.util.ListenerList;

/**
 * A container that arranges field components in a form layout. Each field has
 * an optional text label associated with it and may be flagged as requiring
 * attention using one of several flag types and an optional flag message (for
 * use during form validation, for example).
 *
 * @author gbrown
 */
@ComponentInfo(icon="Form.png")
public class Form extends Container {
    /**
     * Defines form field attributes.
     *
     * @author gbrown
     */
    protected static class FormAttributes extends Attributes {
        private String name = null;
        private Flag flag = null;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            String previousName = this.name;
            this.name = name;

            Component component = getComponent();
            Form form = (Form)component.getParent();
            if (form != null) {
                form.formAttributeListeners.nameChanged(form, component, previousName);
            }
        }

        public Flag getFlag() {
            return flag;
        }

        public void setFlag(Flag flag) {
            Flag previousFlag = this.flag;
            this.flag = flag;

            Component component = getComponent();
            Form form = (Form)component.getParent();
            if (form != null) {
                form.formAttributeListeners.flagChanged(form, component, previousFlag);
            }
        }
    }

    /**
     * Form field sequence.
     *
     * @author gbrown
     */
    public final class FieldSequence implements Sequence<Component>, Iterable<Component> {
        private FieldSequence() {
        }

        public int add(Component field) {
            int i = getLength();
            insert(field, i);

            return i;
        }

        public void insert(Component field, int index) {
            if (field == null) {
                throw new IllegalArgumentException("field is null.");
            }

            if (field.getParent() != null) {
                throw new IllegalArgumentException("Field component already has a parent.");
            }

            // Add the field to the component sequence
            Form.this.add(field);
            fields.insert(field, index);

            // Attach the attributes
            field.setAttributes(new FormAttributes());

            formListeners.fieldInserted(Form.this, index);
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
            // Remove the fields from the field list
            Sequence<Component> removed = fields.remove(index, count);

            // Detach the attributes
            for (int i = 0, n = removed.getLength(); i < n; i++) {
                removed.get(i).setAttributes(null);
            }

            formListeners.fieldsRemoved(Form.this, index, removed);

            // Remove the fields from the component list
            for (int i = 0, n = removed.getLength(); i < n; i++) {
                Component field = removed.get(i);
                Form.this.remove(field);
            }

            return removed;
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
            return new ImmutableIterator<Component>(fields.iterator());
        }
    }

    /**
     * Represents an message alert associated with a form field.
     *
     * @author gbrown
     */
    public static class Flag {
        /**
         * The flag's message type.
         */
        private MessageType messageType = null;

        /**
         * The flag message. May be <tt>null</tt>.
         */
        private String message = null;

        public static final String MESSAGE_TYPE_KEY = "messageType";
        public static final String MESSAGE_KEY = "message";

        /**
         * Creates a new flag with the given message type and no message.
         *
         * @param messageType
         * The type of the flag.
         */
        public Flag(MessageType messageType) {
            this(messageType, null);
        }

        public Flag(String flag) {
            this(JSONSerializer.parseMap(flag));
        }

        public Flag(Dictionary<String, ?> flag) {
            this(MessageType.decode((String)flag.get(MESSAGE_TYPE_KEY)),
                (String)flag.get(MESSAGE_KEY));
        }

        /**
         * Creates a new flag with the given type and message.
         *
         * @param messageType
         * The type of the flag.
         *
         * @param message
         * The message text associated with the flag, or <tt>null</tt> for
         * no message.
         */
        public Flag(MessageType messageType, String message) {
            if (messageType == null) {
                throw new IllegalArgumentException("messageType is null.");
            }

            this.messageType = messageType;
            this.message = message;
        }

        /**
         * Returns the flag's message type.
         *
         * @return
         * The message type of the flag.
         */
        public MessageType getMessageType() {
            return messageType;
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
     * Form listener list.
     *
     * @author gbrown
     */
    private static class FormListenerList extends ListenerList<FormListener>
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
    private static class FormAttributeListenerList extends ListenerList<FormAttributeListener>
        implements FormAttributeListener {
        public void nameChanged(Form form, Component component, String previousName) {
            for (FormAttributeListener listener : this) {
                listener.nameChanged(form, component, previousName);
            }
        }

        public void flagChanged(Form form, Component component, Form.Flag previousFlag) {
            for (FormAttributeListener listener : this) {
                listener.flagChanged(form, component, previousFlag);
            }
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
     * @param messageType
     * The message type to count, or <tt>null</tt> to return the count of all
     * flagged fields regardless of message type.
     */
    public int getFlaggedFieldCount(MessageType messageType) {
        int count = 0;

        for (Component field : fields) {
            Flag flag = getFlag(field);

            if (flag != null
               && (messageType == null
                   || flag.getMessageType() == messageType)) {
                count++;
            }
        }

        return count;
    }

    @Override
    public Sequence<Component> remove(int index, int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);
            if (component.getAttributes() != null) {
                throw new UnsupportedOperationException();
            }
        }

        // Call the base method to remove the components
        return super.remove(index, count);
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

    public static String getName(Component component) {
        FormAttributes formAttributes = (FormAttributes)component.getAttributes();
        return (formAttributes == null) ? null : formAttributes.getName();
    }

    public static void setName(Component component, String name) {
        FormAttributes formAttributes = (FormAttributes)component.getAttributes();
        if (formAttributes == null) {
            throw new IllegalStateException();
        }

        formAttributes.setName(name);
    }

    public static Flag getFlag(Component component) {
        FormAttributes formAttributes = (FormAttributes)component.getAttributes();
        return (formAttributes == null) ? null : formAttributes.getFlag();
    }

    public static void setFlag(Component component, Flag flag) {
        FormAttributes formAttributes = (FormAttributes)component.getAttributes();
        if (formAttributes == null) {
            throw new IllegalStateException();
        }

        formAttributes.setFlag(flag);
    }

    public static final void setFlag(Component component, String flag) {
        if (flag == null) {
            throw new IllegalArgumentException("flag is null.");
        }

        setFlag(component, new Flag(flag));
    }
}
