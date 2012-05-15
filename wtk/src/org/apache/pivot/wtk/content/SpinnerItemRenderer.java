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
import java.awt.Font;

import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.VerticalAlignment;


/**
 * Default spinner item renderer, which renders all items as strings by
 * calling <tt>toString()</tt> on them.
 */
public class SpinnerItemRenderer extends Label implements Spinner.ItemRenderer {
    public SpinnerItemRenderer() {
        getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        getStyles().put("padding", new Insets(2));
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
        setText(item == null ? null : item.toString());

        renderStyles(spinner);
    }

    protected void renderStyles(Spinner spinner) {
        Font font = (Font)spinner.getStyles().get("font");
        getStyles().put("font", font);

        Color color;
        if (spinner.isEnabled()) {
            color = (Color)spinner.getStyles().get("color");
        } else {
            color = (Color)spinner.getStyles().get("disabledColor");
        }

        getStyles().put("color", color);
    }

    @Override
    public String toString(Object item) {
        return (item == null) ? null : item.toString();
    }
}
