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
 * Label binding listener interface.
 */
public interface LabelBindingListener {
    /**
     * Label binding listeners.
     */
    public static class Listeners extends ListenerList<LabelBindingListener>
        implements LabelBindingListener {
        @Override
        public void textKeyChanged(Label label, String previousTextKey) {
            forEach(listener -> listener.textKeyChanged(label, previousTextKey));
        }

        @Override
        public void textBindTypeChanged(Label label, BindType previousTextBindType) {
            forEach(listener -> listener.textBindTypeChanged(label, previousTextBindType));
        }

        @Override
        public void textBindMappingChanged(Label label,
            Label.TextBindMapping previousTextBindMapping) {
            forEach(listener -> listener.textBindMappingChanged(label, previousTextBindMapping));
        }
    }

    /**
     * Label binding listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements LabelBindingListener {
        @Override
        public void textKeyChanged(Label label, String previousTextKey) {
            // empty block
        }

        @Override
        public void textBindTypeChanged(Label label, BindType previousTextBindType) {
            // empty block
        }

        @Override
        public void textBindMappingChanged(Label label,
            Label.TextBindMapping previousTextBindMapping) {
            // empty block
        }
    }

    /**
     * Called when a label's text key has changed.
     *
     * @param label           The label whose binding has changed.
     * @param previousTextKey The previous binding key for the label text.
     */
    default void textKeyChanged(Label label, String previousTextKey) {
    }

    /**
     * Called when a label's text bind type has changed.
     *
     * @param label                The label whose binding has changed.
     * @param previousTextBindType The previous bind type for the label text.
     */
    default void textBindTypeChanged(Label label, BindType previousTextBindType) {
    }

    /**
     * Called when a label's text bind mapping has changed.
     *
     * @param label                   The label whose binding has changed.
     * @param previousTextBindMapping The previous bind mapping for the label text.
     */
    default void textBindMappingChanged(Label label, Label.TextBindMapping previousTextBindMapping) {
    }
}
