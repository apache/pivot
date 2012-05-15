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
    private static class DialogListenerList extends WTKListenerList<DialogListener>
        implements DialogListener {
        @Override
        public void modalChanged(Dialog dialog) {
            for (DialogListener listener : this) {
                listener.modalChanged(dialog);
            }
        }
    }

    private static class DialogStateListenerList extends WTKListenerList<DialogStateListener>
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

    private boolean modal;
    private DialogCloseListener dialogCloseListener = null;

    private boolean result = false;

    private boolean closing = false;

    private DialogListenerList dialogListeners = new DialogListenerList();
    private DialogStateListenerList dialogStateListeners = new DialogStateListenerList();

    public Dialog() {
        this(true);
    }

    public Dialog(boolean modal) {
        this(null, null, modal);
    }

    public Dialog(String title) {
        this(title, true);
    }

    public Dialog(String title, boolean modal) {
        this(title, null, modal);
    }

    public Dialog(Component content) {
        this(content, true);
    }

    public Dialog(Component content, boolean modal) {
        this(null, content, modal);
    }

    public Dialog(String title, Component content) {
        this(title, content, true);
    }

    public Dialog(String title, Component content, boolean modal) {
        super(title, content);
        this.modal = modal;

        installSkin(Dialog.class);
    }

    public boolean isModal() {
        return modal;
    }

    public void setModal(boolean modal) {
        if (this.modal != modal) {
            this.modal = modal;
            dialogListeners.modalChanged(this);
        }
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
        open(display, owner, null);
    }

    /**
     * Opens the dialog.
     *
     * @param display
     * The display on which the dialog will be opened.
     *
     * @param dialogCloseListenerArgument
     * A listener that will be called when the dialog is closed.
     */
    public final void open(Display display, DialogCloseListener dialogCloseListenerArgument) {
        open(display, null, dialogCloseListenerArgument);
    }

    /**
     * Opens the dialog.
     *
     * @param owner
     * The window's owner. The dialog will be modal over this window.
     *
     * @param dialogCloseListenerArgument
     * A listener that will be called when the dialog is closed.
     */
    public final void open(Window owner, DialogCloseListener dialogCloseListenerArgument) {
        if (owner == null) {
            throw new IllegalArgumentException();
        }

        open(owner.getDisplay(), owner, dialogCloseListenerArgument);
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
     * @param dialogCloseListenerArgument
     * A listener that will be called when the dialog is closed.
     */
    public void open(Display display, Window owner, DialogCloseListener dialogCloseListenerArgument) {
        if (modal && owner == null) {
            throw new IllegalArgumentException("Modal dialogs must have an owner.");
        }

        this.dialogCloseListener = dialogCloseListenerArgument;
        result = false;

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

            Vote vote = dialogStateListeners.previewDialogClose(this, resultArgument);

            if (vote == Vote.APPROVE) {
                Window owner = getOwner();

                super.close();

                closing = super.isClosing();

                if (isClosed()) {
                    this.result = resultArgument;

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

    public DialogCloseListener getDialogCloseListener() {
        return dialogCloseListener;
    }

    public boolean getResult() {
        return result;
    }

    public ListenerList<DialogListener> getDialogListeners() {
        return dialogListeners;
    }

    public ListenerList<DialogStateListener> getDialogStateListeners() {
        return dialogStateListeners;
    }
}
