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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import pivot.collections.Sequence;
import pivot.wtk.Alert;
import pivot.wtk.Checkbox;
import pivot.wtk.Component;
import pivot.wtk.ComponentListener;
import pivot.wtk.ComponentStateListener;
import pivot.wtk.Container;
import pivot.wtk.ContainerListener;
import pivot.wtk.Cursor;
import pivot.wtk.DatePicker;
import pivot.wtk.Decorator;
import pivot.wtk.Dialog;
import pivot.wtk.Display;
import pivot.wtk.DragHandler;
import pivot.wtk.DropHandler;
import pivot.wtk.Expander;
import pivot.wtk.FocusTraversalPolicy;
import pivot.wtk.Form;
import pivot.wtk.Frame;
import pivot.wtk.LinkButton;
import pivot.wtk.ListButton;
import pivot.wtk.ListView;
import pivot.wtk.MenuBar;
import pivot.wtk.MenuButton;
import pivot.wtk.MenuPopup;
import pivot.wtk.Menu;
import pivot.wtk.Meter;
import pivot.wtk.Popup;
import pivot.wtk.PushButton;
import pivot.wtk.RadioButton;
import pivot.wtk.Rollup;
import pivot.wtk.ScrollBar;
import pivot.wtk.ScrollPane;
import pivot.wtk.Skin;
import pivot.wtk.Spacer;
import pivot.wtk.Spinner;
import pivot.wtk.SplitPane;
import pivot.wtk.TableViewHeader;
import pivot.wtk.TableView;
import pivot.wtk.TabPane;
import pivot.wtk.TextInput;
import pivot.wtk.Theme;
import pivot.wtk.Tooltip;
import pivot.wtk.TreeView;
import pivot.wtk.Window;

/**
 *
 *
 * @author gbrown
 * @author tvolkert
 */
public final class TerraTheme extends Theme {
    /**
     * Adds drop shadows to all windows and blurs disabled windows.
     *
     * @author gbrown
     * @author tvolkert
     */
    private static class WindowDecorator implements Decorator {
        private BufferedImage bufferedImage = null;
        private boolean blurred = false;

        private Decorator decorator = null;
        private Component component = null;
        private Graphics2D graphics = null;

        private static final int BLUR_RADIUS = 9;

        public WindowDecorator(Decorator decorator) {
            this.decorator = decorator;
        }

        public Decorator getDecorator() {
            return decorator;
        }

        public Graphics2D prepare(Component component, Graphics2D graphics) {
            if (decorator != null) {
                graphics = decorator.prepare(component, graphics);
            }

            this.component = component;
            this.graphics = graphics;

            // Paint the drop shadow
            Graphics2D shadowGraphics = (Graphics2D)graphics.create();
            shadowGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                DROP_SHADOW_OPACITY));
            shadowGraphics.setColor(DROP_SHADOW_COLOR);

            shadowGraphics.setClip(null);
            shadowGraphics.fillRect(DROP_SHADOW_OFFSET, DROP_SHADOW_OFFSET,
                component.getWidth(), component.getHeight());

            if (!component.isEnabled()) {
                // Prepare to blur
                int width = component.getWidth();
                int height = component.getHeight();

                if (bufferedImage == null
                    || bufferedImage.getWidth() != width
                    || bufferedImage.getHeight() != height) {
                    // Create the buffered image
                    bufferedImage = new BufferedImage(width, height,
                        BufferedImage.TYPE_INT_RGB);

                    graphics = bufferedImage.createGraphics();
                    blurred = false;
                } else {
                    // No need to paint - we'll do it in update()
                    graphics = null;
                }
            }

            return graphics;
        }

        public void update() {
            if (decorator != null) {
                decorator.update();
            }

            if (!component.isEnabled()) {
                if (!blurred) {
                    Graphics2D imageGraphics = bufferedImage.createGraphics();

                    float[] kernel = new float[BLUR_RADIUS * BLUR_RADIUS];
                    for (int i = 0, n = kernel.length; i < n; i++) {
                        kernel[i] = 1f / n;
                    }

                    ConvolveOp blur = new ConvolveOp(new Kernel(BLUR_RADIUS, BLUR_RADIUS,
                        kernel), ConvolveOp.EDGE_NO_OP, null);

                    // Perform the blur operation
                    bufferedImage = blur.filter(bufferedImage, null);
                    blurred = true;

                    imageGraphics.dispose();
                }

                // Draw the blurred image to the real graphics
                this.graphics.drawImage(bufferedImage, 0, 0, null);
            } else {
                // Free the bufered image
                bufferedImage = null;
            }
        }
    }

    private class DisplayMonitor implements ContainerListener {
        public void componentInserted(Container container, int index) {
            Window window = (Window)container.getComponents().get(index);
            monitorWindow(window);
        }

        public void componentsRemoved(Container container, int index,
            Sequence<Component> removed) {
            for (int i = 0, n = removed.getLength(); i < n; i++) {
                Window window = (Window)removed.get(i);
                unmonitorWindow(window);
            }
        }

        public void contextKeyChanged(Container container, String previousContextKey) {
            // No-op
        }

        public void focusTraversalPolicyChanged(Container container,
            FocusTraversalPolicy previousFocusTraversalPolicy) {
            // No-op
        }
    }

    private class WindowMonitor implements ComponentListener, ComponentStateListener {
        public void skinClassChanged(Component component, Class<? extends Skin> previousSkinClass) {
            // No-op
        }

        public void parentChanged(Component component, Container previousParent) {
            // No-op
        }

        public void sizeChanged(Component component, int previousWidth, int previousHeight) {
            // Repaint previous shadow region
            Window window = (Window)component;

            Display.getInstance().repaint(window.getX() + DROP_SHADOW_OFFSET,
                window.getY() + DROP_SHADOW_OFFSET,
                previousWidth, previousHeight);

            // Repaint current shadow region
            repaintShadowRegion((Window)component);
        }

        public void locationChanged(Component component, int previousX, int previousY) {
            // Repaint previous shadow region
            Window window = (Window)component;

            Display.getInstance().repaint(previousX + DROP_SHADOW_OFFSET,
                previousY + DROP_SHADOW_OFFSET,
                window.getWidth(), window.getHeight());

            // Repaint current shadow region
            repaintShadowRegion((Window)component);
        }

        public void visibleChanged(Component component) {
            repaintShadowRegion((Window)component);
        }

        public void decoratorChanged(Component component, Decorator previousDecorator) {
            throw new IllegalStateException("Can't change decorator when window is open.");
        }

        public void cursorChanged(Component component, Cursor previousCursor) {
            // No-op
        }

        public void tooltipTextChanged(Component component, String previousTooltipText) {
            // No-op
        }

        public void dragHandlerChanged(Component component, DragHandler previousDragHandler) {
            // No-op
        }

        public void dropHandlerChanged(Component component, DropHandler previousDropHandler) {
            // No-op
        }

        public void enabledChanged(Component component) {
            component.repaint();
        }

        public void focusedChanged(Component component, boolean temporary) {
            // No-op
        }
    }

    private DisplayMonitor displayMonitor = new DisplayMonitor();
    private WindowMonitor windowMonitor = new WindowMonitor();

    private static final Color DROP_SHADOW_COLOR = Color.BLACK;
    private static final float DROP_SHADOW_OPACITY = 0.33f;
    private static final int DROP_SHADOW_OFFSET = 6;

    public TerraTheme() {
        componentSkinMap.put(Alert.class, AlertSkin.class);
        componentSkinMap.put(Checkbox.class, CheckboxSkin.class);
        componentSkinMap.put(DatePicker.class, DatePickerSkin.class);
        componentSkinMap.put(Dialog.class, DialogSkin.class);
        componentSkinMap.put(Expander.class, ExpanderSkin.class);
        componentSkinMap.put(Form.class, FormSkin.class);
        componentSkinMap.put(Frame.class, FrameSkin.class);
        componentSkinMap.put(LinkButton.class, LinkButtonSkin.class);
        componentSkinMap.put(ListButton.class, ListButtonSkin.class);
        componentSkinMap.put(ListView.class, ListViewSkin.class);
        componentSkinMap.put(MenuBar.class, MenuBarSkin.class);
        componentSkinMap.put(MenuButton.class, MenuButtonSkin.class);
        componentSkinMap.put(MenuPopup.class, MenuPopupSkin.class);
        componentSkinMap.put(Menu.class, MenuSkin.class);
        componentSkinMap.put(Meter.class, MeterSkin.class);
        componentSkinMap.put(PushButton.class, PushButtonSkin.class);
        componentSkinMap.put(RadioButton.class, RadioButtonSkin.class);
        componentSkinMap.put(Rollup.class, RollupSkin.class);
        componentSkinMap.put(ScrollBar.class, ScrollBarSkin.class);
        componentSkinMap.put(ScrollPane.Corner.class, ScrollPaneCornerSkin.class);
        componentSkinMap.put(Spacer.class, SpacerSkin.class);
        componentSkinMap.put(Spinner.class, SpinnerSkin.class);
        componentSkinMap.put(SplitPane.class, SplitPaneSkin.class);
        componentSkinMap.put(TableViewHeader.class, TableViewHeaderSkin.class);
        componentSkinMap.put(TableView.class, TableViewSkin.class);
        componentSkinMap.put(TabPane.class, TabPaneSkin.class);
        componentSkinMap.put(TextInput.class, TextInputSkin.class);
        componentSkinMap.put(Tooltip.class, TooltipSkin.class);
        componentSkinMap.put(TreeView.class, TreeViewSkin.class);

        componentSkinMap.put(AbstractFrameSkin.FrameButton.class, AbstractFrameSkin.FrameButtonSkin.class);
        componentSkinMap.put(ExpanderSkin.ShadeButton.class, ExpanderSkin.ShadeButtonSkin.class);
        componentSkinMap.put(RollupSkin.RollupButton.class, RollupSkin.RollupButtonSkin.class);
        componentSkinMap.put(ScrollBarSkin.ScrollButton.class, ScrollBarSkin.ScrollButtonSkin.class);
        componentSkinMap.put(ScrollBarSkin.ScrollHandle.class, ScrollBarSkin.ScrollHandleSkin.class);
        componentSkinMap.put(SplitPaneSkin.Splitter.class, SplitPaneSkin.SplitterSkin.class);
        componentSkinMap.put(SplitPaneSkin.SplitterShadow.class, SplitPaneSkin.SplitterShadowSkin.class);
        componentSkinMap.put(TabPaneSkin.TabButton.class, TabPaneSkin.TabButtonSkin.class);
    }

    public void install() {
        Display display = Display.getInstance();

        // Monitor existing windows
        for (Component component : display.getComponents()) {
            monitorWindow((Window)component);
        }

        // Listen for window open/close events
        display.getContainerListeners().add(displayMonitor);
    }

    public void uninstall() {
        Display display = Display.getInstance();

        // Un-monitor existing windows
        for (Component component : display.getComponents()) {
            unmonitorWindow((Window)component);
        }

        // Ignore window open/close events
        display.getContainerListeners().remove(displayMonitor);
    }

    private void monitorWindow(Window window) {
        if (!(window instanceof Popup)) {
            // Attach shadow decorator and repaint
            Decorator decorator = window.getDecorator();
            window.setDecorator(new WindowDecorator(decorator));
            repaintShadowRegion(window);

            // Add component listeners
            window.getComponentListeners().add(windowMonitor);
            window.getComponentStateListeners().add(windowMonitor);
        }
    }

    private void unmonitorWindow(Window window) {
        if (!(window instanceof Popup)) {
            // Remove component listener
            window.getComponentListeners().remove(windowMonitor);
            window.getComponentStateListeners().remove(windowMonitor);

            // Remove shadow decorator and repaint
            WindowDecorator windowDecorator = (WindowDecorator)window.getDecorator();
            Decorator decorator = windowDecorator.getDecorator();
            window.setDecorator(decorator);
            repaintShadowRegion(window);
        }
    }

    private void repaintShadowRegion(Window window) {
        Display.getInstance().repaint(window.getX() + DROP_SHADOW_OFFSET,
            window.getY() + DROP_SHADOW_OFFSET,
            window.getWidth(), window.getHeight());
    }
}
