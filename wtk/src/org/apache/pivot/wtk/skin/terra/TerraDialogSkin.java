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

import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.DialogStateListener;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Window;


/**
 * Dialog skin.
 *
 * @author gbrown
 */
public class TerraDialogSkin extends TerraFrameSkin implements DialogStateListener {
    private class RepositionCallback implements Runnable {
        private static final float GOLDEN_SECTION = 0.382f;

        public void run() {
            Dialog dialog = (Dialog)getComponent();
            Component owner = dialog.getOwner();

            if (owner == null) {
                owner = dialog.getDisplay();
            }

            int deltaWidth = owner.getWidth() - getWidth();
            int deltaHeight = owner.getHeight() - getHeight();

            int x = Math.max(0, Math.round(owner.getX() + 0.5f * deltaWidth));
            int y = Math.max(0, Math.round(owner.getY() + GOLDEN_SECTION * deltaHeight));

            dialog.setLocation(x, y);
        }
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Dialog dialog = (Dialog)component;
        dialog.getDialogStateListeners().add(this);

        setShowMaximizeButton(false);
        setShowMinimizeButton(false);
    }

    @Override
    public void uninstall() {
        Dialog dialog = (Dialog)getComponent();
        dialog.getDialogStateListeners().remove(this);

        super.uninstall();
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

        ApplicationContext.queueCallback(new RepositionCallback());
    }

    public Vote previewDialogClose(Dialog dialog, boolean result) {
        return Vote.APPROVE;
    }

    public void dialogCloseVetoed(Dialog dialog, Vote reason) {
        // No-op
    }

    public void dialogClosed(Dialog dialog) {
        // No-op
    }
}
