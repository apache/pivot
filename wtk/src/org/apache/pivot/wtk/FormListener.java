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

/**
 * Form listener interface.
 */
public interface FormListener {
    /**
     * Form listener adapter.
     */
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
     * @param form
     * @param index
     */
    public void sectionInserted(Form form, int index);

    /**
     * Called when form sections have been removed.
     *
     * @param form
     * @param index
     * @param removed
     */
    public void sectionsRemoved(Form form, int index, Sequence<Form.Section> removed);

    /**
     * Called when a form section's heading has changed.
     *
     * @param section
     */
    public void sectionHeadingChanged(Form.Section section);

    /**
     * Called when a form field has been inserted.
     *
     * @param section
     * @param index
     */
    public void fieldInserted(Form.Section section, int index);

    /**
     * Called when forms fields items have been removed.
     *
     * @param section
     * @param index
     * @param fields
     */
    public void fieldsRemoved(Form.Section section, int index, Sequence<Component> fields);
}
