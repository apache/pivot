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

import java.awt.Color;
import java.awt.Graphics2D;

import pivot.collections.Dictionary;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Dimensions;
import pivot.wtk.Insets;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.Sheet;
import pivot.wtk.Window;
import pivot.wtk.effects.DropShadowDecorator;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;
import pivot.wtk.skin.WindowSkin;

/**
 * Sheet skin class.
 * <p>
 * TODO Add support for the "resizable" flag. It current exists but does nothing.
 *
 * @author gbrown
 * @author tvolkert
 */
public class SheetSkin extends WindowSkin implements Sheet.Skin {
    private Color borderColor = new Color(0x99, 0x99, 0x99);
    private Color bevelColor = new Color(0xF7, 0xF5, 0xEB);
    private Insets padding = new Insets(8);
    private boolean resizable = false;

    private SlideTransition openTransition = null;
    private SlideTransition closeTransition = null;

    private ComponentMouseButtonListener ownerMouseButtonListener =
        new ComponentMouseButtonListener() {
        public void mouseDown(Component component, Mouse.Button button, int x, int y) {
            Window owner = (Window)component;
            Component ownerContent = owner.getContent();

            if (ownerContent != null
                && !ownerContent.isEnabled()
                && owner.getComponentAt(x, y) == ownerContent) {
                ApplicationContext.beep();
            }
        }

        public void mouseUp(Component component, Mouse.Button button, int x, int y) {
        }

        public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        }
    };

    private DropShadowDecorator dropShadowDecorator = null;

    private static final int SLIDE_DURATION = 300;
    private static final int SLIDE_RATE = 30;

    public SheetSkin() {
        setBackgroundColor(new Color(0xF7, 0xF5, 0xEB));
    }

    @Override
    public void install(Component component) {
        validateComponentType(component, Sheet.class);

        super.install(component);

        Sheet sheet = (Sheet)component;

        // Attach the drop-shadow decorator
        dropShadowDecorator = new DropShadowDecorator(3, 3, 3);
        sheet.getDecorators().add(dropShadowDecorator);
    }

    @Override
    public void uninstall() {
        Sheet sheet = (Sheet)getComponent();

        // Detach the drop shadow decorator
        sheet.getDecorators().remove(dropShadowDecorator);
        dropShadowDecorator = null;

        super.uninstall();
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        Sheet sheet = (Sheet)getComponent();
        Component content = sheet.getContent();

        if (content != null
            && content.isDisplayable()) {
            if (height != -1) {
                height = Math.max(height - (padding.top + padding.bottom + 2), 0);
            }

            preferredWidth = content.getPreferredWidth(height);
        }

        preferredWidth += (padding.left + padding.right + 2);

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        Sheet sheet = (Sheet)getComponent();
        Component content = sheet.getContent();

        if (content != null
            && content.isDisplayable()) {
            if (width != -1) {
                width = Math.max(width - (padding.left + padding.right + 2), 0);
            }

            preferredHeight = content.getPreferredHeight(width);
        }

        preferredHeight += (padding.top + padding.bottom + 2);

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        Sheet sheet = (Sheet)getComponent();
        Component content = sheet.getContent();

        if (content != null
            && content.isDisplayable()) {
            Dimensions preferredContentSize = content.getPreferredSize();
            preferredWidth = preferredContentSize.width;
            preferredHeight = preferredContentSize.height;
        }

        preferredWidth += (padding.left + padding.right + 2);
        preferredHeight += (padding.top + padding.bottom + 2);

        Dimensions preferredSize = new Dimensions(preferredWidth, preferredHeight);

        return preferredSize;
    }

    public void layout() {
        int width = getWidth();
        int height = getHeight();

        Sheet sheet = (Sheet)getComponent();
        Component content = sheet.getContent();

        if (content != null) {
            if (content.isDisplayable()) {
                content.setVisible(true);

                content.setLocation(padding.left + 1, padding.top + 1);

                int contentWidth = Math.max(width - (padding.left + padding.right + 2), 0);
                int contentHeight = Math.max(height - (padding.top + padding.bottom + 2), 0);

                content.setSize(contentWidth, contentHeight);
            } else {
                content.setVisible(false);
            }
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        int width = getWidth();
        int height = getHeight();

        graphics.setPaint(borderColor);
        graphics.drawRect(0, 0, width - 1, height - 1);

        graphics.setPaint(bevelColor);
        graphics.drawLine(1, height - 2, width - 2, height - 2);
    }

    @Override
    public boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        Sheet sheet = (Sheet)getComponent();

        if (keyCode == Keyboard.KeyCode.ENTER) {
            sheet.close(true);
            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.ESCAPE) {
            sheet.close(false);
            consumed = true;
        } else {
            consumed = super.keyPressed(keyCode, keyLocation);
        }

        return consumed;
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

        setBorderColor(Color.decode(borderColor));
    }

    public Color getBevelColor() {
        return bevelColor;
    }

    public void setBevelColor(Color bevelColor) {
        if (bevelColor == null) {
            throw new IllegalArgumentException("bevelColor is null.");
        }

        this.bevelColor = bevelColor;
        repaintComponent();
    }

    public final void setBevelColor(String bevelColor) {
        if (bevelColor == null) {
            throw new IllegalArgumentException("bevelColor is null.");
        }

        setBevelColor(Color.decode(bevelColor));
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

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
        invalidateComponent();
    }

    @Override
    public void windowOpened(final Window window) {
        super.windowOpened(window);

        Window owner = window.getOwner();
        owner.getComponentMouseButtonListeners().add(ownerMouseButtonListener);

        ApplicationContext.queueCallback(new Runnable() {
            public void run() {
                openTransition = new SlideTransition(window, 0, 0,
                    -window.getHeight(), 0, false, SLIDE_DURATION, SLIDE_RATE);
                openTransition.start(new TransitionListener() {
                    public void transitionCompleted(Transition transition) {
                        openTransition = null;
                    }
                });
            }
        });
    }

    public boolean previewSheetClose(final Sheet sheet, final boolean result) {
        // Start a close transition, return false, and close the window
        // when the transition is complete
        boolean close = true;

        if (closeTransition == null) {
            int duration = SLIDE_DURATION;
            int beginX = 0;
            int beginY = 0;

            if (openTransition != null) {
                // Stop the open transition
                openTransition.stop();

                // Record its progress so we can reverse it at the right point
                duration = openTransition.getElapsedTime();
                beginX = openTransition.getX();
                beginY = openTransition.getY();

                openTransition = null;
            }

            if (duration > 0) {
                closeTransition = new SlideTransition(sheet, beginX, 0,
                    beginY, -sheet.getHeight(), true, duration, SLIDE_RATE);
                closeTransition.start(new TransitionListener() {
                    public void transitionCompleted(Transition transition) {
                        sheet.close(result);
                        closeTransition = null;
                    }
                });

                close = false;
            }
        } else {
            close = !closeTransition.isRunning();
        }

        return close;
    }

    public void sheetClosed(Sheet sheet) {
        Window owner = sheet.getOwner();
        owner.getComponentMouseButtonListeners().remove(ownerMouseButtonListener);
    }
}
