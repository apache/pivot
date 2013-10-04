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
 * Dialog state listener interface.
 */
public interface DialogStateListener extends DialogCloseListener {
    /**
     * Dialog state listener adapter.
     */
    public static class Adapter implements DialogStateListener {
        @Override
        public Vote previewDialogClose(Dialog dialog, boolean result) {
            return Vote.APPROVE;
        }

        @Override
        public void dialogCloseVetoed(Dialog dialog, Vote reason) {
            // empty block
        }

        @Override
        public void dialogClosed(Dialog dialog, boolean modal) {
            // empty block
        }
    }

    /**
     * Called to preview a dialog close event.
     *
     * @param dialog
     * @param result
     */
    public Vote previewDialogClose(Dialog dialog, boolean result);

    /**
     * Called when a dialog close event has been vetoed.
     *
     * @param dialog
     * @param reason
     */
    public void dialogCloseVetoed(Dialog dialog, Vote reason);
}
