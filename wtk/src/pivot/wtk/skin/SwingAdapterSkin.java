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
package pivot.wtk.skin;

import java.awt.Graphics2D;
import javax.swing.JComponent;

import pivot.wtk.Component;
import pivot.wtk.SwingAdapter;
import pivot.wtk.SwingAdapterListener;

/**
 * Swing adapter skin.
 *
 * @author tvolkert
 */
public class SwingAdapterSkin extends ComponentSkin implements SwingAdapterListener {
    @Override
    public void install(Component component) {
        super.install(component);

        SwingAdapter swingAdapter = (SwingAdapter)component;
        swingAdapter.getSwingAdapterListeners().add(this);
    }

    @Override
    public void uninstall() {
        SwingAdapter swingAdapter = (SwingAdapter)getComponent();
        swingAdapter.getSwingAdapterListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        //SwingAdapter swingAdapter = (SwingAdapter)getComponent();

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        //SwingAdapter swingAdapter = (SwingAdapter)getComponent();

        return preferredHeight;
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
    }

    // SwingAdapterListener methods

    public void swingComponentChanged(SwingAdapter swingAdapter,
        JComponent previousSwingComponent) {
    }
}
