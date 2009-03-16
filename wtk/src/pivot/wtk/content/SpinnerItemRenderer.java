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
package pivot.wtk.content;

import java.awt.Color;
import java.awt.Font;

import pivot.wtk.Component;
import pivot.wtk.Insets;
import pivot.wtk.Label;
import pivot.wtk.Spinner;
import pivot.wtk.VerticalAlignment;

/**
 * Default spinner item renderer, which renders all items as strings by
 * calling <tt>toString()</tt> on them.
 *
 * @author tvolkert
 */
public class SpinnerItemRenderer extends Label implements Spinner.ItemRenderer {
    public SpinnerItemRenderer() {
        getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        getStyles().put("padding", new Insets(2));
    }

    public void render(Object item, Spinner spinner) {
        setText(item == null ? null : item.toString());

        renderStyles(spinner);
    }

    protected void renderStyles(Spinner spinner) {
        Component.StyleDictionary spinnerStyles = spinner.getStyles();
        Component.StyleDictionary styles = getStyles();

        Object font = spinnerStyles.get("font");

        if (font instanceof Font) {
            styles.put("font", font);
        }

        Object color = null;

        if (spinner.isEnabled()) {
            color = spinnerStyles.get("color");
        } else {
            color = spinnerStyles.get("disabledColor");
        }

        if (color instanceof Color) {
            styles.put("color", color);
        }
    }
}
