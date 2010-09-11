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

/**
 * Text area binding listener interface.
 */
public interface TextPaneBindingListener {
    /**
     * Text area binding listener adapter.
     */
    public static class Adapter implements TextPaneBindingListener {
        @Override
        public void textKeyChanged(TextPane textPane, String previousTextKey) {
        }

        @Override
        public void textBindTypeChanged(TextPane textPane, BindType previousTextBindType) {
        }

        @Override
        public void textBindMappingChanged(TextPane textPane, TextPane.TextBindMapping previousTextBindMapping) {
        }
    }

    /**
     * Called when a text pane's text key has changed.
     *
     * @param textPane
     * @param previousTextKey
     */
    public void textKeyChanged(TextPane textPane, String previousTextKey);

    /**
     * Called when a text pane's text bind type has changed.
     *
     * @param textPane
     * @param previousTextBindType
     */
    public void textBindTypeChanged(TextPane textPane, BindType previousTextBindType);

    /**
     * Called when a text pane's text bind mapping has changed.
     *
     * @param textPane
     * @param previousTextBindMapping
     */
    public void textBindMappingChanged(TextPane textPane, TextPane.TextBindMapping previousTextBindMapping);
}
