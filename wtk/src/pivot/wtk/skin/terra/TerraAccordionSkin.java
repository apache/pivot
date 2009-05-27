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
package pivot.wtk.skin.terra;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.Sequence;
import pivot.util.Vote;
import pivot.wtk.Button;
import pivot.wtk.Component;
import pivot.wtk.ComponentStateListener;
import pivot.wtk.Dimensions;
import pivot.wtk.GraphicsUtilities;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Insets;
import pivot.wtk.Mouse;
import pivot.wtk.Accordion;
import pivot.wtk.AccordionListener;
import pivot.wtk.AccordionSelectionListener;
import pivot.wtk.AccordionAttributeListener;
import pivot.wtk.Theme;
import pivot.wtk.Button.Group;
import pivot.wtk.content.ButtonData;
import pivot.wtk.content.ButtonDataRenderer;
import pivot.wtk.effects.ClipDecorator;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;
import pivot.wtk.effects.easing.Easing;
import pivot.wtk.effects.easing.Quartic;
import pivot.wtk.media.Image;
import pivot.wtk.skin.ButtonSkin;
import pivot.wtk.skin.ContainerSkin;

/**
 * Accordion skin.
 *
 * @author gbrown
 */
public class TerraAccordionSkin extends ContainerSkin
    implements AccordionListener, AccordionSelectionListener, AccordionAttributeListener {
    protected class PanelHeader extends Button {
        public PanelHeader(Component panel) {
            super(panel);

            super.setToggleButton(true);
            setDataRenderer(DEFAULT_DATA_RENDERER);

            setSkin(new PanelHeaderSkin());
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
        public int getPreferredWidth(int height) {
            PanelHeader panelHeader = (PanelHeader)getComponent();

            Button.DataRenderer dataRenderer = panelHeader.getDataRenderer();
            dataRenderer.render(panelHeader.getButtonData(), panelHeader, false);

            int preferredWidth = dataRenderer.getPreferredWidth(-1)
                + buttonPadding.left + buttonPadding.right + 2;

            return preferredWidth;
        }

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

        public void paint(Graphics2D graphics) {
            PanelHeader panelHeader = (PanelHeader)getComponent();

            int width = getWidth();
            int height = getHeight();

            // Paint the background
            graphics.setPaint(new GradientPaint(width / 2, 0, buttonBevelColor,
                width / 2, height, buttonBackgroundColor));
            graphics.fillRect(0, 0, width, height);

            // Paint the border
            graphics.setPaint(borderColor);
            GraphicsUtilities.drawRect(graphics, 0, 0, width, height);

            // Paint the content
            Button.DataRenderer dataRenderer = panelHeader.getDataRenderer();
            dataRenderer.render(panelHeader.getButtonData(), panelHeader, highlighted);
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
            super(SELECTION_CHANGE_DURATION, SELECTION_CHANGE_RATE, false);
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

    private Button.Group panelHeaderGroup = new Button.Group();
    private ArrayList<PanelHeader> panelHeaders = new ArrayList<PanelHeader>();

    private Color borderColor;
    private Insets padding;
    private Font buttonFont;
    private Color buttonColor;
    private Color disabledButtonColor;
    private Color buttonBackgroundColor;
    private Insets buttonPadding;

    // Derived colors
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

    private static final Button.DataRenderer DEFAULT_DATA_RENDERER = new ButtonDataRenderer() {
        {   getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
        }

        @Override
        public void render(Object data, Button button, boolean highlighted) {
            // TODO Create a custom inner renderer class that can display
            // the close button (and also avoid the heap allocation every
            // time we're called to render())
            Component panel = (Component)data;
            super.render(new ButtonData(Accordion.getIcon(panel), Accordion.getName(panel)),
                button, highlighted);
        }
    };

    private static final int SELECTION_CHANGE_DURATION = 250;
    private static final int SELECTION_CHANGE_RATE = 30;

    public TerraAccordionSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(4));

        borderColor = theme.getColor(7);
        padding = new Insets(4);
        buttonFont = theme.getFont().deriveFont(Font.BOLD);
        buttonColor = theme.getColor(15);
        disabledButtonColor = theme.getColor(7);
        buttonBackgroundColor = theme.getColor(10);
        buttonPadding = new Insets(3, 4, 3, 4);

        // Set the derived colors
        buttonBevelColor = TerraTheme.brighten(buttonBackgroundColor);

        panelHeaderGroup.getGroupListeners().add(new Button.GroupListener() {
            public void selectionChanged(Group group, Button previousSelection) {
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

    public void install(Component component) {
        super.install(component);

        Accordion accordion = (Accordion)component;

        // Add this as a listener on the accordion
        accordion.getAccordionListeners().add(this);
        accordion.getAccordionSelectionListeners().add(this);
        accordion.getAccordionAttributeListeners().add(this);

        // Add headers for all existing panels
        for (Component panel : accordion.getPanels()) {
            PanelHeader panelHeader = new PanelHeader(panel);
            panelHeader.setGroup(panelHeaderGroup);
            panelHeaders.add(panelHeader);
            accordion.add(panelHeader);

            // Listen for state changes on the panel
            panelHeader.setEnabled(panel.isEnabled());
            panel.getComponentStateListeners().add(panelStateListener);
        }
    }

    public void uninstall() {
        Accordion accordion = (Accordion)getComponent();

        // Remove this as a listener on the accordion
        accordion.getAccordionListeners().remove(this);
        accordion.getAccordionSelectionListeners().remove(this);
        accordion.getAccordionAttributeListeners().remove(this);

        for (PanelHeader panelHeader : panelHeaders) {
            // Stop listening for state changes on the panel
            Component panel = (Component)panelHeader.getButtonData();
            panel.getComponentStateListeners().remove(panelStateListener);

            // Remove the header
            accordion.remove(panelHeader);
        }

        super.uninstall();
    }

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

        preferredHeight += (maxPanelHeight + padding.top + padding.bottom);

        return preferredHeight;
    }

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

        preferredHeight += (maxPanelHeight + padding.top + padding.bottom);

        return new Dimensions(preferredWidth, preferredHeight);
    }

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

            panelHeight = Math.max(panelHeight - 1, 0);
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
                    panel.setLocation(padding.left + 1, panelY + padding.top);

                    panelY += panelHeight;
                } else {
                    panel.setVisible(false);
                }
            } else {
                if (selectionChangeTransition.isRunning()) {
                    if (panel == selectionChangeTransition.fromPanel) {
                        panel.setLocation(padding.left + 1, panelY + padding.top);

                        int previousSelectedPanelHeight = Math.round((float)panelHeight * (1.0f
                            - selectionChangeTransition.getEasedPercentComplete()));
                        previousSelectedPanelClipDecorator.setWidth(contentWidth);
                        previousSelectedPanelClipDecorator.setHeight(previousSelectedPanelHeight);

                        panelY += previousSelectedPanelHeight;
                    }

                    if (panel == selectionChangeTransition.toPanel) {
                        panel.setLocation(padding.left + 1, panelY + padding.top);

                        int selectedPanelHeight = Math.round((float)panelHeight
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
            throw new IllegalArgumentException("buttonFont is null.");
        }

        setButtonFont(Font.decode(buttonFont));
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

    public final void setButtonPadding(int buttonPadding) {
        setButtonPadding(new Insets(buttonPadding));
    }

    // Accordion events
    public void panelInserted(Accordion accordion, int index) {
        if (selectionChangeTransition != null) {
            selectionChangeTransition.end();
        }

        // Add a header for the panel
        Component panel = accordion.getPanels().get(index);
        PanelHeader panelHeader = new PanelHeader(panel);
        panelHeader.setGroup(panelHeaderGroup);
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

    public void panelsRemoved(Accordion accordion, int index, Sequence<Component> removed) {
        if (selectionChangeTransition != null) {
            selectionChangeTransition.end();
        }

        // Remove the headers
        Sequence<PanelHeader> removedHeaders = panelHeaders.remove(index, removed.getLength());

        for (int i = 0, n = removedHeaders.getLength(); i < n; i++) {
            PanelHeader panelHeader = removedHeaders.get(i);
            panelHeader.setGroup((Group)null);

            // Stop listening for state changes on the panel
            Component panel = (Component)panelHeader.getButtonData();
            panel.getComponentStateListeners().remove(panelStateListener);

            // Remove the header
            accordion.remove(panelHeader);
        }

        invalidateComponent();
    }

    // Accordion selection events
    public Vote previewSelectedIndexChange(final Accordion accordion, final int selectedIndex) {
        Vote vote = Vote.APPROVE;

        if (accordion.isShowing()) {
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

    public void selectedIndexChangeVetoed(Accordion accordion, Vote reason) {
        if (reason == Vote.DENY
            && selectionChangeTransition != null) {
            selectionChangeTransition.stop();
            selectionChangeTransition = null;
            invalidateComponent();
        }
    }

    public void selectedIndexChanged(Accordion accordion, int previousSelectedIndex) {
        int selectedIndex = accordion.getSelectedIndex();

        if (selectedIndex == -1) {
            Button button = panelHeaderGroup.getSelection();
            if (button != null) {
                button.setSelected(false);
            }
        } else {
            Button button = (Button)panelHeaders.get(selectedIndex);
            button.setSelected(true);
        }

        invalidateComponent();
    }

    // Accordion attribute events
    public void nameChanged(Accordion accordion, Component component, String previousName) {
        invalidateComponent();
    }

    public void iconChanged(Accordion accordion, Component component, Image previousIcon) {
        invalidateComponent();
    }
}
