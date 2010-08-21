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
 * Text input selection listener interface.
 */
public interface TextInputSelectionListener {
    /**
     * Called when a text input's selection state has changed.
     *
     * @param textInput
     * The source of the event.
     *
     * @param previousSelectionStart
     * If the selection changed directly, the previous selection start index.
     * Otherwise, the current selection start index.
     *
     * @param previousSelectionLength
     * If the selection changed directly, the previous selection length.
     * Otherwise, the current selection length.
     */
    public void selectionChanged(TextInput textInput, int previousSelectionStart,
        int previousSelectionLength);
}
