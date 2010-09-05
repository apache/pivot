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

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.wtk.content.ButtonDataRenderer;

/**
 * Component representing a push button.
 */
@DefaultProperty("buttonData")
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

    @Override
    public void press() {
        if (isToggleButton()) {
            State state = getState();

            if (getButtonGroup() == null) {
                if (state == State.SELECTED) {
                    setState(State.UNSELECTED);
                }
                else if (state == State.UNSELECTED) {
                    setState(isTriState() ? State.MIXED : State.SELECTED);
                }
                else {
                    setState(State.SELECTED);
                }
            } else {
                setSelected(true);
            }
        }

        super.press();
    }
}
