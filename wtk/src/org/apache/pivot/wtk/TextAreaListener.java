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
 * Text area listener interface.
 */
public interface TextAreaListener {
    /**
     * Text area listeners.
     */
    public static class Listeners extends ListenerList<TextAreaListener> implements TextAreaListener {
        @Override
        public void maximumLengthChanged(TextArea textArea, int previousMaximumLength) {
            forEach(listener -> listener.maximumLengthChanged(textArea, previousMaximumLength));
        }

        @Override
        public void editableChanged(TextArea textArea) {
            forEach(listener -> listener.editableChanged(textArea));
        }
    }

    /**
     * Text area listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements TextAreaListener {
        @Override
        public void maximumLengthChanged(TextArea textArea, int previousMaximumLength) {
            // empty block
        }

        @Override
        public void editableChanged(TextArea textArea) {
            // empty block
        }
    }

    /**
     * Called when a text area's maximum length has changed.
     *
     * @param textArea The source of this event.
     * @param previousMaximumLength What the maximum length used to be.
     */
    default void maximumLengthChanged(TextArea textArea, int previousMaximumLength) {
    }

    /**
     * Called when a text area's editable state has changed.
     *
     * @param textArea The source of this event.
     */
    default void editableChanged(TextArea textArea) {
    }
}
