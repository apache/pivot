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
package pivot.wtk.skin.terra;

import pivot.wtk.Component;
import pivot.wtk.Dialog;
import pivot.wtk.Keyboard;

/**
 * Dialog skin.
 *
 * @author gbrown
 */
public class DialogSkin extends FrameSkin implements Dialog.Skin {
    @Override
    public void install(Component component) {
        super.install(component);

        setShowMaximizeButton(false);
        setShowMinimizeButton(false);
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

    public boolean previewDialogClose(Dialog dialog, boolean result) {
        return true;
    }

    public void dialogClosed(Dialog dialog) {
        // No-op
    }
}
