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
package org.apache.pivot.wtk.text;

import org.apache.pivot.util.ListenerList;

/**
 * NumberedList listener interface.
 */
public interface NumberedListListener {
    /**
     * Numbered list listeners.
     */
    public static class Listeners extends ListenerList<NumberedListListener>
        implements NumberedListListener {
        @Override
        public void styleChanged(NumberedList numberedList, NumberedList.Style previousStyle) {
            forEach(listener -> listener.styleChanged(numberedList, previousStyle));
        }
    }

    /**
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public class Adapter implements NumberedListListener {
        @Override
        public void styleChanged(NumberedList numberedList, NumberedList.Style previousStyle) {
            // empty block
        }
    }

    /**
     * Called when the list style has changed.
     *
     * @param numberedList  The list whose style has changed.
     * @param previousStyle The previous style for this list.
     */
    default void styleChanged(NumberedList numberedList, NumberedList.Style previousStyle) {
    }
}
