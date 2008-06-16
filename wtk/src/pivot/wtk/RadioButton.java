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
import pivot.wtk.skin.terra.RadioButtonSkin;

public class RadioButton extends Button {
    public RadioButton() {
        this(null, null);
    }

    public RadioButton(Group group) {
        this(group, null);
    }

    public RadioButton(Object buttonData) {
        this(null, buttonData);
    }

    public RadioButton(Group group, Object buttonData) {
        super(buttonData);

        super.setToggleButton(true);

        if (getClass() == RadioButton.class) {
            setSkinClass(RadioButtonSkin.class);
        }

        Button.DataRenderer dataRenderer = new ButtonDataRenderer();
        dataRenderer.getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
        setDataRenderer(dataRenderer);

        this.setGroup(group);
    }

    public void press() {
        super.press();

        setSelected(getGroup() == null ? !isSelected() : true);
    }

    @Override
    public void setToggleButton(boolean toggleButton) {
        throw new UnsupportedOperationException("Radio buttons are always toggle buttons.");
    }

    @Override
    public void setTriState(boolean triState) {
        throw new UnsupportedOperationException("Radio buttons can't be tri-state.");
    }
}
