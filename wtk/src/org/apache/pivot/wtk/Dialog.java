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
        public void dialogClosed(Dialog dialog) {
            for (DialogStateListener listener : this) {
                listener.dialogClosed(dialog);
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
     */
    @Override
    public final void open(Display display) {
        open(display, null);
    }

    /**
     * Opens the dialog.
     *
     * @param display
     * @param dialogCloseListener
     */
    public void open(Display display, DialogCloseListener dialogCloseListener) {
        super.open(display);

        if (isOpen()) {
            this.dialogCloseListener = dialogCloseListener;
            this.modal = false;
        }
    }

    /**
     * Opens the dialog as modal over its owner.
     *
     * @param owner
     */
    @Override
    public final void open(Window owner) {
        open(owner, true, null);
    }

    /**
     * Opens the dialog.
     *
     * @param owner
     * @param modal
     */
    public final void open(Window owner, boolean modal) {
        open(owner, modal, null);
    }

    /**
     * Opens the dialog as modal over its owner.
     *
     * @param owner
     * The dialog's owner.
     *
     * @param dialogCloseListener
     * Optional dialog close listener to be called when the dialog is closed.
     */
    public final void open(Window owner, DialogCloseListener dialogCloseListener) {
        open(owner, true, dialogCloseListener);
    }

    /**
     * Opens the dialog.
     *
     * @param owner
     * The dialog's owner.
     *
     * @param modal
     * If <tt>true</tt>, the dialog is opened as modal, disabling its owner
     * tree.
     *
     * @param dialogCloseListener
     * Optional dialog close listener to be called when the dialog is closed.
     */
    public void open(Window owner, boolean modal, DialogCloseListener dialogCloseListener) {
        super.open(owner);

        if (isOpen()) {
            this.dialogCloseListener = dialogCloseListener;
            this.modal = modal;
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
                super.close();

                closing = super.isClosing();

                if (isClosed()) {
                    this.result = result;

                    modal = false;

                    // Move the owner to the front
                    Window owner = getOwner();
                    if (owner.isOpen()) {
                        owner.moveToFront();
                    }

                    // Notify listeners
                    if (dialogCloseListener != null) {
                        dialogCloseListener.dialogClosed(this);
                        dialogCloseListener = null;
                    }

                    dialogStateListeners.dialogClosed(this);
                }
            } else if (vote == Vote.DENY) {
                closing = false;
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
