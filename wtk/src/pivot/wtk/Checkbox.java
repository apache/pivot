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
import pivot.wtk.skin.terra.CheckboxSkin;

public class Checkbox extends Button {
    public Checkbox() {
        this(null);
    }

    public Checkbox(Object buttonData) {
        super(buttonData);

        super.setToggleButton(true);

        if (getClass() == Checkbox.class) {
            setSkinClass(CheckboxSkin.class);
        }

        Button.DataRenderer dataRenderer = new ButtonDataRenderer();
        dataRenderer.getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
        setDataRenderer(dataRenderer);
    }

    public void press() {
        super.press();

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

    @Override
    public void setToggleButton(boolean toggleButton) {
        throw new UnsupportedOperationException("Checkboxes are always toggle buttons.");
    }

    @Override
    public void setGroup(Group group) {
        throw new UnsupportedOperationException("Checkboxes can't be added to a group.");
    }
}
