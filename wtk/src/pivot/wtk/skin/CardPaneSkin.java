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

import pivot.wtk.CardPane;
import pivot.wtk.CardPaneListener;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;

/**
 * <p>Card pane skin.</p>
 *
 * @author gbrown
 */
public class CardPaneSkin extends ContainerSkin implements CardPaneListener {
    public void install(Component component) {
        validateComponentType(component, CardPane.class);

        super.install(component);

        CardPane cardPane = (CardPane)component;
        cardPane.getCardPaneListeners().add(this);
    }

    public void uninstall() {
        CardPane cardPane = (CardPane)getComponent();
        cardPane.getCardPaneListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;
        CardPane cardPane = (CardPane)getComponent();

        for (Component component : cardPane) {
            preferredWidth = Math.max(preferredWidth,
                component.getPreferredWidth(height));
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;
        CardPane cardPane = (CardPane)getComponent();

        for (Component component : cardPane) {
            preferredHeight = Math.max(preferredHeight,
                component.getPreferredHeight(width));
        }

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        CardPane cardPane = (CardPane)getComponent();

        for (Component component : cardPane) {
            Dimensions preferredCardSize = component.getPreferredSize();

            preferredWidth = Math.max(preferredWidth,
                preferredCardSize.width);

            preferredHeight = Math.max(preferredHeight,
                preferredCardSize.height);
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    public void layout() {
        // Hide all cards but the selected card; set the size of the selected
        // card to match the size of the card pane
        CardPane cardPane = (CardPane)getComponent();
        int selectedIndex = cardPane.getSelectedIndex();

        for (int i = 0, n = cardPane.getLength(); i < n; i++) {
            Component component = cardPane.get(i);
            if (i == selectedIndex) {
                component.setVisible(true);
                component.setSize(getWidth(), getHeight());
            } else {
                component.setVisible(false);
            }
        }
    }

    public void selectedIndexChanged(CardPane cardPane, int previousSelectedIndex) {
        invalidateComponent();
    }
}
