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

import org.apache.pivot.util.Vote;

/**
 * Menu popup state listener interface.
 */
public interface MenuPopupStateListener {
    /**
     * Menu popup state listener adapter.
     */
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
     * @param menuPopup
     * @param immediate
     */
    public Vote previewMenuPopupClose(MenuPopup menuPopup, boolean immediate);

    /**
     * Called when a menu popup close event has been vetoed.
     *
     * @param menuPopup
     * @param reason
     */
    public void menuPopupCloseVetoed(MenuPopup menuPopup, Vote reason);

    /**
     * Called when a menu popup has closed.
     *
     * @param menuPopup
     */
    public void menuPopupClosed(MenuPopup menuPopup);
}
