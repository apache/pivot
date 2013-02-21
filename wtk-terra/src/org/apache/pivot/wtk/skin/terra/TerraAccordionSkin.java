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
package org.apache.pivot.wtk.skin.terra;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Accordion;
import org.apache.pivot.wtk.AccordionAttributeListener;
import org.apache.pivot.wtk.AccordionListener;
import org.apache.pivot.wtk.AccordionSelectionListener;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonGroupListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentStateListener;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.Modifier;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.effects.ClipDecorator;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;
import org.apache.pivot.wtk.effects.easing.Easing;
import org.apache.pivot.wtk.effects.easing.Quartic;
import org.apache.pivot.wtk.skin.ButtonSkin;
import org.apache.pivot.wtk.skin.ContainerSkin;

/**
 * Accordion skin.
 */
public class TerraAccordionSkin extends ContainerSkin
    implements AccordionListener, AccordionSelectionListener, AccordionAttributeListener {
    protected class PanelHeader extends Button {
        private final Component panel;

        public PanelHeader(Component panel) {
            this.panel = panel;
            super.setToggleButton(true);

            setSkin(new PanelHeaderSkin());
        }

        @Override
        public Object getButtonData() {
            return Accordion.getHeaderData(panel);
        }

        @Override
        public void setButtonData(Object buttonData) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Button.DataRenderer getDataRenderer() {
            Accordion accordion = (Accordion)TerraAccordionSkin.this.getComponent();
            return accordion.getHeaderDataRenderer();
        }

        @Override
        public void setDataRenderer(Button.DataRenderer dataRenderer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getTooltipText() {
            return Accordion.getTooltipText(panel);
        }

        @Override
        public void setTooltipText(String tooltipText) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setToggleButton(boolean toggleButton) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setTriState(boolean triState) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void press() {
            setSelected(true);
            super.press();
        }
    }

    protected class PanelHeaderSkin extends ButtonSkin {
        @Override
        public int getPreferredWidth(int height) {
            PanelHeader panelHeader = (PanelHeader)getComponent();

            Button.DataRenderer dataRenderer = panelHeader.getDataRenderer();
            dataRenderer.render(panelHeader.getButtonData(), panelHeader, false);

            int preferredWidth = dataRenderer.getPreferredWidth(-1)
                + buttonPadding.left + buttonPadding.right + 2;

            return preferredWidth;
        }

        @Override
        public int getPreferredHeight(int width) {
            PanelHeader panelHeader = (PanelHeader)getComponent();

            Button.DataRenderer dataRenderer = panelHeader.getDataRenderer();
            dataRenderer.render(panelHeader.getButtonData(), panelHeader, false);

            // Include padding and border in constraint
            int contentWidth = width;
            if (contentWidth != -1) {
                contentWidth = Math.max(contentWidth - (buttonPadding.left
                    + buttonPadding.right + 2), 0);
            }

            int preferredHeight = dataRenderer.getPreferredHeight(contentWidth)
                + buttonPadding.top + buttonPadding.bottom + 2;

            return preferredHeight;
        }

        @Override
        public Dimensions getPreferredSize() {
            PanelHeader panelHeader = (PanelHeader)getComponent();

            Button.DataRenderer dataRenderer = panelHeader.getDataRenderer();
            dataRenderer.render(panelHeader.getButtonData(), panelHeader, false);

            Dimensions preferredContentSize = dataRenderer.getPreferredSize();

            int preferredWidth = preferredContentSize.width
                + buttonPadding.left + buttonPadding.right + 2;

            int preferredHeight = preferredContentSize.height
                + buttonPadding.top + buttonPadding.bottom + 2;

            return new Dimensions(preferredWidth, preferredHeight);
        }

        @Override
        public int getBaseline(int width, int height) {
            PanelHeader panelHeader = (PanelHeader)getComponent();

            Button.DataRenderer dataRenderer = panelHeader.getDataRenderer();
            dataRenderer.render(panelHeader.getButtonData(), panelHeader, false);

            int clientWidth = Math.max(width - (buttonPadding.left + buttonPadding.right + 2), 0);
            int clientHeight = Math.max(height - (buttonPadding.top + buttonPadding.bottom + 2), 0);

            int baseline = dataRenderer.getBaseline(clientWidth, clientHeight);

            if (baseline != -1) {
                baseline += buttonPadding.top + 1;
            }

            return baseline;
        }

        @Override
        public void paint(Graphics2D graphics) {
            PanelHeader panelHeader = (PanelHeader)getComponent();

            int width = getWidth();
            int height = getHeight();

            // Paint the background
            graphics.setPaint(new GradientPaint(width / 2f, 0, buttonBevelColor,
                width / 2f, height, buttonBackgroundColor));
            graphics.fillRect(0, 0, width, height);

            // Paint the border
            graphics.setPaint(borderColor);
            GraphicsUtilities.drawRect(graphics, 0, 0, width, height);

            // Paint the content
            Button.DataRenderer dataRenderer = panelHeader.getDataRenderer();
            dataRenderer.render(panelHeader.getButtonData(), panelHeader, false);
            dataRenderer.setSize(Math.max(width - (buttonPadding.left + buttonPadding.right + 2), 0),
                Math.max(getHeight() - (buttonPadding.top + buttonPadding.bottom + 2), 0));

            Graphics2D contentGraphics = (Graphics2D)graphics.create();
            contentGraphics.translate(buttonPadding.left + 1, buttonPadding.top + 1);
            contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
            dataRenderer.paint(contentGraphics);
            contentGraphics.dispose();
        }

        @Override
        public boolean isFocusable() {
            return false;
        }

        @Override
        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            PanelHeader panelHeader = (PanelHeader)getComponent();
            panelHeader.press();

            return true;
        }

        public Font getFont() {
            return buttonFont;
        }

        public Color getColor() {
            return buttonColor;
        }

        public Color getDisabledColor() {
            return disabledButtonColor;
        }
    }

    public class SelectionChangeTransition extends Transition {
        public final Component fromPanel;
        public final Component toPanel;

        private Easing easing = new Quartic();

        public SelectionChangeTransition(Component fromPanel, Component toPanel) {
            super(selectionChangeDuration, selectionChangeRate, false);
            this.fromPanel = fromPanel;
            this.toPanel = toPanel;
        }

        public float getEasedPercentComplete() {
            return easing.easeOut(getElapsedTime(), 0, 1, getDuration());
        }

        @Override
        public void start(TransitionListener transitionListener) {
            fromPanel.getDecorators().add(previousSelectedPanelClipDecorator);
            toPanel.getDecorators().add(selectedPanelClipDecorator);

            getComponent().setEnabled(false);

            super.start(transitionListener);
        }

        @Override
        public void stop() {
            fromPanel.getDecorators().remove(previousSelectedPanelClipDecorator);
            toPanel.getDecorators().remove(selectedPanelClipDecorator);

            getComponent().setEnabled(true);

            super.stop();
        }

        @Override
        protected void update() {
            invalidateComponent();
        }
    }

    private ButtonGroup panelHeaderGroup = new ButtonGroup();
    private ArrayList<PanelHeader> panelHeaders = new ArrayList<PanelHeader>();

    private Color borderColor;
    private Insets padding;
    private Font buttonFont;
    private Color buttonColor;
    private Color disabledButtonColor;
    private Color buttonBackgroundColor;
    private Insets buttonPadding;

    private int selectionChangeDuration = DEFAULT_SELECTION_CHANGE_DURATION;
    private int selectionChangeRate = DEFAULT_SELECTION_CHANGE_RATE;

    private Color buttonBevelColor;

    private SelectionChangeTransition selectionChangeTransition = null;
    private ClipDecorator previousSelectedPanelClipDecorator = new ClipDecorator();
    private ClipDecorator selectedPanelClipDecorator = new ClipDecorator();

    private ComponentStateListener panelStateListener = new ComponentStateListener.Adapter() {
        @Override
        public void enabledChanged(Component component) {
            Accordion accordion = (Accordion)getComponent();
            int i = accordion.getPanels().indexOf(component);
            panelHeaders.get(i).setEnabled(component.isEnabled());
        }
    };

    public static final int GRADIENT_BEVEL_THICKNESS = 4;

    private static final int DEFAULT_SELECTION_CHANGE_DURATION = 250;
    private static final int DEFAULT_SELECTION_CHANGE_RATE = 30;

    public TerraAccordionSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(4));

        borderColor = theme.getColor(7);
        padding = new Insets(4);
        buttonFont = theme.getFont().deriveFont(Font.BOLD);
        buttonColor = theme.getColor(12);
        disabledButtonColor = theme.getColor(7);
        buttonBackgroundColor = theme.getColor(10);
        buttonPadding = new Insets(3, 4, 3, 4);

        // Set the derived colors
        buttonBevelColor = TerraTheme.brighten(buttonBackgroundColor);

        panelHeaderGroup.getButtonGroupListeners().add(new ButtonGroupListener.Adapter() {
            @Override
            public void selectionChanged(ButtonGroup buttonGroup, Button previousSelection) {
                Button button = panelHeaderGroup.getSelection();
                int index = (button == null) ? -1 : panelHeaders.indexOf((PanelHeader)button);

                Accordion accordion = (Accordion)getComponent();
                accordion.setSelectedIndex(index);
            }
        });
    }

    @Override
    public void setSize(int width, int height) {
        if (selectionChangeTransition != null) {
            selectionChangeTransition.end();
        }

        super.setSize(width, height);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Accordion accordion = (Accordion)component;
        accordion.getAccordionListeners().add(this);
        accordion.getAccordionSelectionListeners().add(this);
        accordion.getAccordionAttributeListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        Accordion accordion = (Accordion)getComponent();

        // The preferred width is the maximum unconstrained preferred width of
        // the headers and the panels, plus border
        int maxPanelHeaderWidth = 0;
        for (PanelHeader panelHeader : panelHeaders) {
            maxPanelHeaderWidth = Math.max(panelHeader.getPreferredWidth(), maxPanelHeaderWidth);
        }

        int maxPanelWidth = 0;
        for (Component panel : accordion.getPanels()) {
            maxPanelWidth = Math.max(panel.getPreferredWidth(), maxPanelWidth);
        }

        int preferredWidth = Math.max(maxPanelHeaderWidth, maxPanelWidth
            + (padding.left + padding.right + 2));

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        Accordion accordion = (Accordion)getComponent();

        int preferredHeight = 0;

        // The preferred height is the sum of the constrained preferred heights
        // of the headers and selected panel, plus border
        for (PanelHeader panelHeader : panelHeaders) {
            preferredHeight += panelHeader.getPreferredHeight(width) - 1;
        }

        if (width != -1) {
            width = Math.max(0, width - (padding.left + padding.right + 2));
        }

        int maxPanelHeight = 0;
        for (Component panel : accordion.getPanels()) {
            maxPanelHeight = Math.max(maxPanelHeight, panel.getPreferredHeight(width));
        }

        preferredHeight += (maxPanelHeight + padding.top + padding.bottom + 2);

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        Accordion accordion = (Accordion)getComponent();

        int preferredHeight = 0;

        int maxPanelHeaderWidth = 0;
        for (PanelHeader panelHeader : panelHeaders) {
            Dimensions preferredSize = panelHeader.getPreferredSize();
            maxPanelHeaderWidth = Math.max(preferredSize.width, maxPanelHeaderWidth);
            preferredHeight += preferredSize.height - 1;
        }

        int maxPanelWidth = 0;
        int maxPanelHeight = 0;

        for (Component panel : accordion.getPanels()) {
            Dimensions preferredSize = panel.getPreferredSize();
            maxPanelWidth = Math.max(preferredSize.width, maxPanelWidth);
            maxPanelHeight = Math.max(maxPanelHeight, preferredSize.height);
        }

        int preferredWidth = Math.max(maxPanelHeaderWidth, maxPanelWidth
            + (padding.left + padding.right + 2));

        preferredHeight += (maxPanelHeight + padding.top + padding.bottom + 2);

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        int baseline = -1;

        if (panelHeaders.getLength() > 0) {
            PanelHeader firstPanelHeader = panelHeaders.get(0);
            baseline = firstPanelHeader.getBaseline(width, firstPanelHeader.getPreferredHeight(width));
        }

        return baseline;
    }

    @Override
    public void layout() {
        Accordion accordion = (Accordion)getComponent();

        int width = getWidth();
        int height = getHeight();

        int contentWidth = Math.max(width - (padding.left + padding.right + 2), 0);

        // Determine the content height
        int panelHeight = 0;
        int contentHeight = 0;

        if (selectionChangeTransition == null) {
            panelHeight = height;
            for (PanelHeader panelHeader : panelHeaders) {
                panelHeader.setSize(width, panelHeader.getPreferredHeight(width));
                panelHeight -= (panelHeader.getHeight() - 1);
            }

            panelHeight = Math.max(panelHeight - 2, 0);
            contentHeight = Math.max(panelHeight - (padding.top + padding.bottom), 0);
        } else {
            panelHeight = selectionChangeTransition.toPanel.getHeight()
                + (padding.top + padding.bottom);
        }

        // Lay out the components
        Accordion.PanelSequence panels = accordion.getPanels();

        int panelY = 0;
        for (int i = 0, n = panels.getLength(); i < n; i++) {
            Component panel = panels.get(i);

            PanelHeader panelHeader = panelHeaders.get(i);
            panelHeader.setLocation(0, panelY);
            panelY += (panelHeader.getHeight() - 1);

            if (selectionChangeTransition == null) {
                Component toPanel = accordion.getSelectedPanel();

                if (panel == toPanel) {
                    panel.setVisible(true);

                    panel.setSize(contentWidth, contentHeight);
                    panel.setLocation(padding.left + 1, panelY + padding.top + 1);

                    panelY += panelHeight + 1;
                } else {
                    panel.setVisible(false);
                }
            } else {
                if (selectionChangeTransition.isRunning()) {
                    if (panel == selectionChangeTransition.fromPanel) {
                        panel.setLocation(padding.left + 1, panelY + padding.top + 1);

                        int previousSelectedPanelHeight = Math.round(panelHeight * (1.0f
                            - selectionChangeTransition.getEasedPercentComplete()));
                        previousSelectedPanelClipDecorator.setWidth(contentWidth);
                        previousSelectedPanelClipDecorator.setHeight(previousSelectedPanelHeight);

                        panelY += previousSelectedPanelHeight + 1;
                    }

                    if (panel == selectionChangeTransition.toPanel) {
                        panel.setLocation(padding.left + 1, panelY + padding.top + 1);

                        int selectedPanelHeight = Math.round(panelHeight
                            * selectionChangeTransition.getEasedPercentComplete());
                        selectedPanelClipDecorator.setWidth(contentWidth);
                        selectedPanelClipDecorator.setHeight(selectedPanelHeight);

                        panelY += selectedPanelHeight;
                    }
                } else {
                    selectionChangeTransition.toPanel.setSize(selectionChangeTransition.fromPanel.getSize());
                    selectionChangeTransition.toPanel.setVisible(true);
                }
            }
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        // Call the base class to paint the background
        super.paint(graphics);

        // Draw the border
        int width = getWidth();
        int height = getHeight();

        graphics.setPaint(borderColor);
        GraphicsUtilities.drawRect(graphics, 0, 0, width, height);
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        this.borderColor = borderColor;
        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        setBorderColor(GraphicsUtilities.decodeColor(borderColor));
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }

    public final void setPadding(String padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(Insets.decode(padding));
    }

    public Font getButtonFont() {
        return buttonFont;
    }

    public void setButtonFont(Font buttonFont) {
        if (buttonFont == null) {
            throw new IllegalArgumentException("buttonFont is null.");
        }

        this.buttonFont = buttonFont;
        invalidateComponent();
    }

    public final void setButtonFont(String buttonFont) {
        if (buttonFont == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setButtonFont(decodeFont(buttonFont));
    }

    public final void setButtonFont(Dictionary<String, ?> buttonFont) {
        if (buttonFont == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setButtonFont(Theme.deriveFont(buttonFont));
    }

    public Color getButtonColor() {
        return buttonColor;
    }

    public void setButtonColor(Color buttonColor) {
        if (buttonColor == null) {
            throw new IllegalArgumentException("buttonColor is null.");
        }

        this.buttonColor = buttonColor;
        repaintComponent();
    }

    public final void setButtonColor(String buttonColor) {
        if (buttonColor == null) {
            throw new IllegalArgumentException("buttonColor is null.");
        }

        setButtonColor(GraphicsUtilities.decodeColor(buttonColor));
    }

    public final void setButtonColor(int buttonColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setButtonColor(theme.getColor(buttonColor));
    }

    public Color getDisabledButtonColor() {
        return disabledButtonColor;
    }

    public void setDisabledButtonColor(Color disabledButtonColor) {
        if (disabledButtonColor == null) {
            throw new IllegalArgumentException("disabledButtonColor is null.");
        }

        this.disabledButtonColor = disabledButtonColor;
        repaintComponent();
    }

    public final void setDisabledButtonColor(String disabledButtonColor) {
        if (disabledButtonColor == null) {
            throw new IllegalArgumentException("disabledButtonColor is null.");
        }

        setDisabledButtonColor(GraphicsUtilities.decodeColor(disabledButtonColor));
    }

    public final void setDisabledButtonColor(int disabledButtonColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setDisabledButtonColor(theme.getColor(disabledButtonColor));
    }

    public Color getButtonBackgroundColor() {
        return buttonBackgroundColor;
    }

    public void setButtonBackgroundColor(Color buttonBackgroundColor) {
        if (buttonBackgroundColor == null) {
            throw new IllegalArgumentException("buttonBackgroundColor is null.");
        }

        this.buttonBackgroundColor = buttonBackgroundColor;
        buttonBevelColor = TerraTheme.brighten(buttonBackgroundColor);
        repaintComponent();
    }

    public final void setButtonBackgroundColor(String buttonBackgroundColor) {
        if (buttonBackgroundColor == null) {
            throw new IllegalArgumentException("buttonBackgroundColor is null.");
        }

        setButtonBackgroundColor(GraphicsUtilities.decodeColor(buttonBackgroundColor));
    }

    public final void setButtonBackgroundColor(int buttonBackgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setButtonBackgroundColor(theme.getColor(buttonBackgroundColor));
    }

    public Insets getButtonPadding() {
        return buttonPadding;
    }

    public void setButtonPadding(Insets buttonPadding) {
        if (buttonPadding == null) {
            throw new IllegalArgumentException("buttonPadding is null.");
        }

        this.buttonPadding = buttonPadding;
        invalidateComponent();
    }

    public final void setButtonPadding(Dictionary<String, ?> buttonPadding) {
        if (buttonPadding == null) {
            throw new IllegalArgumentException("buttonPadding is null.");
        }

        setButtonPadding(new Insets(buttonPadding));
    }

    public final void setButtonPadding(int buttonPadding) {
        setButtonPadding(new Insets(buttonPadding));
    }

    public final void setButtonPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("buttonPadding is null.");
        }

        setButtonPadding(padding.intValue());
    }

    public final void setButtonPadding(String padding) {
        if (padding == null) {
            throw new IllegalArgumentException("buttonPadding is null.");
        }

        setButtonPadding(Insets.decode(padding));
    }

    public int getSelectionChangeDuration() {
        return selectionChangeDuration;
    }

    public void setSelectionChangeDuration(int selectionChangeDuration) {
        this.selectionChangeDuration = selectionChangeDuration;
    }

    public int getSelectionChangeRate() {
        return selectionChangeRate;
    }

    public void setSelectionChangeRate(int selectionChangeRate) {
        this.selectionChangeRate = selectionChangeRate;
    }

    /**
     * Key presses have no effect if the event has already been consumed.<p>
     * CommandModifier + {@link KeyCode#KEYPAD_1 KEYPAD_1} to
     * {@link KeyCode#KEYPAD_9 KEYPAD_9}<br>or CommandModifier +
     * {@link KeyCode#N1 1} to {@link KeyCode#N9 9} Select the (enabled) pane at
     * index 0 to 8 respectively<p>
     * {@link Modifier#ALT ALT} + {@link KeyCode#UP UP} Select the next enabled
     * panel.<br>
     * {@link Modifier#ALT ALT} + {@link KeyCode#DOWN DOWN} Select the previous
     * enabled panel.
     *
     * @see Platform#getCommandModifier()
     */
    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(component, keyCode, keyLocation);

        if (!consumed) {
            Accordion accordion = (Accordion)getComponent();
            Accordion.PanelSequence panels = accordion.getPanels();

            Keyboard.Modifier commandModifier = Platform.getCommandModifier();
            if (Keyboard.isPressed(commandModifier)) {
                int selectedIndex = -1;

                switch (keyCode) {
                    case Keyboard.KeyCode.KEYPAD_1:
                    case Keyboard.KeyCode.N1: {
                        selectedIndex = 0;
                        break;
                    }

                    case Keyboard.KeyCode.KEYPAD_2:
                    case Keyboard.KeyCode.N2: {
                        selectedIndex = 1;
                        break;
                    }

                    case Keyboard.KeyCode.KEYPAD_3:
                    case Keyboard.KeyCode.N3: {
                        selectedIndex = 2;
                        break;
                    }

                    case Keyboard.KeyCode.KEYPAD_4:
                    case Keyboard.KeyCode.N4: {
                        selectedIndex = 3;
                        break;
                    }

                    case Keyboard.KeyCode.KEYPAD_5:
                    case Keyboard.KeyCode.N5: {
                        selectedIndex = 4;
                        break;
                    }

                    case Keyboard.KeyCode.KEYPAD_6:
                    case Keyboard.KeyCode.N6: {
                        selectedIndex = 5;
                        break;
                    }

                    case Keyboard.KeyCode.KEYPAD_7:
                    case Keyboard.KeyCode.N7: {
                        selectedIndex = 6;
                        break;
                    }

                    case Keyboard.KeyCode.KEYPAD_8:
                    case Keyboard.KeyCode.N8: {
                        selectedIndex = 7;
                        break;
                    }

                    case Keyboard.KeyCode.KEYPAD_9:
                    case Keyboard.KeyCode.N9: {
                        selectedIndex = 8;
                        break;
                    }

                    default: {
                        break;
                    }
                }

                if (selectedIndex >= 0
                    && selectedIndex < panels.getLength()
                    && panels.get(selectedIndex).isEnabled()) {
                    accordion.setSelectedIndex(selectedIndex);
                    consumed = true;
                }
            } else if (Keyboard.isPressed(Keyboard.Modifier.ALT)) {
                int n = panels.getLength();
                int selectedIndex = accordion.getSelectedIndex();

                switch (keyCode) {
                    case Keyboard.KeyCode.UP: {
                        do {
                            selectedIndex--;
                        } while (selectedIndex >= 0
                            && !panels.get(selectedIndex).isEnabled());

                        break;
                    }

                    case Keyboard.KeyCode.DOWN: {
                        do {
                            selectedIndex++;
                        } while (selectedIndex < n
                            && !panels.get(selectedIndex).isEnabled());

                        break;
                    }

                    default: {
                        break;
                    }
                }

                if (selectedIndex >= 0
                    && selectedIndex < n
                    && panels.get(selectedIndex).isEnabled()) {
                    accordion.setSelectedIndex(selectedIndex);
                    consumed = true;
                }
            }
        }

        return consumed;
    }

    // Accordion events
    @Override
    public void panelInserted(Accordion accordion, int index) {
        if (selectionChangeTransition != null) {
            selectionChangeTransition.end();
        }

        // Add a header for the panel
        Component panel = accordion.getPanels().get(index);
        PanelHeader panelHeader = new PanelHeader(panel);
        panelHeader.setButtonGroup(panelHeaderGroup);
        panelHeaders.insert(panelHeader, index);
        accordion.add(panelHeader);

        // Listen for state changes on the panel
        panelHeader.setEnabled(panel.isEnabled());
        panel.getComponentStateListeners().add(panelStateListener);

        // If this is the first panel, select it
        if (accordion.getPanels().getLength() == 1) {
            accordion.setSelectedIndex(0);
        }

        invalidateComponent();
    }

    @Override
    public void panelsRemoved(Accordion accordion, int index, Sequence<Component> removed) {
        if (selectionChangeTransition != null) {
            selectionChangeTransition.end();
        }

        // Remove the headers
        Sequence<PanelHeader> removedHeaders = panelHeaders.remove(index, removed.getLength());

        for (int i = 0, n = removedHeaders.getLength(); i < n; i++) {
            PanelHeader panelHeader = removedHeaders.get(i);
            panelHeader.setButtonGroup(null);

            // Stop listening for state changes on the panel
            panelHeader.panel.getComponentStateListeners().remove(panelStateListener);

            // Remove the header
            accordion.remove(panelHeader);
        }

        invalidateComponent();
    }

    @Override
    public void headerDataRendererChanged(Accordion accordion, Button.DataRenderer previousHeaderDataRenderer) {
        for (Component panelHeader : panelHeaders) {
            panelHeader.invalidate();
        }
    }

    // Accordion selection events
    @Override
    public Vote previewSelectedIndexChange(final Accordion accordion, final int selectedIndex) {
        Vote vote = Vote.APPROVE;

        if (accordion.isShowing()
            && accordion.isValid()) {
            if (selectionChangeTransition == null) {
                int previousSelectedIndex = accordion.getSelectedIndex();

                if (selectedIndex != -1
                    && previousSelectedIndex != -1) {
                    Component fromPanel = accordion.getPanels().get(previousSelectedIndex);
                    Component toPanel = accordion.getPanels().get(selectedIndex);

                    selectionChangeTransition = new SelectionChangeTransition(fromPanel,
                        toPanel);

                    layout();
                    selectionChangeTransition.start(new TransitionListener() {
                        @Override
                        public void transitionCompleted(Transition transition) {
                            accordion.setSelectedIndex(selectedIndex);
                            selectionChangeTransition = null;

                            invalidateComponent();
                        }
                    });

                    vote = Vote.DEFER;
                }
            } else {
                if (selectionChangeTransition.isRunning()) {
                    vote = Vote.DEFER;
                }
            }
        }

        return vote;
    }

    @Override
    public void selectedIndexChangeVetoed(Accordion accordion, Vote reason) {
        if (reason == Vote.DENY
            && selectionChangeTransition != null) {
            // NOTE We stop, rather than end, the transition so the completion
            // event isn't fired; if the event fires, the listener will set
            // the selection state
            selectionChangeTransition.stop();
            selectionChangeTransition = null;
            invalidateComponent();
        }
    }

    @Override
    public void selectedIndexChanged(Accordion accordion, int previousSelectedIndex) {
        int selectedIndex = accordion.getSelectedIndex();

        if (selectedIndex != previousSelectedIndex) {
            // This was not an indirect selection change
            if (selectedIndex == -1) {
                Button button = panelHeaderGroup.getSelection();
                if (button != null) {
                    button.setSelected(false);
                }
            } else {
                Button button = panelHeaders.get(selectedIndex);
                button.setSelected(true);

                Component selectedPanel = accordion.getPanels().get(selectedIndex);
                selectedPanel.requestFocus();
            }

            invalidateComponent();
        }
    }

    // Accordion attribute events
    @Override
    public void headerDataChanged(Accordion accordion, Component component, Object previousHeaderData) {
        int i = accordion.getPanels().indexOf(component);
        panelHeaders.get(i).invalidate();
    }

    @Override
    public void tooltipTextChanged(Accordion accordion, Component component, String previousTooltipText) {
        // No-op
    }
}
