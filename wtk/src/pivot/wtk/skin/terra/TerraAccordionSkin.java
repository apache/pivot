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
 * <p>
 * TODO Make headers focusable?
 * <p>
 * TODO Disable the header when the component is disabled? We'd need
 * style properties to present a disabled header state. We'd also need
 * to manage button enabled state independently of the accordion enabled
 * state.
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

            // Include padding and border in constraint
            int contentHeight = height;
            if (contentHeight != -1) {
                contentHeight = Math.max(contentHeight - (buttonPadding.top
        			+ buttonPadding.bottom + 2), 0);
            }

            int preferredWidth = dataRenderer.getPreferredWidth(contentHeight)
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
            graphics.setStroke(new BasicStroke());
            graphics.setPaint(borderColor);
            graphics.drawRect(0, 0, width - 1, height - 1);

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
        	return buttonColor;
        }
    }

	public class SelectionChangeTransition extends Transition {
	    public final Component previousSelectedPanel;
	    public final Component selectedPanel;

	    public final ClipDecorator previousSelectedPanelClipDecorator = new ClipDecorator();
	    public final ClipDecorator selectedPanelClipDecorator = new ClipDecorator();

	    private Easing easing = new Quartic();

	    public SelectionChangeTransition(Component previousSelectedPanel, Component selectedPanel,
    		int duration, int rate) {
	        super(duration, rate, false);
	        this.previousSelectedPanel = previousSelectedPanel;
	        this.selectedPanel = selectedPanel;
	    }

        public float getEasedPercentComplete() {
            return easing.easeOut(getElapsedTime(), 0, 1, getDuration());
        }

        @Override
	    public void start(TransitionListener transitionListener) {
	    	previousSelectedPanel.getDecorators().add(previousSelectedPanelClipDecorator);
	        selectedPanel.getDecorators().add(selectedPanelClipDecorator);

	        getComponent().setEnabled(false);

	        super.start(transitionListener);
	    }

	    @Override
	    public void stop() {
	    	previousSelectedPanel.getDecorators().remove(previousSelectedPanelClipDecorator);
	    	selectedPanel.getDecorators().remove(selectedPanelClipDecorator);

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

    private SelectionChangeTransition selectionChangeTransition = null;

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

	private static final int SELECTION_CHANGE_DURATION = 250;
	private static final int SELECTION_CHANGE_RATE = 30;

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

	@Override
	public void setSize(int width, int height) {
		if (selectionChangeTransition != null) {
			selectionChangeTransition.end();
			selectionChangeTransition = null;
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
    	int preferredWidth;
    	if (selectionChangeTransition == null) {
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

            preferredWidth = Math.max(maxPanelHeaderWidth, maxPanelWidth
            	+ (padding.left + padding.right + 2));
    	} else {
    		preferredWidth = getWidth();
    	}

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight;
        if (selectionChangeTransition == null) {
        	preferredHeight = getHeight();
        } else {
        	preferredHeight = 0;
            Accordion accordion = (Accordion)getComponent();

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
        }

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
        	panelHeight = selectionChangeTransition.selectedPanel.getHeight()
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
                Component selectedPanel = accordion.getSelectedPanel();

                if (panel == selectedPanel) {
                    panel.setVisible(true);

            		panel.setSize(contentWidth, contentHeight);
                    panel.setLocation(padding.left + 1, panelY + padding.top);

                    panelY += panelHeight;
                } else {
                    panel.setVisible(false);
                }
        	} else {
        		Component previousSelectedPanel = selectionChangeTransition.previousSelectedPanel;
        		Component selectedPanel = selectionChangeTransition.selectedPanel;

        		if (selectionChangeTransition.isRunning()) {
            		if (panel == previousSelectedPanel) {
                        panel.setLocation(padding.left + 1, panelY + padding.top);

                        int previousSelectedPanelHeight = Math.round((float)panelHeight * (1.0f
                    		- selectionChangeTransition.getEasedPercentComplete()));
            			selectionChangeTransition.previousSelectedPanelClipDecorator.setWidth(contentWidth);
                        selectionChangeTransition.previousSelectedPanelClipDecorator.setHeight(previousSelectedPanelHeight);

                        panelY += previousSelectedPanelHeight;
            		}

            		if (panel == selectedPanel) {
                        panel.setLocation(padding.left + 1, panelY + padding.top);

            			int selectedPanelHeight = Math.round((float)panelHeight
        					* selectionChangeTransition.getEasedPercentComplete());
            			selectionChangeTransition.selectedPanelClipDecorator.setWidth(contentWidth);
                        selectionChangeTransition.selectedPanelClipDecorator.setHeight(selectedPanelHeight);

            			panelY += selectedPanelHeight;
            		}
        		} else {
        			selectedPanel.setSize(previousSelectedPanel.getSize());
        			selectedPanel.setVisible(true);
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

        graphics.setStroke(new BasicStroke());
        graphics.setPaint(borderColor);
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

    // Accordion events
    public void panelInserted(Accordion accordion, int index) {
		if (selectionChangeTransition != null) {
			selectionChangeTransition.stop();
			selectionChangeTransition = null;
		}

		// Create a new button for the panel
        Component panel = accordion.getPanels().get(index);
        PanelHeader panelHeader = new PanelHeader(new ButtonData(Accordion.getIcon(panel),
            Accordion.getName(panel)));

        accordion.add(panelHeader);
        panelHeader.setGroup(panelHeaderGroup);
        panelHeaders.insert(panelHeader, index);

        invalidateComponent();
    }

    public void panelsRemoved(Accordion accordion, int index, Sequence<Component> panels) {
		if (selectionChangeTransition != null) {
			selectionChangeTransition.stop();
			selectionChangeTransition = null;
		}

		// Remove the buttons
        Sequence<PanelHeader> removed = panelHeaders.remove(index, panels.getLength());

        for (int i = 0, n = removed.getLength(); i < n; i++) {
            PanelHeader panelHeader = removed.get(i);
            panelHeader.setGroup((Group)null);
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
	    			Component previousSelectedPanel = accordion.getPanels().get(previousSelectedIndex);
	    			Component selectedPanel = accordion.getPanels().get(selectedIndex);

	        		selectionChangeTransition = new SelectionChangeTransition(previousSelectedPanel, selectedPanel,
	    				SELECTION_CHANGE_DURATION, SELECTION_CHANGE_RATE);

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
