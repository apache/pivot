/*
 * Copyright (c) 2009 VMware, Inc.
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
package pivot.wtk.skin;

import java.awt.Graphics2D;

import pivot.wtk.Component;
import pivot.wtk.Dimensions;

/**
 * Terra text area skin.
 *
 * @author gbrown
 */
public class TextAreaSkin extends ContainerSkin {
    public void install(Component component) {
        super.install(component);

        // TODO
    }

    public void uninstall() {
        // TODO

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        // TODO
        return 0;
    }

    public int getPreferredHeight(int width) {
        // TODO
        return 0;
    }

    public Dimensions getPreferredSize() {
        // TODO
        return null;
    }

    public void layout() {
        // TODO
    }

    public void paint(Graphics2D graphics) {
        // TODO
    }

    @Override
    public boolean isFocusable() {
        // TODO Update Container#requestFocus() to only transfer focus to
        // first subcomponent if the container itself is not focusable
        return true;
    }
}
