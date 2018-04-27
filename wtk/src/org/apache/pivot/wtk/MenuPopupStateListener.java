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
import org.apache.pivot.util.Vote;
import org.apache.pivot.util.VoteResult;

/**
 * Menu popup state listener interface.
 */
public interface MenuPopupStateListener {
    /**
     * Menu popup state listeners.
     */
    public static class Listeners extends ListenerList<MenuPopupStateListener>
        implements MenuPopupStateListener {
        @Override
        public Vote previewMenuPopupClose(MenuPopup menuPopup, boolean immediate) {
            VoteResult result = new VoteResult();

            forEach(listener -> result.tally(listener.previewMenuPopupClose(menuPopup, immediate)));

            return result.get();
        }

        @Override
        public void menuPopupCloseVetoed(MenuPopup menuPopup, Vote reason) {
            forEach(listener -> listener.menuPopupCloseVetoed(menuPopup, reason));
        }

        @Override
        public void menuPopupClosed(MenuPopup menuPopup) {
            forEach(listener -> listener.menuPopupClosed(menuPopup));
        }
    }

    /**
     * Menu popup state listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements MenuPopupStateListener {
        @Override
        public Vote previewMenuPopupClose(MenuPopup menuPopup, boolean immediate) {
            return Vote.APPROVE;
        }

        @Override
        public void menuPopupCloseVetoed(MenuPopup menuPopup, Vote reason) {
            // empty block
        }

        @Override
        public void menuPopupClosed(MenuPopup menuPopup) {
            // empty block
        }
    }

    /**
     * Called to preview a menu popup close event.
     *
     * @param menuPopup The source of the event.
     * @param immediate Whether the close is meant to be immediate.
     * @return The verdict as to whether to close from this listener.
     */
    default Vote previewMenuPopupClose(MenuPopup menuPopup, boolean immediate) {
        return Vote.APPROVE;
    }

    /**
     * Called when a menu popup close event has been vetoed.
     *
     * @param menuPopup The source of the event.
     * @param reason The accumulated vote that caused the veto.
     */
    default void menuPopupCloseVetoed(MenuPopup menuPopup, Vote reason) {
    }

    /**
     * Called when a menu popup has closed.
     *
     * @param menuPopup The menu popup that closed.
     */
    default void menuPopupClosed(MenuPopup menuPopup) {
    }
}
