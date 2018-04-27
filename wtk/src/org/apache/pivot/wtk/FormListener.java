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

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;

/**
 * Form listener interface.
 */
public interface FormListener {
    /**
     * Form listeners.
     */
    public static class Listeners extends ListenerList<FormListener> implements FormListener {
        @Override
        public void sectionInserted(Form form, int index) {
            forEach(listener -> listener.sectionInserted(form, index));
        }

        @Override
        public void sectionsRemoved(Form form, int index, Sequence<Form.Section> removed) {
            forEach(listener -> listener.sectionsRemoved(form, index, removed));
        }

        @Override
        public void sectionHeadingChanged(Form.Section section) {
            forEach(listener -> listener.sectionHeadingChanged(section));
        }

        @Override
        public void fieldInserted(Form.Section section, int index) {
            forEach(listener -> listener.fieldInserted(section, index));
        }

        @Override
        public void fieldsRemoved(Form.Section section, int index, Sequence<Component> fields) {
            forEach(listener -> listener.fieldsRemoved(section, index, fields));
        }
    }

    /**
     * Form listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements FormListener {
        @Override
        public void sectionInserted(Form form, int index) {
            // empty block
        }

        @Override
        public void sectionsRemoved(Form form, int index, Sequence<Form.Section> removed) {
            // empty block
        }

        @Override
        public void sectionHeadingChanged(Form.Section section) {
            // empty block
        }

        @Override
        public void fieldInserted(Form.Section section, int index) {
            // empty block
        }

        @Override
        public void fieldsRemoved(Form.Section section, int index, Sequence<Component> fields) {
            // empty block
        }
    }

    /**
     * Called when a form section has been inserted.
     *
     * @param form  The form that has changed.
     * @param index The index where the new section has been inserted.
     */
    default void sectionInserted(Form form, int index) {
    }

    /**
     * Called when form sections have been removed.
     *
     * @param form    The form that has changed.
     * @param index   The starting index where sections were removed.
     * @param removed The complete sequence of the removed sections.
     */
    default void sectionsRemoved(Form form, int index, Sequence<Form.Section> removed) {
    }

    /**
     * Called when a form section's heading has changed.
     *
     * @param section The form section whose heading changed.
     */
    default void sectionHeadingChanged(Form.Section section) {
    }

    /**
     * Called when a form field has been inserted.
     *
     * @param section The enclosing form section that has changed.
     * @param index   The index where a new field has been inserted.
     */
    default void fieldInserted(Form.Section section, int index) {
    }

    /**
     * Called when form fields have been removed.
     *
     * @param section The enclosing form section.
     * @param index   The starting index where fields were removed.
     * @param fields  The complete sequence of fields that were removed.
     */
    default void fieldsRemoved(Form.Section section, int index, Sequence<Component> fields) {
    }
}
