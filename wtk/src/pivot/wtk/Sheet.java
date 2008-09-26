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
 * <p>Window class representing a "sheet". A sheet behaves like a dialog that is
 * modal only over a window's content component.</p>
 *
 * @author gbrown
 */
public class Sheet extends Window {
    /**
     * <p>Sheet skin interface.</p>
     *
     * @author gbrown
     */
    public interface Skin extends pivot.wtk.Skin, SheetStateListener {
    }

    private boolean result = false;
    private SheetStateListener sheetStateListener = null;

    private ComponentListener ownerListener = new ComponentListener() {
        public void parentChanged(Component component, Container previousParent) {
            // No-op
        }

        public void sizeChanged(Component component, int previousWidth, int previousHeight) {
            alignToOwnerContent();
        }

        public void locationChanged(Component component, int previousX, int previousY) {
            alignToOwnerContent();
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
     * <tt>true</tt>; by default, sheets are auxilliary windows.
     */
    @Override
    public boolean isAuxilliary() {
        return true;
    }

    @Override
    protected void setSkin(pivot.wtk.Skin skin) {
        if (!(skin instanceof Sheet.Skin)) {
            throw new IllegalArgumentException("Skin class must implement "
                + Sheet.Skin.class.getName());
        }

        super.setSkin(skin);
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
            owner.getComponentListeners().add(ownerListener);

            Component content = owner.getContent();
            content.setEnabled(false);

            ApplicationContext.queueCallback(new Runnable() {
                public void run() {
                    alignToOwnerContent();
                }
            });
        }
    }

    public final void open(Window owner) {
        open(owner, null);
    }

    public void open(Window owner, SheetStateListener sheetStateListener) {
        super.open(owner);

        if (isOpen()) {
            this.sheetStateListener = sheetStateListener;
        }
    }

    @Override
    public final void close() {
        close(false);
    }

    public void close(boolean result) {
        Sheet.Skin sheetSkin = (Sheet.Skin)getSkin();

        if (!isClosed()
            && (sheetStateListener == null
                || sheetStateListener.previewSheetClose(this, result))
            && sheetSkin.previewSheetClose(this, result)) {
            super.close();

            if (isClosed()) {
                this.result = result;

                Window owner = getOwner();
                owner.getComponentListeners().remove(ownerListener);

                Component content = owner.getContent();
                content.setEnabled(true);

                owner.moveToFront();

                if (sheetStateListener != null) {
                    sheetSkin.sheetClosed(this);
                    sheetStateListener.sheetClosed(this);
                }

                sheetStateListener = null;
            }
        }
    }

    public SheetStateListener getSheetStateListener() {
        return sheetStateListener;
    }

    public boolean getResult() {
        return result;
    }

    private void alignToOwnerContent() {
        Window owner = getOwner();
        Component content = owner.getContent();
        Point contentLocation = content.mapPointToAncestor(owner.getDisplay(), 0, 0);
        setLocation(contentLocation.x + (content.getWidth() - getWidth()) / 2,
            contentLocation.y);
    }
}
