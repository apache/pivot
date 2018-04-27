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
 * Label listener interface.
 */
public interface LabelListener {
    /**
     * Label listeners.
     */
    public static class Listeners extends ListenerList<LabelListener> implements LabelListener {
        @Override
        public void textChanged(Label label, String previousText) {
            forEach(listener -> listener.textChanged(label, previousText));
        }

        @Override
        public void maximumLengthChanged(Label label, int previousMaximumLength) {
            forEach(listener -> listener.maximumLengthChanged(label, previousMaximumLength));
        }
    }

    /**
     * Label listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements LabelListener {
        @Override
        public void textChanged(Label label, String previousText) {
            // empty block
        }

        @Override
        public void maximumLengthChanged(Label label, int previousMaximumLength) {
            // empty block
        }
    }

    /**
     * Called when a label's text has changed.
     *
     * @param label        The label that has changed.
     * @param previousText The previous text associated with the label.
     */
    default void textChanged(Label label, String previousText) {
    }

    /**
     * Called when a label text maximum length has changed.
     *
     * @param label                 The label that has changed.
     * @param previousMaximumLength The previous maximum text length for the label.
     */
    default void maximumLengthChanged(Label label, int previousMaximumLength) {
    }
}
