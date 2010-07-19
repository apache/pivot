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
package org.apache.pivot.wtk.skin;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.SwingAdapter;
import org.apache.pivot.wtk.SwingAdapterListener;

/**
 * Swing adapter skin.
 */
public class SwingAdapterSkin extends ComponentSkin implements SwingAdapterListener {
    @Override
    public void install(Component component) {
        super.install(component);

        SwingAdapter swingAdapter = (SwingAdapter)component;
        swingAdapter.getSwingAdapterListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        return getPreferredSize().width;
    }

    @Override
    public int getPreferredHeight(int width) {
        return getPreferredSize().height;
    }

    @Override
    public Dimensions getPreferredSize() {
        SwingAdapter swingAdapter = (SwingAdapter)getComponent();
        JComponent swingComponent = swingAdapter.getSwingComponent();

        int preferredWidth = 0;
        int preferredHeight = 0;
        if (swingComponent != null) {
            java.awt.Dimension preferredSize = swingComponent.getPreferredSize();
            preferredWidth = preferredSize.width;
            preferredHeight = preferredSize.height;
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public void layout() {
        SwingAdapter swingAdapter = (SwingAdapter)getComponent();
        JComponent swingComponent = swingAdapter.getSwingComponent();

        if (swingComponent != null) {
            swingComponent.setSize(getWidth(), getHeight());
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        SwingAdapter swingAdapter = (SwingAdapter)getComponent();
        JComponent swingComponent = swingAdapter.getSwingComponent();

        if (swingComponent != null) {
            swingComponent.paint(graphics);
        }
    }

    @Override
    public boolean mouseMove(Component component, int x, int y) {
        return processMouseEvent(component, MouseEvent.MOUSE_MOVED, x, y);
    }

    @Override
    public void mouseOver(Component component) {
        processMouseEvent(component, MouseEvent.MOUSE_ENTERED, 0, 0); // TODO
    }

    @Override
    public void mouseOut(Component component) {
        processMouseEvent(component, MouseEvent.MOUSE_EXITED, 0, 0); // TODO
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        return processMouseEvent(component, MouseEvent.MOUSE_PRESSED, x, y);
    }

    @Override
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        return processMouseEvent(component, MouseEvent.MOUSE_RELEASED, x, y);
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        return processMouseEvent(component, MouseEvent.MOUSE_CLICKED, x, y);
    }

    @Override
    public boolean mouseWheel(Component component, Mouse.ScrollType scrollType,
        int scrollAmount, int wheelRotation, int x, int y) {
        // TODO
        return false;
    }

    @Override
    public boolean keyTyped(Component component, char character) {
        return processKeyEvent(component);
    }

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        return processKeyEvent(component);
    }

    @Override
    public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        return processKeyEvent(component);
    }

    @Override
    public void enabledChanged(Component component) {
        // TODO? User input events won't get fired, but Swing component may have
        // focus, so we need to clear it.
    }

    @Override
    public void focusedChanged(Component component, Component obverseComponent) {
        // TODO?
    }

    private boolean processMouseEvent(Component component, int id, int x, int y) {
        boolean consumed = false;

        SwingAdapter swingAdapter = (SwingAdapter)getComponent();
        JComponent swingComponent = swingAdapter.getSwingComponent();

        if (swingComponent != null) {
            Display display = component.getDisplay();
            ApplicationContext.DisplayHost displayHost = display.getDisplayHost();
            MouseEvent currentAWTEvent = (MouseEvent)displayHost.getCurrentAWTEvent();

            if (currentAWTEvent != null) {
                MouseEvent mouseEvent = new MouseEvent(swingComponent, id,
                    currentAWTEvent.getWhen(),
                    currentAWTEvent.getModifiers(),
                    x, y,
                    currentAWTEvent.getXOnScreen(),
                    currentAWTEvent.getYOnScreen(),
                    currentAWTEvent.getClickCount(),
                    currentAWTEvent.isPopupTrigger(),
                    currentAWTEvent.getButton());

                swingComponent.dispatchEvent(mouseEvent);

                consumed = mouseEvent.isConsumed();
            }
        }

        return consumed;
    }

    private boolean processKeyEvent(Component component) {
        boolean consumed = false;

        SwingAdapter swingAdapter = (SwingAdapter)getComponent();
        JComponent swingComponent = swingAdapter.getSwingComponent();

        if (swingComponent != null) {
            Display display = component.getDisplay();
            ApplicationContext.DisplayHost displayHost = display.getDisplayHost();
            KeyEvent currentAWTEvent = (KeyEvent)displayHost.getCurrentAWTEvent();

            if (currentAWTEvent != null) {
                KeyEvent keyEvent = new KeyEvent(swingComponent,
                    currentAWTEvent.getID(),
                    currentAWTEvent.getWhen(),
                    currentAWTEvent.getModifiers(),
                    currentAWTEvent.getKeyCode(),
                    currentAWTEvent.getKeyChar(),
                    currentAWTEvent.getKeyLocation());

                swingComponent.dispatchEvent(keyEvent);

                consumed = keyEvent.isConsumed();
            }
        }

        return consumed;
    }

    @Override
    public void swingComponentChanged(SwingAdapter swingAdapter, JComponent previousSwingComponent) {
        invalidateComponent();
    }
}
