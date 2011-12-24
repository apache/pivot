/*
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
package org.apache.pivot.wtk.skin.terra;

import java.awt.Toolkit;

import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.DialogListener;
import org.apache.pivot.wtk.DialogStateListener;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.Keyboard.KeyCode;

/**
 * Dialog skin.
 */
public class TerraDialogSkin extends TerraFrameSkin
    implements DialogListener, DialogStateListener {
    private static final float GOLDEN_SECTION = 0.382f;

    private ContainerMouseListener displayMouseListener = new ContainerMouseListener.Adapter() {
        @Override
        public boolean mouseMove(Container display, int x, int y) {
            return isMouseOverOwner(display, x, y);
        }

        @Override
        public boolean mouseDown(Container display, Mouse.Button button, int x, int y) {
            boolean consumed = false;

            Dialog dialog = (Dialog)getComponent();
            if (isMouseOverOwner(display, x, y)) {
                Window rootOwner = dialog.getRootOwner();
                rootOwner.moveToFront();
                consumed = true;

                Toolkit.getDefaultToolkit().beep();
            }

            int top = display.getLength() - 1;
            int index = display.indexOf(dialog);
            if (index == top) {
                dialog.requestActive();
            }

            return consumed;
        }

        @Override
        public boolean mouseUp(Container display, Mouse.Button button, int x, int y) {
            return isMouseOverOwner(display, x, y);
        }

        @Override
        public boolean mouseWheel(Container display, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            return isMouseOverOwner(display, x, y);
        }

        private boolean isMouseOverOwner(Container display, int x, int y) {
            boolean mouseOverOwner = false;

            Dialog dialog = (Dialog)getComponent();
            if (dialog.isModal()) {
                Component descendant = display.getDescendantAt(x, y);

                if (descendant != display) {
                    Window window = descendant.getWindow();
                    mouseOverOwner = (dialog.getOwner() == window);
                }
            }

            return mouseOverOwner;
        }
    };

    @Override
    public void install(Component component) {
        super.install(component);

        Dialog dialog = (Dialog)component;
        dialog.getDialogStateListeners().add(this);

        setShowMaximizeButton(false);
        setShowMinimizeButton(false);
    }

    @Override
    public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
        Dialog dialog = (Dialog)container;
        if (!dialog.isTopMost()) {
            Window rootOwner = dialog.getRootOwner();
            rootOwner.moveToFront();
        }

        return super.mouseDown(container, button, x, y);
    }

    /**
     * {@link KeyCode#ENTER ENTER} Close the dialog with a 'result' of true.<br>
     * {@link KeyCode#ESCAPE ESCAPE} Close the dialog with a 'result' of false.
     */
    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        Dialog dialog = (Dialog)getComponent();

        if (keyCode == Keyboard.KeyCode.ENTER) {
            dialog.close(true);
            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.ESCAPE) {
            dialog.close(false);
            consumed = true;
        } else {
            consumed = super.keyPressed(component, keyCode, keyLocation);
        }

        return consumed;
    }

    @Override
    public void windowOpened(Window window) {
        super.windowOpened(window);

        Display display = window.getDisplay();
        display.getContainerMouseListeners().add(displayMouseListener);
        display.reenterMouse();

        if (!window.requestFocus()) {
            Component.clearFocus();
        }

        // Center the dialog over its owner
        Container ancestor = window.getOwner();

        if (ancestor == null) {
            ancestor = window.getDisplay();
        }

        Dimensions size = window.getPreferredSize();
        int deltaWidth = ancestor.getWidth() - size.width;
        int deltaHeight = ancestor.getHeight() - size.height;

        int x = Math.max(0, Math.round(ancestor.getX() + 0.5f * deltaWidth));
        int y = Math.max(0, Math.round(ancestor.getY() + GOLDEN_SECTION * deltaHeight));

        window.setLocation(x, y);
    }

    @Override
    public void windowClosed(Window window, Display display, Window owner) {
        super.windowClosed(window, display, owner);
        display.getContainerMouseListeners().remove(displayMouseListener);
    }

    @Override
    public void modalChanged(Dialog dialog) {
        // No-op
    }

    @Override
    public Vote previewDialogClose(Dialog dialog, boolean result) {
        return Vote.APPROVE;
    }

    @Override
    public void dialogCloseVetoed(Dialog dialog, Vote reason) {
        // No-op
    }

    @Override
    public void dialogClosed(Dialog dialog, boolean modal) {
        // No-op
    }
}
