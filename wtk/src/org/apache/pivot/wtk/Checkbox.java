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

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.wtk.content.ButtonDataRenderer;

/**
 * Component representing a checkbox.
 */
@DefaultProperty("buttonData")
public class Checkbox extends Button {
    private static final ButtonDataRenderer DEFAULT_DATA_RENDERER = new ButtonDataRenderer();

    static {
        DEFAULT_DATA_RENDERER.getStyles().put(Style.horizontalAlignment, HorizontalAlignment.LEFT);
    }

    public Checkbox() {
        this(null);
    }

    public Checkbox(final Object buttonData) {
        super(buttonData);
        super.setToggleButton(true);

        setDataRenderer(DEFAULT_DATA_RENDERER);

        installSkin(Checkbox.class);
    }

    @Override
    public void press() {
        State state = getState();

        if (state == State.SELECTED) {
            setState(State.UNSELECTED);
        } else if (state == State.UNSELECTED) {
            setState(isTriState() ? State.MIXED : State.SELECTED);
        } else {
            setState(State.SELECTED);
        }

        super.press();
    }

    /**
     * This operation is not supported for checkboxes.
     *
     * @throws UnsupportedOperationException always since this is unsupported.
     */
    @Override
    @UnsupportedOperation
    public void setToggleButton(final boolean toggleButton) {
        throw new UnsupportedOperationException("Checkboxes are always toggle buttons.");
    }

    /**
     * This operation is not supported for checkboxes.
     *
     * @throws UnsupportedOperationException always since this is unsupported.
     */
    @Override
    @UnsupportedOperation
    public void setButtonGroup(final ButtonGroup buttonGroup) {
        throw new UnsupportedOperationException("Checkboxes can't be added to a group.");
    }
}
