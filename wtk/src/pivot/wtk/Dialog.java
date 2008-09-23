/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk;

/**
 * <p>Window class whose primary purpose is to facilitate interaction between
 * an application and a user.</p>
 *
 * @author gbrown
 */
public class Dialog extends Window {
    private class RepositionCallback implements Runnable {
        private static final float GOLDEN_SECTION = 0.382f;

        public void run() {
            Window owner = getOwner();

            if (owner == null) {
                throw new IllegalStateException("Dialog has no owner.");
            }

            int deltaWidth = owner.getWidth() - getWidth();
            int deltaHeight = owner.getHeight() - getHeight();

            int x = Math.max(0, Math.round(owner.getX() + 0.5f * deltaWidth));
            int y = Math.max(0, Math.round(owner.getY() + GOLDEN_SECTION * deltaHeight));

            setLocation(x, y);
        }
    }

    private boolean modal = false;
    private boolean result = false;

    private DialogStateListener dialogStateListener = null;
    private Window disabledOwner = null;

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
     */
    public final void open(Display display) {
        super.open(display);
    }

    /**
     * Opens the dialog.
     *
     * @param display
     * The display on which the dialog will be opened.
     *
     * @param dialogStateListener
     * Optional dialog state listener to be called when the dialog is closed.
     */
    public void open(Display display, DialogStateListener dialogStateListener) {
        if (isOpen()) {
            throw new IllegalStateException("Dialog is already open.");
        }

        this.dialogStateListener = dialogStateListener;
        this.modal = false;

        super.open(display);

        if (!isOpen()) {
            this.dialogStateListener = null;
        }
    }

    /**
     * Opens the dialog.
     *
     * @param owner
     * The dialog's owner. If <tt>null</tt>, the dialog is opened non-modal.
     * Otherwise, it is opened as modal.
     */
    @Override
    public final void open(Window owner) {
        open(owner, true, null);
    }

    /**
     * Opens the dialog.
     *
     * @param owner
     * The dialog's owner. If <tt>null</tt>, the dialog is opened non-modal.
     * Otherwise, it is opened as modal.
     *
     * @param dialogStateListener
     * Optional dialog state listener to be called when the dialog is closed.
     */
    public final void open(Window owner, DialogStateListener dialogStateListener) {
        open(owner, true, dialogStateListener);
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
     * @param dialogStateListener
     * Optional dialog state listener to be called when the dialog is closed.
     */
    public void open(Window owner, boolean modal, DialogStateListener dialogStateListener) {
        if (isOpen()) {
            throw new IllegalStateException("Dialog is already open.");
        }

        this.dialogStateListener = dialogStateListener;
        this.modal = modal;

        // Call the base method
        super.open(owner);

        if (isOpen()) {
            if (modal) {
                // Walk owner tree to find the nearest enabled owning ancestor
                // and disable it
                Window disabledOwner = null;

                while (owner != null
                    && owner.isEnabled()) {
                    disabledOwner = owner;
                    owner = owner.getOwner();
                }

                // Disable the ancestor and maintain a reference to it so we can
                // enable it when this dialog is closed
                if (disabledOwner != null) {
                    disabledOwner.setEnabled(false);
                }

                this.disabledOwner = disabledOwner;

                // Disabling the owner tree also disabled this dialog; re-enable it
                // and make it the active window
                setEnabled(true);
                setActiveWindow(this);

                // Align the dialog with its owner
                ApplicationContext.queueCallback(new RepositionCallback());
            }
        } else {
            this.dialogStateListener = null;
            this.modal = false;
        }
    }

    @Override
    public final void close() {
        close(false);
    }

    public void close(boolean result) {
        if (!isClosed()
            && (dialogStateListener == null
                || dialogStateListener.previewDialogClose(this, result))) {
            // Close the window
            super.close();

            // Only proceed if the state listeners allowed us to close
            if (isClosed()) {
                this.result = result;

                // Enable the ancestor that was disabled when this dialog
                // was opened
                if (disabledOwner != null) {
                    disabledOwner.setEnabled(true);

                    // Move the owner to the front
                    if (modal) {
                        disabledOwner.moveToFront();
                    }
                }

                modal = false;
                disabledOwner = null;

                // Notify listener
                if (dialogStateListener != null) {
                    dialogStateListener.dialogClosed(this);
                }

                dialogStateListener = null;
            }
        }
    }

    public boolean isModal() {
        return modal;
    }

    public boolean getResult() {
        return result;
    }

    public Window getDisabledOwner() {
        return disabledOwner;
    }

    public DialogStateListener getDialogStateListener() {
        return dialogStateListener;
    }
}
