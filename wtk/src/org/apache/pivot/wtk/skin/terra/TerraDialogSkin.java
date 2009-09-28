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
package org.apache.pivot.wtk.skin.terra;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.DialogStateListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Window;

/**
 * Dialog skin.
 */
public class TerraDialogSkin extends TerraFrameSkin implements DialogStateListener {
    private class RepositionCallback implements Runnable {
        private static final float GOLDEN_SECTION = 0.382f;

        @Override
        public void run() {
            Dialog dialog = (Dialog)getComponent();
            Container ancestor = dialog.getOwner();

            if (ancestor == null) {
                ancestor = dialog.getDisplay();
            }

            int deltaWidth = ancestor.getWidth() - getWidth();
            int deltaHeight = ancestor.getHeight() - getHeight();

            int x = Math.max(0, Math.round(ancestor.getX() + 0.5f * deltaWidth));
            int y = Math.max(0, Math.round(ancestor.getY() + GOLDEN_SECTION * deltaHeight));

            dialog.setLocation(x, y);
        }
    }

    private ContainerMouseListener displayMouseListener = new ContainerMouseListener.Adapter() {
        @Override
        public boolean mouseMove(Container display, int x, int y) {
            return isMouseOverRelative(display, x, y);
        }

        @Override
        public boolean mouseDown(Container display, Mouse.Button button, int x, int y) {
            boolean consumed = false;

            if (isMouseOverRelative(display, x, y)) {
                Dialog dialog = (Dialog)getComponent();
                Window rootOwner = dialog.getRootOwner();
                rootOwner.moveToFront();
                dialog.requestActive();
                consumed = true;

                ApplicationContext.beep();
            }

            return consumed;
        }

        @Override
        public boolean mouseUp(Container display, Mouse.Button button, int x, int y) {
            return isMouseOverRelative(display, x, y);
        }

        @Override
        public boolean mouseWheel(Container display, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            return isMouseOverRelative(display, x, y);
        }

        private boolean isMouseOverRelative(Container display, int x, int y) {
            boolean mouseOverOwner = false;

            Dialog dialog = (Dialog)getComponent();

            if (dialog.isModal()) {
                Component descendant = display.getDescendantAt(x, y);

                if (descendant != display) {
                    Window window = descendant.getWindow();

                    if (window != dialog) {
                        // Compare the windows' owner paths (including the windows themselves);
                        // the windows are relatives if they have any ancestry in common
                        ArrayList<Window> dialogOwnerPath = new ArrayList<Window>();
                        Window dialogOwner = dialog;
                        while (dialogOwner != null) {
                            dialogOwnerPath.insert(dialogOwner, 0);
                            dialogOwner = dialogOwner.getOwner();
                        }

                        ArrayList<Window> windowOwnerPath = new ArrayList<Window>();
                        Window windowOwner = window;
                        while (windowOwner != null) {
                            windowOwnerPath.insert(windowOwner, 0);
                            windowOwner = windowOwner.getOwner();
                        }

                        int i = 0;
                        while (i < dialogOwnerPath.getLength()
                            && i < windowOwnerPath.getLength()
                            && dialogOwnerPath.get(i) == windowOwnerPath.get(i)) {
                            i++;
                        }

                        mouseOverOwner = (i > 0);
                    }
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
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        Dialog dialog = (Dialog)component;
        Window rootOwner = dialog.getRootOwner();
        rootOwner.moveToFront();

        return super.mouseDown(component, button, x, y);
    }

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

        if (!window.requestFocus()) {
            Component.clearFocus();
        }

        ApplicationContext.queueCallback(new RepositionCallback());
    }

    @Override
    public void windowClosed(Window window, Display display, Window owner) {
        super.windowClosed(window, display, owner);

        display.getContainerMouseListeners().remove(displayMouseListener);
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
