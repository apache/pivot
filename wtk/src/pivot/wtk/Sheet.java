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

public class Sheet extends Window {
    private boolean result = false;
    private SheetStateListener sheetStateListener = null;

    private ComponentListener ownerListener = new ComponentListener() {
        public void parentChanged(Component component, Container previousParent) {
            // No-op
        }

        public void sizeChanged(Component component, int previousWidth, int previousHeight) {
            ApplicationContext.queueCallback(repositionCallback);
        }

        public void locationChanged(Component component, int previousX, int previousY) {
            ApplicationContext.queueCallback(repositionCallback);
        }

        public void visibleChanged(Component component) {
            // No-op
        }

        public void styleUpdated(Component component, String styleKey, Object previousValue) {
            // No-op
        }

        public void cursorChanged(Component component, Cursor previousCursor) {
            // No-op
        }

        public void tooltipTextChanged(Component component, String previousTooltipText) {
            // No-op
        }
    };

    private Runnable repositionCallback = new Runnable() {
        public void run() {
            Window owner = getOwner();
            Component content = owner.getContent();
            Point contentLocation = content.mapPointToAncestor(owner.getDisplay(), 0, 0);
            setLocation(contentLocation.x + (content.getWidth() - getWidth()) / 2,
                contentLocation.y);
        }
    };

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

    /**
     * @return
     * <tt>true</tt>; by default, popups are auxilliary windows.
     */
    @Override
    public boolean isAuxilliary() {
        return true;
    }

    /**
     * Opens the sheet.
     *
     * @throws UnsupportedOperationException
     * If the sheet does not have an owner, or the owner has no content.
     */
    public final void open(Display display) {
        Window owner = getOwner();
        if (owner == null) {
            throw new UnsupportedOperationException("A sheet must have an owner.");
        }

        if (owner.getContent() == null) {
            throw new UnsupportedOperationException("A sheet's owner must have a content component.");
        }

        super.open(display);
    }

    /**
     * Opens the sheet.
     *
     * @param owner
     * The sheet's owner.
     */
    @Override
    public final void open(Window owner) {
        open(owner, null);
    }

    /**
     * Opens the sheet.
     *
     * @param owner
     * The sheet's owner.
     *
     * @param sheetStateListener
     * Optional sheet state listener to be called when the sheet is closed.
     */
    public void open(Window owner, SheetStateListener sheetStateListener) {
        if (owner == null) {
            throw new UnsupportedOperationException("A sheet must have an owner.");
        }

        Component content = owner.getContent();

        if (content == null) {
            throw new UnsupportedOperationException("A sheet's owner must have a content component.");
        }

        if (isOpen()) {
            throw new IllegalStateException("Sheet is already open.");
        }

        this.sheetStateListener = sheetStateListener;

        super.open(owner);

        if (isOpen()) {
            content.setEnabled(false);
            owner.getComponentListeners().add(ownerListener);
            ApplicationContext.queueCallback(repositionCallback);
        } else {
            // A preview listener vetoed the open event
            this.sheetStateListener = null;
        }
    }

    @Override
    public final void close() {
        close(false);
    }

    public void close(boolean result) {
        if (!isClosed()
            && (sheetStateListener == null
                || sheetStateListener.previewSheetClose(this, result))) {
            Window owner = getOwner();
            Component content = owner.getContent();

            super.close();

            if (isClosed()) {
                this.result = result;

                content.setEnabled(true);
                owner.getComponentListeners().remove(ownerListener);

                owner.moveToFront();

                if (sheetStateListener != null) {
                    sheetStateListener.sheetClosed(this);
                }

                sheetStateListener = null;
            }
        }
    }

    public boolean getResult() {
        return result;
    }

    public SheetStateListener getSheetStateListener() {
        return sheetStateListener;
    }
}
