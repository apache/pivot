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
import java.awt.GradientPaint;
import java.awt.Graphics2D;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.Sequence;
import pivot.util.Vote;
import pivot.wtk.Button;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
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
import pivot.wtk.media.Image;
import pivot.wtk.skin.ButtonSkin;
import pivot.wtk.skin.ContainerSkin;

/**
 * Accordion skin.
 * <p>
 * TODO Make headers focusable?
 * <p>
 * TODO Disable the header when the component is disabled? We'd need
 * style properties to present a disabled header state. We'd also need
 * to manage button enabled state independently of the accordion enabled
 * state.
 * <p>
 * TODO Support the displayable flag to show/hide panels.
 *
 * @author gbrown
 */
public class TerraAccordionSkin extends ContainerSkin
    implements AccordionListener, AccordionSelectionListener, AccordionAttributeListener,
        Button.GroupListener {
    protected class PanelHeader extends Button {
        public PanelHeader() {
            this(null);
        }

        public PanelHeader(Object buttonData) {
            super(buttonData);

            super.setToggleButton(true);
            setDataRenderer(DEFAULT_DATA_RENDERER);

            setSkin(new PanelHeaderSkin());
        }

        @Override
        public boolean isEnabled() {
            Accordion accordion = (Accordion)TerraAccordionSkin.this.getComponent();
            return accordion.isEnabled();
        }

        @Override
        public void setEnabled(boolean enabled) {
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
        public int getPreferredWidth(int height) {
            PanelHeader panelHeader = (PanelHeader)getComponent();

            Button.DataRenderer dataRenderer = panelHeader.getDataRenderer();
            dataRenderer.render(panelHeader.getButtonData(), panelHeader, false);

            // Include padding in constraint
            int contentHeight = height;
            if (contentHeight != -1) {
                contentHeight = Math.max(contentHeight - (buttonPadding.top
        			+ buttonPadding.bottom), 0);
            }

            int preferredWidth = dataRenderer.getPreferredWidth(contentHeight)
                + buttonPadding.left + buttonPadding.right;

            return preferredWidth;
        }

        public int getPreferredHeight(int width) {
            PanelHeader panelHeader = (PanelHeader)getComponent();

            Button.DataRenderer dataRenderer = panelHeader.getDataRenderer();
            dataRenderer.render(panelHeader.getButtonData(), panelHeader, false);

            // Include padding in constraint
            int contentWidth = width;
            if (contentWidth != -1) {
                contentWidth = Math.max(contentWidth - (buttonPadding.left
            		+ buttonPadding.right), 0);
            }

            int preferredHeight = dataRenderer.getPreferredHeight(contentWidth)
                + buttonPadding.top + buttonPadding.bottom;

            return preferredHeight;
        }

        public Dimensions getPreferredSize() {
            PanelHeader panelHeader = (PanelHeader)getComponent();

            Button.DataRenderer dataRenderer = panelHeader.getDataRenderer();
            dataRenderer.render(panelHeader.getButtonData(), panelHeader, false);

            Dimensions preferredContentSize = dataRenderer.getPreferredSize();

            int preferredWidth = preferredContentSize.width
                + buttonPadding.left + buttonPadding.right;

            int preferredHeight = preferredContentSize.height
                + buttonPadding.top + buttonPadding.bottom;

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

            // Paint the content
            Button.DataRenderer dataRenderer = panelHeader.getDataRenderer();
            dataRenderer.render(panelHeader.getButtonData(), panelHeader, highlighted);
            dataRenderer.setSize(Math.max(width - (buttonPadding.left + buttonPadding.right), 0),
                Math.max(getHeight() - (buttonPadding.top + buttonPadding.bottom), 0));

            Graphics2D contentGraphics = (Graphics2D)graphics.create();
            contentGraphics.translate(buttonPadding.left, buttonPadding.top);
            contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
            dataRenderer.paint(contentGraphics);
            contentGraphics.dispose();
        }

        @Override
        public boolean isFocusable() {
            return false;
        }

        @Override
        public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            PanelHeader panelHeader = (PanelHeader)getComponent();
            panelHeader.press();
        }

        public Font getFont() {
        	return buttonFont;
        }

        public Color getColor() {
        	return buttonColor;
        }

        public Color getDisabledColor() {
        	return buttonColor;
        }
    }

    private Button.Group panelHeaderGroup = new Button.Group();
    private ArrayList<PanelHeader> panelHeaders = new ArrayList<PanelHeader>();

    private Color borderColor;
    private Insets padding;
    private Font buttonFont;
    private Color buttonColor;
    private Color buttonBackgroundColor;
    private Insets buttonPadding;

    // Derived colors
    private Color buttonBevelColor;

	public static final int GRADIENT_BEVEL_THICKNESS = 4;
	private static final Button.DataRenderer DEFAULT_DATA_RENDERER = new ButtonDataRenderer();

	static {
		DEFAULT_DATA_RENDERER.getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
	}

    public TerraAccordionSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(4));

        borderColor = theme.getColor(7);
        padding = new Insets(4);
        buttonFont = theme.getFont().deriveFont(Font.BOLD);
        buttonColor = theme.getColor(15);
        buttonBackgroundColor = theme.getColor(10);
        buttonPadding = new Insets(3, 4, 3, 4);

        // Set the derived colors
        buttonBevelColor = TerraTheme.brighten(buttonBackgroundColor);

        panelHeaderGroup.getGroupListeners().add(this);
    }

    public void install(Component component) {
        super.install(component);

        Accordion accordion = (Accordion)component;

        // Add this as a listener on the accordion
        accordion.getAccordionListeners().add(this);
        accordion.getAccordionSelectionListeners().add(this);
        accordion.getAccordionAttributeListeners().add(this);

        // Add header buttons for all existing panels
        for (Component panel : accordion.getPanels()) {
            PanelHeader panelHeader = new PanelHeader(new ButtonData(Accordion.getIcon(panel),
                Accordion.getName(panel)));
            panelHeader.setGroup(panelHeaderGroup);
            accordion.add(panelHeader);
            panelHeaders.add(panelHeader);
        }

        selectedIndexChanged(accordion, -1);
    }

    public void uninstall() {
        Accordion accordion = (Accordion)getComponent();

        // Remove this as a listener on the accordion
        accordion.getAccordionListeners().remove(this);
        accordion.getAccordionSelectionListeners().remove(this);
        accordion.getAccordionAttributeListeners().remove(this);

        // Remove the header buttons
        for (int i = 0, n = panelHeaders.getLength(); i < n; i++) {
            accordion.remove(panelHeaders.get(i));
        }

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        Accordion accordion = (Accordion)getComponent();

        // The preferred width is the maximum unconstrained preferred width of
        // the headers and the panels, plus border
        for (PanelHeader panelHeader : panelHeaders) {
        	preferredWidth = Math.max(panelHeader.getPreferredWidth(), preferredWidth);
        }

        for (Component panel : accordion.getPanels()) {
        	preferredWidth = Math.max(panel.getPreferredWidth()
    			+ (padding.left + padding.right), preferredWidth);
        }

        preferredWidth += 2;

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        Accordion accordion = (Accordion)getComponent();

        // The preferred height is the sum of the constrained preferred heights
        // of the headers and selected panel, plus dividers and border
        for (PanelHeader panelHeader : panelHeaders) {
        	preferredHeight += panelHeader.getPreferredHeight(width);
        }

        preferredHeight += (panelHeaders.getLength() - 1);

        int selectedIndex = accordion.getSelectedIndex();
        if (selectedIndex != -1) {
        	Component selectedPanel = accordion.getPanels().get(selectedIndex);

        	if (width != -1) {
        		width = Math.max(0, width - (padding.left + padding.right));
        	}

        	preferredHeight += selectedPanel.getPreferredHeight(width) + 1;
        }

        // Include top and bottom border
        preferredHeight += 2;

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        // TODO Optimize
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void layout() {
        Accordion accordion = (Accordion)getComponent();

        int width = getWidth();
        int height = getHeight();

        // Determine the area available to the content
        int panelWidth = Math.max(width - 2, 0);
        int contentHeight = height - 1;
        for (PanelHeader panelHeader : panelHeaders) {
        	panelHeader.setSize(panelWidth, panelHeader.getPreferredHeight(panelWidth));
        	contentHeight -= (panelHeader.getHeight() + 1);
        }

        contentHeight = Math.max(contentHeight, 0);

        // Lay out the components
        Accordion.PanelSequence panels = accordion.getPanels();
        int selectedIndex = accordion.getSelectedIndex();

        int panelY = 1;
        for (int i = 0, n = panels.getLength(); i < n; i++) {
        	PanelHeader panelHeader = panelHeaders.get(i);
        	panelHeader.setLocation(1, panelY);
        	panelY += panelHeader.getHeight();

            Component panel = panels.get(i);
            if (i == selectedIndex) {
                // Show the selected panel
                panel.setVisible(true);

                // Set the panel's size and location
                panel.setSize(Math.max(width - (padding.left + padding.right + 2), 0),
            		Math.max(contentHeight - (padding.top + padding.bottom + 1), 0));
                panel.setLocation(padding.left + 1, panelY + padding.top + 1);

                panelY += contentHeight;
            } else {
                panel.setVisible(false);
            }

            panelY += 1;
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        // Call the base class to paint the background
        super.paint(graphics);

        Accordion accordion = (Accordion)getComponent();

        int width = getWidth();
        int height = getHeight();

        graphics.setStroke(new BasicStroke());
        graphics.setPaint(borderColor);

        // Draw dividers
        Accordion.PanelSequence panels = accordion.getPanels();
        int selectedIndex = accordion.getSelectedIndex();

        for (int i = 0, n = panels.getLength(); i < n; i++) {
        	PanelHeader panelHeader = panelHeaders.get(i);
        	int dividerY = panelHeader.getY() + panelHeader.getHeight();

        	graphics.drawLine(0, dividerY, width - 1, dividerY);

        	if (i == selectedIndex
    			&& i < n - 1) {
        		Component panel = panels.get(i);
        		dividerY = panel.getY() + panel.getHeight() + padding.bottom;
        		graphics.drawLine(0, dividerY, width - 1, dividerY);
            }
        }

        graphics.drawRect(0, 0, width - 1, height - 1);
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

        setBorderColor(decodeColor(borderColor));
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

        setButtonColor(decodeColor(buttonColor));
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

    protected void updateButtonData(Component panel) {
        Accordion accordion = (Accordion)getComponent();
        int panelIndex = accordion.getPanels().indexOf(panel);

        if (panelIndex != -1) {
            PanelHeader panelHeader = panelHeaders.get(panelIndex);
            panelHeader.setButtonData(new ButtonData(Accordion.getIcon(panel),
                Accordion.getName(panel)));
        }
    }

    // AccordionListener methods

    public void panelInserted(Accordion accordion, int index) {
        // Create a new button for the panel
        Component panel = accordion.getPanels().get(index);
        PanelHeader panelHeader = new PanelHeader(new ButtonData(Accordion.getIcon(panel),
            Accordion.getName(panel)));

        accordion.add(panelHeader);
        panelHeader.setGroup(panelHeaderGroup);
        panelHeaders.insert(panelHeader, index);
    }

    public void panelsRemoved(Accordion accordion, int index, Sequence<Component> panels) {
        // Remove the buttons
        Sequence<PanelHeader> removed = panelHeaders.remove(index, panels.getLength());

        for (int i = 0, n = removed.getLength(); i < n; i++) {
            PanelHeader panelHeader = removed.get(i);
            panelHeader.setGroup((Group)null);
            accordion.remove(panelHeader);
        }
    }

    public void cornerChanged(Accordion accordion, Component previousCorner) {
        invalidateComponent();
    }

    // Tab pane selection events
	public Vote previewSelectedIndexChange(Accordion accordion, int selectedIndex) {
		// TODO
		return Vote.APPROVE;
	}

	public void selectedIndexChangeVetoed(Accordion accordion, Vote reason) {
		// TODO
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

    // Tab pane attribute events
    public void nameChanged(Accordion accordion, Component component, String previousName) {
        updateButtonData(component);
    }

    public void iconChanged(Accordion accordion, Component component, Image previousIcon) {
        updateButtonData(component);
    }

    // Button group events
    public void selectionChanged(Group group, Button previousSelection) {
        Button button = panelHeaderGroup.getSelection();
        int index = (button == null) ? -1 : panelHeaders.indexOf((PanelHeader)button);

        Accordion accordion = (Accordion)getComponent();
        accordion.setSelectedIndex(index);
    }
}
