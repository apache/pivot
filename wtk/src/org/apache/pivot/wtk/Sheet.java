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

/**
 * Window class representing a "sheet". A sheet behaves like a dialog that is
 * modal only over a window's content component.
 */
public class Sheet extends Window {
    private static class SheetStateListenerList extends WTKListenerList<SheetStateListener>
        implements SheetStateListener {
        @Override
        public Vote previewSheetClose(Sheet sheet, boolean result) {
            Vote vote = Vote.APPROVE;

            for (SheetStateListener listener : this) {
                vote = vote.tally(listener.previewSheetClose(sheet, result));
            }

            return vote;
        }

        @Override
        public void sheetCloseVetoed(Sheet sheet, Vote reason) {
            for (SheetStateListener listener : this) {
                listener.sheetCloseVetoed(sheet, reason);
            }
        }

        @Override
        public void sheetClosed(Sheet sheet) {
            for (SheetStateListener listener : this) {
                listener.sheetClosed(sheet);
            }
        }
    }

    private SheetCloseListener sheetCloseListener = null;
    private boolean result = false;

    private boolean closing = false;

    private SheetStateListenerList sheetStateListeners = new SheetStateListenerList();

    /**
     * Creates a new sheet.
     */
    public Sheet() {
        this(null);
    }

    /**
     * Creates a new sheet with an initial content component.
     *
     * @param content
     * The sheet's content component.
     */
    public Sheet(Component content) {
        super(content);

        installSkin(Sheet.class);
    }

    public final void open(Window owner, SheetCloseListener sheetCloseListenerArgument) {
        if (owner == null) {
            throw new IllegalArgumentException("owner is null");
        }

        open(owner.getDisplay(), owner, sheetCloseListenerArgument);
    }

    @Override
    public final void open(Display display, Window owner) {
        open(display, owner, null);
    }

    public void open(Display display, Window owner, SheetCloseListener sheetCloseListenerArgument) {
        if (owner == null) {
            throw new IllegalArgumentException("Sheets must have an owner.");
        }

        this.sheetCloseListener = sheetCloseListenerArgument;

        super.open(display, owner);
    }

    @Override
    public boolean isClosing() {
        return closing;
    }

    @Override
    public final void close() {
        close(false);
    }

    public void close(boolean resultArgument) {
        if (!isClosed()) {
            closing = true;

            Vote vote = sheetStateListeners.previewSheetClose(this, resultArgument);

            if (vote == Vote.APPROVE) {
                Window owner = getOwner();

                super.close();

                closing = super.isClosing();

                if (isClosed()) {
                    this.result = resultArgument;

                    // Move the owner to the front
                    if (owner.isOpen()) {
                        owner.moveToFront();
                    }

                    // Notify listeners
                    sheetStateListeners.sheetClosed(this);

                    if (sheetCloseListener != null) {
                        sheetCloseListener.sheetClosed(this);
                        sheetCloseListener = null;
                    }
                }
            } else {
                if (vote == Vote.DENY) {
                    closing = false;
                }

                sheetStateListeners.sheetCloseVetoed(this, vote);
            }
        }
    }

    public SheetCloseListener getSheetCloseListener() {
        return sheetCloseListener;
    }

    public boolean getResult() {
        return result;
    }

    public ListenerList<SheetStateListener> getSheetStateListeners() {
        return sheetStateListeners;
    }
}
