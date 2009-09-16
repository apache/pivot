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

    @Override
    public final void setOwner(Window owner) {
        if (isOpen()
            && modal) {
            throw new IllegalStateException("Dialog is open.");
        }

        super.setOwner(owner);
    }

    /**
     * Opens the dialog.
     *
     * @param display
     */
    @Override
    public final void open(Display display) {
        open(display, true, null);
    }

    /**
     * Opens the dialog.
     *
     * @param display
     * @param modal
     */
    public final void open(Display display, boolean modal) {
        open(display, modal, null);
    }

    /**
     * Opens the dialog.
     *
     * @param display
     * @param dialogCloseListener
     */
    public final void open(Display display, DialogCloseListener dialogCloseListener) {
        open(display, true, dialogCloseListener);
    }

    /**
     * Opens the dialog.
     *
     * @param display
     * @param modal
     * @param dialogCloseListener
     */
    public void open(Display display, boolean modal, DialogCloseListener dialogCloseListener) {
        Window owner = getOwner();
        if (modal
            && owner == null) {
            throw new IllegalStateException("Dialog does not have an owner.");
        }

        super.open(display);

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
                super.close();

                closing = super.isClosing();

                if (isClosed()) {
                    this.result = result;

                    modal = false;

                    // Move the owner to the front
                    Window owner = getOwner();
                    if (owner != null
                        && owner.isOpen()) {
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
