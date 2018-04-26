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
package org.apache.pivot.wtk.content;

import java.awt.Color;

import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.VerticalAlignment;

/**
 * Default spinner item renderer, which renders all items as strings by calling
 * <tt>toString()</tt> on them.
 */
public class SpinnerItemRenderer extends Label implements Spinner.ItemRenderer {
    public SpinnerItemRenderer() {
        getStyles().put(Style.verticalAlignment, VerticalAlignment.CENTER);
        getStyles().put(Style.padding, new Insets(2));
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    @Override
    public void render(Object item, Spinner spinner) {
        String text = toString(item);
        setText(text != null ? text : "");

        renderStyles(spinner);
    }

    protected void renderStyles(Spinner spinner) {
        getStyles().copy(Style.font, spinner.getStyles());

        Color color;
        if (spinner.isEnabled()) {
            color = spinner.getStyles().getColor(Style.color);
        } else {
            color = spinner.getStyles().getColor(Style.disabledColor);
        }

        getStyles().put(Style.color, color);
    }

    @Override
    public String toString(Object item) {
        return (item == null) ? null : item.toString();
    }
}
