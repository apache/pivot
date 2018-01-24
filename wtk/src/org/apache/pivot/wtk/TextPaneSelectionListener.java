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
 * Text pane selection listener interface.
 */
public interface TextPaneSelectionListener {
    /**
     * Text pane selection listeners.
     */
    public static class Listeners extends ListenerList<TextPaneSelectionListener>
        implements TextPaneSelectionListener {
        @Override
        public void selectionChanged(TextPane textPane, int previousSelectionStart,
            int previousSelectionLength) {
            forEach(listener -> listener.selectionChanged(textPane, previousSelectionStart, previousSelectionLength));
        }
    }

    /**
     * Called when a text pane's selection state has changed.
     *
     * @param textPane                The source of this event.
     * @param previousSelectionStart  The offset where the selection used to start.
     * @param previousSelectionLength What the selection length was previously.
     */
    public void selectionChanged(TextPane textPane, int previousSelectionStart,
        int previousSelectionLength);
}
