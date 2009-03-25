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
import pivot.util.Vote;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Dimensions;
import pivot.wtk.Insets;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.Sheet;
import pivot.wtk.SheetStateListener;
import pivot.wtk.Theme;
import pivot.wtk.Window;
import pivot.wtk.effects.DropShadowDecorator;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;
import pivot.wtk.skin.WindowSkin;

/**
 * Sheet skin class.
 * <p>
 * TODO Wire up the "resizable" flag. It current exists but does nothing.
 *
 * @author gbrown
 * @author tvolkert
 */
public class TerraSheetSkin extends WindowSkin implements SheetStateListener {
    private Color borderColor;
    private Insets padding;
    private boolean resizable;

    // Derived colors
    private Color bevelColor;

    private SlideTransition openTransition = null;
    private SlideTransition closeTransition = null;

    private ComponentMouseButtonListener ownerMouseButtonListener =
        new ComponentMouseButtonListener() {
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            Window owner = (Window)component;
            Component ownerContent = owner.getContent();

            if (ownerContent != null
                && !ownerContent.isEnabled()
                && owner.getComponentAt(x, y) == ownerContent) {
                ApplicationContext.beep();
            }

            return false;
        }

        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            return false;
        }
    };

    private DropShadowDecorator dropShadowDecorator = null;

    private static final int SLIDE_DURATION = 250;
    private static final int SLIDE_RATE = 30;

    public TerraSheetSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();

        Color backgroundColor = theme.getColor(11);
        backgroundColor = new Color(backgroundColor.getRed(), backgroundColor.getGreen(),
            backgroundColor.getBlue(), 235);
        setBackgroundColor(backgroundColor);

        borderColor = theme.getColor(7);
        padding = new Insets(8);
        resizable = false;

        // Set the derived colors
        bevelColor = TerraTheme.darken(backgroundColor);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Sheet sheet = (Sheet)component;
        sheet.getSheetStateListeners().add(this);

        // Attach the drop-shadow decorator
        dropShadowDecorator = new DropShadowDecorator(3, 3, 3);
        sheet.getDecorators().add(dropShadowDecorator);
    }

    @Override
    public void uninstall() {
        Sheet sheet = (Sheet)getComponent();
        sheet.getSheetStateListeners().remove(this);

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
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        Sheet sheet = (Sheet)getComponent();

        if (keyCode == Keyboard.KeyCode.ENTER) {
            sheet.close(true);
            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.ESCAPE) {
            sheet.close(false);
            consumed = true;
        } else {
            consumed = super.keyPressed(component, keyCode, keyLocation);
        }

        return consumed;
    }

    @Override
    public void setBackgroundColor(Color backgroundColor) {
        super.setBackgroundColor(backgroundColor);
        bevelColor = TerraTheme.darken(backgroundColor);
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

    public Vote previewSheetClose(final Sheet sheet, final boolean result) {
        // Start a close transition, return false, and close the window
        // when the transition is complete
        Vote vote = Vote.APPROVE;

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

                vote = Vote.DEFER;
            }
        } else {
            vote = (closeTransition.isRunning()) ? Vote.DEFER : Vote.APPROVE;
        }

        return vote;
    }

    public void sheetCloseVetoed(Sheet sheet, Vote reason) {
        if (reason == Vote.DENY
            && closeTransition != null) {
            closeTransition.stop();
            closeTransition = null;
        }
    }

    public void sheetClosed(Sheet sheet) {
        Window owner = sheet.getOwner();
        owner.getComponentMouseButtonListeners().remove(ownerMouseButtonListener);
    }
}
