package pivot.wtk.skin.terra;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

import pivot.collections.Sequence;
import pivot.wtk.Alert;
import pivot.wtk.Checkbox;
import pivot.wtk.Component;
import pivot.wtk.ComponentListener;
import pivot.wtk.Container;
import pivot.wtk.ContainerListener;
import pivot.wtk.Cursor;
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
import pivot.wtk.PushButton;
import pivot.wtk.RadioButton;
import pivot.wtk.Rollup;
import pivot.wtk.ScrollBar;
import pivot.wtk.ScrollPane;
import pivot.wtk.Spacer;
import pivot.wtk.SplitPane;
import pivot.wtk.TableViewHeader;
import pivot.wtk.TableView;
import pivot.wtk.TabPane;
import pivot.wtk.TextInput;
import pivot.wtk.Theme;
import pivot.wtk.Tooltip;
import pivot.wtk.TreeView;
import pivot.wtk.Window;

public final class TerraTheme extends Theme {
    private class DropShadowDecorator implements Decorator {
        Decorator decorator = null;

        public DropShadowDecorator(Decorator decorator) {
            this.decorator = decorator;
        }

        public Decorator getDecorator() {
            return decorator;
        }

        public Graphics2D prepare(Component component, Graphics2D graphics) {
            // Paint the drop shadow
            Graphics2D shadowGraphics = (Graphics2D)graphics.create();
            shadowGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                DROP_SHADOW_OPACITY));
            shadowGraphics.setColor(DROP_SHADOW_COLOR);

            shadowGraphics.setClip(null);
            shadowGraphics.fillRect(DROP_SHADOW_OFFSET, DROP_SHADOW_OFFSET,
                component.getWidth(), component.getHeight());

            return graphics;
        }

        public void update() {
            // No-op
        }
    }

    private class DisplayMonitor implements ContainerListener {
        public void componentInserted(Container container, int index) {
            monitorWindow((Window)container.getComponents().get(index));
        }

        public void componentsRemoved(Container container, int index,
            Sequence<Component> components) {
            for (int i = 0, n = components.getLength(); i < n; i++) {
                unmonitorWindow((Window)components.get(i));
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

    private class WindowMonitor implements ComponentListener {
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
    }

    private DisplayMonitor displayMonitor = new DisplayMonitor();
    private WindowMonitor windowMonitor = new WindowMonitor();

    private static final Color DROP_SHADOW_COLOR = Color.BLACK;
    private static final float DROP_SHADOW_OPACITY = 0.33f;
    private static final int DROP_SHADOW_OFFSET = 6;

    public TerraTheme() {
        componentSkinMap.put(Alert.class, AlertSkin.class);
        componentSkinMap.put(Checkbox.class, CheckboxSkin.class);
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
        // Attach shadow decorator and repaint
        window.setDecorator(new DropShadowDecorator(window.getDecorator()));
        repaintShadowRegion(window);

        // Add component listener
        window.getComponentListeners().add(windowMonitor);
    }

    private void unmonitorWindow(Window window) {
        // Remove component listener
        window.getComponentListeners().remove(windowMonitor);

        // Remove shadow decorator and repaint
        window.setDecorator(((DropShadowDecorator)window.getDecorator()).getDecorator());
        repaintShadowRegion(window);
    }

    private void repaintShadowRegion(Window window) {
        Display.getInstance().repaint(window.getX() + DROP_SHADOW_OFFSET,
            window.getY() + DROP_SHADOW_OFFSET,
            window.getWidth(), window.getHeight());
    }
}
