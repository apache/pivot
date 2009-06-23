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
 *
 * @author gbrown
 */
public class Sheet extends Window {
    private static class SheetStateListenerList extends ListenerList<SheetStateListener>
        implements SheetStateListener {
        public Vote previewSheetClose(Sheet sheet, boolean result) {
            Vote vote = Vote.APPROVE;

            for (SheetStateListener listener : this) {
                vote = vote.tally(listener.previewSheetClose(sheet, result));
            }

            return vote;
        }

        public void sheetCloseVetoed(Sheet sheet, Vote reason) {
            for (SheetStateListener listener : this) {
                listener.sheetCloseVetoed(sheet, reason);
            }
        }

        public void sheetClosed(Sheet sheet) {
            for (SheetStateListener listener : this) {
                listener.sheetClosed(sheet);
            }
        }
    }

    private boolean result = false;
    private SheetCloseListener sheetCloseListener = null;

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
        super(content, false);

        installSkin(Sheet.class);
    }

    @Override
    public final void setOwner(Window owner) {
        if (owner == null) {
            throw new UnsupportedOperationException("A sheet must have an owner.");
        }

        if (owner.getContent() == null) {
            throw new UnsupportedOperationException("A sheet's owner must have a content component.");
        }

        super.setOwner(owner);
    }

    @Override
    public void open(Display display) {
        super.open(display);

        if (isOpen()) {
            Window owner = getOwner();

            Component content = owner.getContent();
            if (content.isBlocked()) {
                throw new IllegalStateException("Owner content is already blocked.");
            }

            content.setEnabled(false);
        }
    }

    public final void open(Window owner) {
        open(owner, null);
    }

    public void open(Window owner, SheetCloseListener sheetCloseListener) {
        super.open(owner);

        if (isOpen()) {
            this.sheetCloseListener = sheetCloseListener;
        }
    }

    @Override
    public final void close() {
        close(false);
    }

    public void close(boolean result) {
        if (!isClosed()) {
            Vote vote = sheetStateListeners.previewSheetClose(this, result);

            if (vote.isApproved()) {
                super.close();

                if (isClosed()) {
                    this.result = result;

                    Window owner = getOwner();
                    Component content = owner.getContent();
                    content.setEnabled(true);

                    if (owner.isOpen()) {
                        owner.moveToFront();
                    }

                    if (sheetCloseListener != null) {
                        sheetCloseListener.sheetClosed(this);
                        sheetCloseListener = null;
                    }

                    sheetStateListeners.sheetClosed(this);
                }
            } else {
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
