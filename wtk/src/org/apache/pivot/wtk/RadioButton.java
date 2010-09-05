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
 * Component representing a "radio button".
 */
@DefaultProperty("buttonData")
public class RadioButton extends Button {
    private static final Button.DataRenderer DEFAULT_DATA_RENDERER = new ButtonDataRenderer();

    static {
        DEFAULT_DATA_RENDERER.getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
    }

    public RadioButton() {
        this(null, null);
    }

    public RadioButton(ButtonGroup buttonGroup) {
        this(buttonGroup, null);
    }

    public RadioButton(Object buttonData) {
        this(null, buttonData);
    }

    public RadioButton(ButtonGroup buttonGroup, Object buttonData) {
        super(buttonData);
        super.setToggleButton(true);

        setButtonGroup(buttonGroup);
        setDataRenderer(DEFAULT_DATA_RENDERER);

        installSkin(RadioButton.class);
    }

    @Override
    public void press() {
        setSelected(true);

        super.press();
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
