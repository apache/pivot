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
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

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
        private ArrayList<Component> fields = new ArrayList<>();

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
            Utils.checkNull(field, "field");

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
        @UnsupportedOperation
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
            return new ImmutableIterator<>(fields.iterator());
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
                throw new IllegalArgumentException("Section already has a Form.");
            }

            sections.insert(section, index);
            section.form = Form.this;

            for (int i = 0, n = section.getLength(); i < n; i++) {
                Form.this.add(section.get(i));
            }

            formListeners.sectionInserted(Form.this, index);
        }

        @Override
        @UnsupportedOperation
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
            return new ImmutableIterator<>(sections.iterator());
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
         * @param messageType The type of the flag.
         */
        public Flag(MessageType messageType) {
            this(messageType, null);
        }

        /**
         * Creates a new flag with the given type and message.
         *
         * @param messageType The type of the flag.
         * @param message The message text associated with the flag, or
         * <tt>null</tt> for no message.
         */
        public Flag(MessageType messageType, String message) {
            Utils.checkNull(messageType, "messageType");

            this.messageType = messageType;
            this.message = message;
        }

        /**
         * Returns the flag's message type.
         *
         * @return The message type of the flag.
         */
        public MessageType getMessageType() {
            return messageType;
        }

        /**
         * Sets the flag's message type.
         *
         * @param messageType The new message type for this flag.
         * @throws IllegalArgumentException if the message type is {@code null}.
         */
        public void setMessageType(MessageType messageType) {
            Utils.checkNull(messageType, "messageType");

            this.messageType = messageType;
        }

        /**
         * Returns the flag message.
         *
         * @return The message text associated with the flag, or <tt>null</tt>
         * if there is no message.
         */
        public String getMessage() {
            return message;
        }

        /**
         * Sets the flag message.
         *
         * @param message The message text associated with the flag, or
         * <tt>null</tt> if there is no message.
         */
        public void setMessage(String message) {
            this.message = message;
        }

        public static Flag decode(String flag) {
            Utils.checkNullOrEmpty(flag, "flag");

            Dictionary<String, ?> map;
            try {
                map = JSONSerializer.parseMap(flag);
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }

            String messageType = map.getString(MESSAGE_TYPE_KEY);
            if (messageType == null) {
                throw new IllegalArgumentException(MESSAGE_TYPE_KEY + " is required.");
            }

            Flag value = new Flag(MessageType.fromString(messageType),
                map.getString(MESSAGE_KEY));

            return value;
        }
    }

    private enum Attribute {
        SECTION,
        LABEL,
        REQUIRED,
        FLAG;
    }

    private ArrayList<Section> sections = new ArrayList<>();
    private SectionSequence sectionSequence = new SectionSequence();

    private FormListener.Listeners formListeners = new FormListener.Listeners();
    private FormAttributeListener.Listeners formAttributeListeners = new FormAttributeListener.Listeners();

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
     * @return The form's field sequence.
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
                setFlag(field, (Flag) null);
            }
        }
    }

    /**
     * Returns the number of fields that are flagged with a given message type.
     *
     * @param messageType The message type to count, or <tt>null</tt> to return
     * the count of all flagged fields regardless of message type.
     * @return The number of flagged fields.
     */
    public int getFlaggedFieldCount(MessageType messageType) {
        int count = 0;

        for (Section section : sections) {
            for (Component field : section) {
                Flag flag = getFlag(field);

                if (flag != null && (messageType == null || flag.getMessageType() == messageType)) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Ensures that the first field with the given flag type is visible.
     *
     * @param messageType The message type, or <tt>null</tt> to scroll the first
     * flag of any type to visible.
     */
    public void scrollFirstFlagToVisible(MessageType messageType) {
        Flag flag = null;

        for (Section section : sections) {
            for (Component field : section) {
                flag = getFlag(field);

                if (flag != null && (messageType == null || flag.getMessageType() == messageType)) {
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
                    throw new UnsupportedOperationException("Cannot directly remove a Form Section.");
                }
            }
        }

        // Call the base method to remove the components
        return super.remove(index, count);
    }

    /**
     * @return The form listener list.
     */
    public ListenerList<FormListener> getFormListeners() {
        return formListeners;
    }

    /**
     * @return The form attribute listener list.
     */
    public ListenerList<FormAttributeListener> getFormAttributeListeners() {
        return formAttributeListeners;
    }

    /**
     * Finds the {@link Form.Section} that the given component belongs to. Only
     * finds the section if the component is a direct child of the section.
     *
     * @param component The component in question.
     * @return The section this component belongs to.
     * @see #getEnclosingSection getEnclosingSection(Component)
     */
    public static Section getSection(Component component) {
        return (Section) component.getAttribute(Attribute.SECTION);
    }

    /**
     * Finds the {@link Form.Section} that the given component belongs to. Will
     * search up the parent hierarchy in case the component is nested inside
     * other containers inside the form.
     *
     * @param component The component in question.
     * @return The form section this component (or one of its parents) belongs
     * to or <code>null</code> if the component does not belong to a form.
     * @see #getSection getSection(Component)
     */
    public static Section getEnclosingSection(Component component) {
        Section section = (Section) component.getAttribute(Attribute.SECTION);
        if (section == null) {
            for (Container parent = component.getParent(); parent != null
                && (section = (Section) parent.getAttribute(Attribute.SECTION)) == null;) {
                parent = parent.getParent();
            }
        }
        return section;
    }

    public static String getLabel(Component component) {
        return (String) component.getAttribute(Attribute.LABEL);
    }

    public static void setLabel(Component component, String label) {
        String previousLabel = (String) component.setAttribute(Attribute.LABEL, label);

        if (previousLabel != label) {
            Container parent = component.getParent();

            if (parent instanceof Form) {
                Form form = (Form) parent;
                form.formAttributeListeners.labelChanged(form, component, previousLabel);
            }
        }
    }

    public static boolean isRequired(Component component) {
        Boolean value = (Boolean) component.getAttribute(Attribute.REQUIRED);
        return (value == null) ? false : value.booleanValue();
    }

    public static void setRequired(Component component, boolean required) {
        Boolean previousValue = (Boolean) component.setAttribute(Attribute.REQUIRED, Boolean.valueOf(required));
        boolean previousRequired = (previousValue == null) ? false : previousValue.booleanValue();

        if (previousRequired != required) {
            Container parent = component.getParent();

            if (parent instanceof Form) {
                Form form = (Form) parent;
                form.formAttributeListeners.requiredChanged(form, component);
            }
        }
    }

    public static Flag getFlag(Component component) {
        return (Flag) component.getAttribute(Attribute.FLAG);
    }

    public static void setFlag(Component component, Flag flag) {
        Flag previousFlag = (Flag) component.setAttribute(Attribute.FLAG, flag);

        if (previousFlag != flag) {
            Container parent = component.getParent();

            if (parent instanceof Form) {
                Form form = (Form) parent;
                form.formAttributeListeners.flagChanged(form, component, previousFlag);
            }
        }
    }

    public static final void setFlag(Component component, String flag) {
        setFlag(component, Flag.decode(flag));
    }

    public static final void clearFlag(Component component) {
        setFlag(component, (Flag) null);
    }
}
