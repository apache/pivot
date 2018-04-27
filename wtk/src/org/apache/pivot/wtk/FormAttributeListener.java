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

import org.apache.pivot.util.ListenerList;

/**
 * Form attribute listener interface.
 */
public interface FormAttributeListener {
    /**
     * Form attribute listeners.
     */
    public static class Listeners extends ListenerList<FormAttributeListener>
        implements FormAttributeListener {
        @Override
        public void labelChanged(Form form, Component component, String previousLabel) {
            forEach(listener -> listener.labelChanged(form, component, previousLabel));
        }

        @Override
        public void requiredChanged(Form form, Component field) {
            forEach(listener -> listener.requiredChanged(form, field));
        }

        @Override
        public void flagChanged(Form form, Component component, Form.Flag previousFlag) {
            forEach(listener -> listener.flagChanged(form, component, previousFlag));
        }
    }

    /**
     * Form attribute listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public class Adapter implements FormAttributeListener {
        @Override
        public void labelChanged(Form form, Component field, String previousLabel) {
            // empty block
        }

        @Override
        public void requiredChanged(Form form, Component field) {
            // empty block
        }

        @Override
        public void flagChanged(Form form, Component field, Form.Flag previousFlag) {
            // empty block
        }
    }

    /**
     * Called when a field's label attribute has changed.
     *
     * @param form          The enclosing form.
     * @param field         The field whose form label has changed.
     * @param previousLabel The previous form label for this field.
     */
    default void labelChanged(Form form, Component field, String previousLabel) {
    }

    /**
     * Called when a fields's required attribute has changed.
     *
     * @param form  The enclosing form.
     * @param field The field that is or is not now required.
     */
    default void requiredChanged(Form form, Component field) {
    }

    /**
     * Called when a field's flag attribute has changed.
     *
     * @param form         The enclosing form.
     * @param field        The field whose flag attribute has changed.
     * @param previousFlag The previous flag value for this field.
     */
    default void flagChanged(Form form, Component field, Form.Flag previousFlag) {
    }
}
