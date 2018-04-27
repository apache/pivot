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
 * Sheet state listener interface.
 */
public interface SheetStateListener extends SheetCloseListener {
    /**
     * Sheet state listeners.
     */
    public static class Listeners extends ListenerList<SheetStateListener>
        implements SheetStateListener {
        @Override
        public Vote previewSheetClose(Sheet sheet, boolean result) {
            VoteResult vote = new VoteResult(Vote.APPROVE);

            forEach(listener -> vote.tally(listener.previewSheetClose(sheet, result)));

            return vote.get();
        }

        @Override
        public void sheetCloseVetoed(Sheet sheet, Vote reason) {
            forEach(listener -> listener.sheetCloseVetoed(sheet, reason));
        }

        @Override
        public void sheetClosed(Sheet sheet) {
            forEach(listener -> listener.sheetClosed(sheet));
        }
    }

    /**
     * Sheet state listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
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
     * @param sheet The source of the event.
     * @param result The proposed result of the close.
     * @return What this listener wants to decide about this proposed close.
     */
    default Vote previewSheetClose(Sheet sheet, boolean result) {
        return Vote.APPROVE;
    }

    /**
     * Called when a sheet close event has been vetoed.
     *
     * @param sheet The close event source.
     * @param reason The accumulated vote that resulted in the veto.
     */
    default void sheetCloseVetoed(Sheet sheet, Vote reason) {
    }
}
