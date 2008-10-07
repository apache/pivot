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

import pivot.wtk.content.ButtonDataRenderer;

/**
 * Component representing a push button.
 *
 * @author gbrown
 */
@ComponentInfo(icon="PushButton.png")
public class PushButton extends Button {
    private static final Button.DataRenderer DEFAULT_DATA_RENDERER = new ButtonDataRenderer();

    public PushButton() {
        this(false, null);
    }

    public PushButton(boolean toggleButton) {
        this(toggleButton, null);
    }

    public PushButton(Object buttonData) {
        this(false, buttonData);
    }

    public PushButton(boolean toggleButton, Object buttonData) {
        super(buttonData);

        setToggleButton(toggleButton);
        setDataRenderer(DEFAULT_DATA_RENDERER);

        installSkin(PushButton.class);
    }

    public void press() {
        if (isToggleButton()) {
            // If the button is not part of a group, cycle through the
            // available states; otherwise, select the button
            if (getGroup() == null) {
                State state = getState();

                if (state == State.SELECTED) {
                    setState(State.UNSELECTED);
                }
                else if (state == State.UNSELECTED) {
                    setState(isTriState() ? State.MIXED : State.SELECTED);
                }
                else {
                    setState(State.SELECTED);
                }
            }
            else {
                setSelected(true);
            }
        }

        super.press();
    }
}
