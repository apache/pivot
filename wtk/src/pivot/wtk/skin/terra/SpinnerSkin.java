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
package pivot.wtk.skin.terra;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import pivot.collections.List;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Insets;
import pivot.wtk.PushButton;
import pivot.wtk.Spinner;
import pivot.wtk.SpinnerListener;
import pivot.wtk.SpinnerSelectionListener;
import pivot.wtk.skin.ContainerSkin;

/**
 * Spinner skin
 *
 * @author tvolkert
 */
public class SpinnerSkin extends ContainerSkin
    implements SpinnerListener, SpinnerSelectionListener, ButtonPressListener {

    private PushButton upButton = new PushButton("^");
    private PushButton downButton = new PushButton("v");

    private Color color = Color.BLACK;
    private Color disabledColor = new Color(0x99, 0x99, 0x99);
    private Color borderColor = new Color(0x99, 0x99, 0x99);
    private Font font = new Font("Verdana", Font.PLAIN, 11);

    public SpinnerSkin() {
        upButton.getStyles().put("padding", new Insets(0));
        downButton.getStyles().put("padding", new Insets(0));
    }

    @Override
    public void install(Component component) {
        validateComponentType(component, Spinner.class);

        super.install(component);

        Spinner spinner = (Spinner)component;
        spinner.getSpinnerListeners().add(this);
        spinner.getSpinnerSelectionListeners().add(this);

        spinner.getComponents().add(upButton);
        spinner.getComponents().add(downButton);
        upButton.getButtonPressListeners().add(this);
        downButton.getButtonPressListeners().add(this);
    }

    @Override
    public void uninstall() {
        Spinner spinner = (Spinner)getComponent();
        spinner.getSpinnerListeners().remove(this);
        spinner.getSpinnerSelectionListeners().remove(this);

        spinner.getComponents().remove(upButton);
        spinner.getComponents().remove(downButton);
        upButton.getButtonPressListeners().remove(this);
        downButton.getButtonPressListeners().remove(this);

        super.uninstall();
    }

    @Override
    public int getPreferredWidth(int height) {
        Spinner spinner = (Spinner)getComponent();
        Spinner.ItemRenderer itemRenderer = spinner.getItemRenderer();

        // Preferred width is the sum of our maximum button width plus the
        // renderer width, plus the border

        // Border thickness
        int preferredWidth = 2;

        int buttonHeight = (height < 0 ? -1 : height / 2);
        preferredWidth += Math.max(upButton.getPreferredWidth(buttonHeight),
            downButton.getPreferredWidth(buttonHeight));

        if (height >= 0) {
            // Subtract border thickness from height constraint
            height = Math.max(height - 2, 0);
        }

        itemRenderer.render(spinner.getSelectedValue(), spinner);
        preferredWidth += itemRenderer.getPreferredWidth(height);

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        Spinner spinner = (Spinner)getComponent();
        Spinner.ItemRenderer itemRenderer = spinner.getItemRenderer();

        // Preferred height is the maximum of the button height and the
        // renderer's preferred height (plus the border), where button
        // height is defined as the larger of the two buttons' preferred
        // height, doubled.

        Dimensions upButtonPreferredSize = upButton.getPreferredSize();
        Dimensions downButtonPreferredSize = downButton.getPreferredSize();

        int preferredHeight = Math.max(upButtonPreferredSize.height,
            downButtonPreferredSize.height) * 2;

        if (width >= 0) {
            // Subtract the button and border width from width constraint
            int buttonWidth = Math.max(upButtonPreferredSize.width,
                downButtonPreferredSize.width);

            width = Math.max(width - buttonWidth - 2, 0);
        }

        itemRenderer.render(spinner.getSelectedValue(), spinner);
        preferredHeight = Math.max(preferredHeight,
            itemRenderer.getPreferredHeight(width) + 2);

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        // TODO Optimize
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void layout() {
        int width = getWidth();
        int height = getHeight();

        int buttonHeight = height / 2;
        int buttonWidth = Math.max(upButton.getPreferredWidth(buttonHeight),
            downButton.getPreferredWidth(buttonHeight));

        upButton.setSize(buttonWidth, buttonHeight);
        downButton.setSize(buttonWidth, buttonHeight);

        upButton.setLocation(width - buttonWidth, 0);
        downButton.setLocation(width - buttonWidth, buttonHeight);
    }

    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        Spinner spinner = (Spinner)getComponent();
        Spinner.ItemRenderer itemRenderer = spinner.getItemRenderer();

        int width = getWidth();
        int height = getHeight();

        int buttonWidth = upButton.getWidth();

        graphics.setStroke(new BasicStroke(0));
        graphics.setPaint(borderColor);
        graphics.draw(new Rectangle2D.Double(0, 0, width - buttonWidth, height - 1));

        itemRenderer.render(spinner.getSelectedValue(), spinner);
        itemRenderer.setSize(width - buttonWidth - 2, height - 2);

        Graphics2D contentGraphics = (Graphics2D)graphics.create();
        contentGraphics.translate(1, 1);
        contentGraphics.clipRect(0, 0, itemRenderer.getWidth(), itemRenderer.getHeight());
        itemRenderer.paint(contentGraphics);
    }

    // SpinnerListener methods

    public void spinnerDataChanged(Spinner spinner, List<?> previousSpinnerData) {
        invalidateComponent();
    }

    public void itemRendererChanged(Spinner spinner,
        Spinner.ItemRenderer previousItemRenderer) {
        invalidateComponent();
    }

    public void circularChanged(Spinner spinner) {
        // No-op
    }

    public void selectedValueKeyChanged(Spinner spinner,
        String previousSelectedValueKey) {
        // No-op
    }

    public void valueMappingChanged(Spinner spinner,
        Spinner.ValueMapping previousValueMapping) {
        invalidateComponent();
    }

    // SpinnerSelectionListener methods

    public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex) {
        invalidateComponent();
    }

    // ButtonPressListener methods

    public void buttonPressed(Button button) {
        Spinner spinner = (Spinner)getComponent();
        boolean circular = spinner.isCircular();
        int selectedIndex = spinner.getSelectedIndex();
        int count = spinner.getSpinnerData().getLength();

        if (button == upButton) {
            if (selectedIndex < count - 1) {
                spinner.setSelectedIndex(selectedIndex + 1);
            } else if (circular) {
                spinner.setSelectedIndex(0);
            }
        } else {
            if (selectedIndex > 0) {
                spinner.setSelectedIndex(selectedIndex - 1);
            } else if (circular) {
                spinner.setSelectedIndex(count - 1);
            }
        }
    }
}
