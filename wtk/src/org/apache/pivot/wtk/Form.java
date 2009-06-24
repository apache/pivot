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
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;


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
     * Class representing a form section. A section is a grouping of components
     * within a form.
     *
     * @author gbrown
     */
    public static class Section implements Sequence<Component>, Iterable<Component> {
        private Form form = null;
        private String heading = null;
        private ArrayList<Component> fields = new ArrayList<Component>();

        public Form getForm() {
            return form;
        }

        private void setForm(Form form) {
            this.form = form;
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

        public int add(Component field) {
            int index = getLength();
            insert(field, index);

            return index;
        }

        public void insert(Component field, int index) {
            fields.insert(field, index);

            if (form != null) {
                form.add(field);
                form.formListeners.fieldInserted(this, index);
            }

            field.setAttributes(new FormAttributes(this));
        }

        public Component update(int index, Component field) {
            throw new UnsupportedOperationException();
        }

        public int remove(Component field) {
            int index = fields.indexOf(field);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        public Sequence<Component> remove(int index, int count) {
            Sequence<Component> removed = fields.remove(index, count);

            for (int i = 0, n = removed.getLength(); i < n; i++) {
                Component field = removed.get(i);
                field.setAttributes(null);

                if (form != null) {
                    form.remove(field);
                }
            }

            if (form != null) {
                form.formListeners.fieldsRemoved(this, index, removed);
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
     * Section sequence implementation.
     *
     * @author gbrown
     */
    public final class SectionSequence implements Sequence<Section>, Iterable<Section> {
        private SectionSequence() {
        }

        public int add(Section section) {
            int index = getLength();
            insert(section, index);

            return index;
        }

        public void insert(Section section, int index) {
            if (section.getForm() != null) {
                throw new IllegalArgumentException("section already has a form.");
            }

            sections.insert(section, index);
            section.setForm(Form.this);

            for (int i = 0, n = section.getLength(); i < n; i++) {
                Form.this.add(section.get(i));
            }

            formListeners.sectionInserted(Form.this, index);
        }

        public Section update(int index, Section section) {
            throw new UnsupportedOperationException();
        }

        public int remove(Section section) {
            int index = sections.indexOf(section);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        public Sequence<Section> remove(int index, int count) {
            Sequence<Section> removed = sections.remove(index, count);

            for (int i = 0, n = removed.getLength(); i < n; i++) {
                Section section = removed.get(i);

                section.setForm(null);

                for (Component field : section) {
                    Form.this.remove(field);
                }
            }

            formListeners.sectionsRemoved(Form.this, index, removed);

            return removed;
        }

        public Section get(int index) {
            return sections.get(index);
        }

        public int indexOf(Section item) {
            return sections.indexOf(item);
        }

        public int getLength() {
            return sections.getLength();
        }

        public Iterator<Section> iterator() {
            return new ImmutableIterator<Section>(sections.iterator());
        }
    }

    /**
     * Defines form field attributes.
     *
     * @author gbrown
     */
    protected static class FormAttributes extends Attributes {
        private Section section = null;
        private String name = null;
        private Flag flag = null;

        protected FormAttributes(Section section) {
            this.section = section;
        }

        public Section getSection() {
            return section;
        }

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

        public static Flag decode(String flag) {
            Dictionary<String, ?> map;
            try {
                map = JSONSerializer.parseMap(flag);
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }

            Flag value = new Flag(MessageType.decode((String)map.get(MESSAGE_TYPE_KEY)),
                (String)map.get(MESSAGE_KEY));

            return value;
        }
    }

    /**
     * Form listener list.
     *
     * @author gbrown
     */
    private static class FormListenerList extends ListenerList<FormListener>
        implements FormListener {
        public void sectionInserted(Form form, int index) {
            for (FormListener listener : this) {
                listener.sectionInserted(form, index);
            }
        }

        public void sectionsRemoved(Form form, int index, Sequence<Section> removed) {
            for (FormListener listener : this) {
                listener.sectionsRemoved(form, index, removed);
            }
        }

        public void sectionHeadingChanged(Form.Section section) {
            for (FormListener listener : this) {
                listener.sectionHeadingChanged(section);
            }
        }

        public void fieldInserted(Section section, int index) {
            for (FormListener listener : this) {
                listener.fieldInserted(section, index);
            }
        }

        public void fieldsRemoved(Section section, int index, Sequence<Component> fields) {
            for (FormListener listener : this) {
                listener.fieldsRemoved(section, index, fields);
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
     * Returns the number of fields that are flagged with a given flag type.
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

    public static Section getSection(Component component) {
        FormAttributes formAttributes = (FormAttributes)component.getAttributes();
        return (formAttributes == null) ? null : formAttributes.getSection();
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

        setFlag(component, Flag.decode(flag));
    }
}
