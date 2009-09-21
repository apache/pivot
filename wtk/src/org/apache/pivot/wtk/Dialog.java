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
 * Window class whose primary purpose is to facilitate interaction between
 * an application and a user.
 */
public class Dialog extends Frame {
    private static class DialogStateListenerList extends ListenerList<DialogStateListener>
        implements DialogStateListener {
        @Override
        public Vote previewDialogClose(Dialog dialog, boolean result) {
            Vote vote = Vote.APPROVE;

            for (DialogStateListener listener : this) {
                vote = vote.tally(listener.previewDialogClose(dialog, result));
            }

            return vote;
        }

        @Override
        public void dialogCloseVetoed(Dialog dialog, Vote reason) {
            for (DialogStateListener listener : this) {
                listener.dialogCloseVetoed(dialog, reason);
            }
        }

        @Override
        public void dialogClosed(Dialog dialog, boolean modal) {
            for (DialogStateListener listener : this) {
                listener.dialogClosed(dialog, modal);
            }
        }
    }

    private boolean modal = false;
    private DialogCloseListener dialogCloseListener = null;
    private boolean result = false;

    private boolean closing = false;

    private DialogStateListenerList dialogStateListeners = new DialogStateListenerList();

    public Dialog() {
        this(null, null);
    }

    public Dialog(String title) {
        this(title, null);
    }

    public Dialog(Component content) {
        this(null, content);
    }

    public Dialog(String title, Component content) {
        super(title, content);
        installSkin(Dialog.class);
    }

    /**
     * Opens the dialog.
     *
     * @param display
     * The display on which the dialog will be opened.
     *
     * @param owner
     * The window's owner. The dialog will be modal over this window.
     */
    @Override
    public final void open(Display display, Window owner) {
        open(display, owner, owner != null, null);
    }

    /**
     * Opens the dialog.
     *
     * @param display
     * The display on which the dialog will be opened.
     *
     * @param dialogCloseListener
     * A listener that will be called when the dialog is closed.
     */
    public final void open(Display display, DialogCloseListener dialogCloseListener) {
        open(display, null, false, dialogCloseListener);
    }

    /**
     * Opens the dialog.
     *
     * @param owner
     * The window's owner. The dialog will be modal over this window.
     *
     * @param dialogCloseListener
     * A listener that will be called when the dialog is closed.
     */
    public final void open(Window owner, DialogCloseListener dialogCloseListener) {
        if (owner == null) {
            throw new IllegalArgumentException();
        }

        open(owner.getDisplay(), owner, true, dialogCloseListener);
    }

    /**
     * Opens the dialog.
     *
     * @param display
     * The display on which the dialog will be opened.
     *
     * @param owner
     * The window's owner, or <tt>null</tt> if the window has no owner. Required if the dialog
     * is modal.
     *
     * @param modal
     * <tt>true</tt> if the dialog should be modal; <tt>false</tt>, otherwise.
     *
     * @param dialogCloseListener
     * A listener that will be called when the dialog is closed.
     */
    public void open(Display display, Window owner, boolean modal, DialogCloseListener dialogCloseListener) {
        if (modal
            && owner == null) {
            throw new IllegalArgumentException("Modal dialogs must have an owner.");
        }

        super.open(display, owner);

        if (isOpen()) {
            this.modal = modal;
            this.dialogCloseListener = dialogCloseListener;

            result = false;
        }
    }

    @Override
    public boolean isClosing() {
        return closing;
    }

    @Override
    public final void close() {
        close(false);
    }

    public void close(boolean result) {
        if (!isClosed()) {
            closing = true;

            Vote vote = dialogStateListeners.previewDialogClose(this, result);

            if (vote == Vote.APPROVE) {
                Window owner = getOwner();

                super.close();

                closing = super.isClosing();

                if (isClosed()) {
                    this.result = result;

                    boolean modal = this.modal;
                    this.modal = false;

                    // Move the owner to the front
                    if (owner != null
                        && owner.isOpen()) {
                        owner.moveToFront();
                    }

                    // Notify listeners
                    dialogStateListeners.dialogClosed(this, modal);

                    if (dialogCloseListener != null) {
                        dialogCloseListener.dialogClosed(this, modal);
                        dialogCloseListener = null;
                    }
                }
            } else {
                if (vote == Vote.DENY) {
                    closing = false;
                }

                dialogStateListeners.dialogCloseVetoed(this, vote);
            }
        }
    }

    public boolean isModal() {
        return modal;
    }

    public DialogCloseListener getDialogCloseListener() {
        return dialogCloseListener;
    }

    public boolean getResult() {
        return result;
    }

    public ListenerList<DialogStateListener> getDialogStateListeners() {
        return dialogStateListeners;
    }
}
