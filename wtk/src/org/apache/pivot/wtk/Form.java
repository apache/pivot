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
import java.util.Locale;

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;

/**
 * A container that arranges field components in a form layout. Each field has
 * an optional text label associated with it and may be flagged as requiring
 * attention using one of several flag types and an optional flag message (for
 * use during form validation, for example).
 */
@DefaultProperty("sections")
public class Form extends Container {
    /**
     * Class representing a form section. A section is a grouping of components
     * within a form.
     */
    public static class Section implements Sequence<Component>, Iterable<Component> {
        private Form form = null;
        private String heading = null;
        private ArrayList<Component> fields = new ArrayList<Component>();

        public Form getForm() {
            return form;
        }

        public String getHeading() {
            return heading;
        }

        public void setHeading(String heading) {
            String previousHeading = this.heading;
            if (previousHeading != heading) {
                this.heading = heading;

                if (form != null) {
                    form.formListeners.sectionHeadingChanged(this);
                }
            }
        }

        @Override
        public int add(Component field) {
            int index = getLength();
            insert(field, index);

            return index;
        }

        @Override
        public void insert(Component field, int index) {
            if (field == null) {
                throw new IllegalArgumentException();
            }

            if (field.getParent() != null) {
                throw new IllegalArgumentException("Field already has a parent.");
            }

            fields.insert(field, index);
            field.setAttribute(Attribute.SECTION, this);

            if (form != null) {
                form.add(field);
                form.formListeners.fieldInserted(this, index);
            }
        }

        @Override
        public Component update(int index, Component field) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(Component field) {
            int index = fields.indexOf(field);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<Component> remove(int index, int count) {
            Sequence<Component> removed = fields.remove(index, count);

            for (int i = 0, n = removed.getLength(); i < n; i++) {
                Component field = removed.get(i);
                field.setAttribute(Attribute.SECTION, null);

                if (form != null) {
                    form.remove(field);
                }
            }

            if (form != null) {
                form.formListeners.fieldsRemoved(this, index, removed);
            }

            return removed;
        }

        @Override
        public Component get(int index) {
            return fields.get(index);
        }

        @Override
        public int indexOf(Component field) {
            return fields.indexOf(field);
        }

        @Override
        public int getLength() {
            return fields.getLength();
        }

        @Override
        public Iterator<Component> iterator() {
            return new ImmutableIterator<Component>(fields.iterator());
        }
    }

    /**
     * Section sequence implementation.
     */
    public final class SectionSequence implements Sequence<Section>, Iterable<Section> {
        private SectionSequence() {
        }

        @Override
        public int add(Section section) {
            int index = getLength();
            insert(section, index);

            return index;
        }

        @Override
        public void insert(Section section, int index) {
            if (section.form != null) {
                throw new IllegalArgumentException("section already has a form.");
            }

            sections.insert(section, index);
            section.form = Form.this;

            for (int i = 0, n = section.getLength(); i < n; i++) {
                Form.this.add(section.get(i));
            }

            formListeners.sectionInserted(Form.this, index);
        }

        @Override
        public Section update(int index, Section section) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(Section section) {
            int index = sections.indexOf(section);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<Section> remove(int index, int count) {
            Sequence<Section> removed = sections.remove(index, count);

            for (int i = 0, n = removed.getLength(); i < n; i++) {
                Section section = removed.get(i);

                section.form = null;

                for (Component field : section) {
                    Form.this.remove(field);
                }
            }

            formListeners.sectionsRemoved(Form.this, index, removed);

            return removed;
        }

        @Override
        public Section get(int index) {
            return sections.get(index);
        }

        @Override
        public int indexOf(Section item) {
            return sections.indexOf(item);
        }

        @Override
        public int getLength() {
            return sections.getLength();
        }

        @Override
        public Iterator<Section> iterator() {
            return new ImmutableIterator<Section>(sections.iterator());
        }
    }

    /**
     * Represents an message alert associated with a form field.
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
         * Creates a new flag with a type of "error" and no message.
         */
        public Flag() {
            this(MessageType.ERROR, null);
        }

        /**
         * Creates a new flag with the given message type and no message.
         *
         * @param messageType
         * The type of the flag.
         */
        public Flag(MessageType messageType) {
            this(messageType, null);
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
         * Sets the flag's message type.
         *
         * @param messageType
         */
        public void setMessageType(MessageType messageType) {
            if (messageType == null) {
                throw new IllegalArgumentException();
            }

            this.messageType = messageType;
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

        /**
         * Sets the flag message.
         *
         * @param message
         * The message text associated with the flag, or <tt>null</tt> if
         * there is no message.
         */
        public void setMessage(String message) {
            this.message = message;
        }

        public static Flag decode(String flag) {
            Dictionary<String, ?> map;
            try {
                map = JSONSerializer.parseMap(flag);
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }

            String messageType = (String)map.get(MESSAGE_TYPE_KEY);
            if (messageType == null) {
                throw new IllegalArgumentException(MESSAGE_TYPE_KEY + " is required.");
            }

            Flag value = new Flag(MessageType.valueOf(messageType.toUpperCase(Locale.ENGLISH)),
                (String)map.get(MESSAGE_KEY));

            return value;
        }
    }

    private enum Attribute {
        SECTION,
        LABEL,
        REQUIRED,
        FLAG;
    }

    private static class FormListenerList extends WTKListenerList<FormListener>
        implements FormListener {
        @Override
        public void sectionInserted(Form form, int index) {
            for (FormListener listener : this) {
                listener.sectionInserted(form, index);
            }
        }

        @Override
        public void sectionsRemoved(Form form, int index, Sequence<Section> removed) {
            for (FormListener listener : this) {
                listener.sectionsRemoved(form, index, removed);
            }
        }

        @Override
        public void sectionHeadingChanged(Form.Section section) {
            for (FormListener listener : this) {
                listener.sectionHeadingChanged(section);
            }
        }

        @Override
        public void fieldInserted(Section section, int index) {
            for (FormListener listener : this) {
                listener.fieldInserted(section, index);
            }
        }

        @Override
        public void fieldsRemoved(Section section, int index, Sequence<Component> fields) {
            for (FormListener listener : this) {
                listener.fieldsRemoved(section, index, fields);
            }
        }
    }

    private static class FormAttributeListenerList extends WTKListenerList<FormAttributeListener>
        implements FormAttributeListener {
        @Override
        public void labelChanged(Form form, Component component, String previousLabel) {
            for (FormAttributeListener listener : this) {
                listener.labelChanged(form, component, previousLabel);
            }
        }

        @Override
        public void requiredChanged(Form form, Component field) {
            for (FormAttributeListener listener : this) {
                listener.requiredChanged(form, field);
            }
        }

        @Override
        public void flagChanged(Form form, Component component, Form.Flag previousFlag) {
            for (FormAttributeListener listener : this) {
                listener.flagChanged(form, component, previousFlag);
            }
        }
    }

    private ArrayList<Section> sections = new ArrayList<Section>();
    private SectionSequence sectionSequence = new SectionSequence();

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
    public SectionSequence getSections() {
        return sectionSequence;
    }

    /**
     * Clears all field flags.
     */
    public void clearFlags() {
        for (Section section : sections) {
            for (Component field : section) {
                setFlag(field, (Flag)null);
            }
        }
    }

    /**
     * Returns the number of fields that are flagged with a given message type.
     *
     * @param messageType
     * The message type to count, or <tt>null</tt> to return the count of all
     * flagged fields regardless of message type.
     */
    public int getFlaggedFieldCount(MessageType messageType) {
        int count = 0;

        for (Section section : sections) {
            for (Component field : section) {
                Flag flag = getFlag(field);

                if (flag != null
                   && (messageType == null
                       || flag.getMessageType() == messageType)) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Ensures that the first field with the given flag type is visible.
     *
     * @param messageType
     * The message type, or <tt>null</tt> to scroll the first flag of any
     * type to visible.
     */
    public void scrollFirstFlagToVisible(MessageType messageType) {
        Flag flag = null;

        for (Section section : sections) {
            for (Component field : section) {
                flag = getFlag(field);

                if (flag != null
                   && (messageType == null
                       || flag.getMessageType() == messageType)) {
                    field.scrollAreaToVisible(0, 0, field.getWidth(), field.getHeight());
                    break;
                }
            }

            if (flag != null) {
                break;
            }
        }
    }

    @Override
    public Sequence<Component> remove(int index, int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);

            for (Section section : sections) {
                if (section.indexOf(component) >= 0) {
                    throw new UnsupportedOperationException();
                }
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

    public static Section getSection(Component component) {
        return (Section)component.getAttribute(Attribute.SECTION);
    }

    public static String getLabel(Component component) {
        return (String)component.getAttribute(Attribute.LABEL);
    }

    public static void setLabel(Component component, String label) {
        String previousLabel = (String)component.setAttribute(Attribute.LABEL, label);

        if (previousLabel != label) {
            Container parent = component.getParent();

            if (parent instanceof Form) {
                Form form = (Form)parent;
                form.formAttributeListeners.labelChanged(form, component, previousLabel);
            }
        }
    }

    public static boolean isRequired(Component component) {
        Boolean value = (Boolean)component.getAttribute(Attribute.REQUIRED);
        return (value == null) ? false : value;
    }

    public static void setRequired(Component component, boolean required) {
        Boolean previousValue = (Boolean)component.setAttribute(Attribute.REQUIRED, required);
        boolean previousRequired = (previousValue == null) ? false : previousValue;

        if (previousRequired != required) {
            Container parent = component.getParent();

            if (parent instanceof Form) {
                Form form = (Form)parent;
                form.formAttributeListeners.requiredChanged(form, component);
            }
        }
    }

    public static Flag getFlag(Component component) {
        return (Flag)component.getAttribute(Attribute.FLAG);
    }

    public static void setFlag(Component component, Flag flag) {
        Flag previousFlag = (Flag)component.setAttribute(Attribute.FLAG, flag);

        if (previousFlag != flag) {
            Container parent = component.getParent();

            if (parent instanceof Form) {
                Form form = (Form)parent;
                form.formAttributeListeners.flagChanged(form, component, previousFlag);
            }
        }
    }

    public static final void setFlag(Component component, String flag) {
        if (flag == null) {
            throw new IllegalArgumentException("flag is null.");
        }

        setFlag(component, Flag.decode(flag));
    }

    public static final void clearFlag(Component component) {
        setFlag(component, (Flag)null);
    }
}
