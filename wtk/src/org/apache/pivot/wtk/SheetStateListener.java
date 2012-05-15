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
 * Sheet state listener interface.
 */
public interface SheetStateListener extends SheetCloseListener {
    /**
     * Sheet state listener adapter.
     */
    public static class Adapter implements SheetStateListener {
        @Override
        public Vote previewSheetClose(Sheet sheet, boolean result) {
            return Vote.APPROVE;
        }

        @Override
        public void sheetCloseVetoed(Sheet sheet, Vote reason) {
            // empty block
        }

        @Override
        public void sheetClosed(Sheet sheet) {
            // empty block
        }
    }

    /**
     * Called to preview a sheet close event.
     *
     * @param sheet
     * @param result
     */
    public Vote previewSheetClose(Sheet sheet, boolean result);

    /**
     * Called when a sheet close event has been vetoed.
     *
     * @param sheet
     * @param reason
     */
    public void sheetCloseVetoed(Sheet sheet, Vote reason);
}
