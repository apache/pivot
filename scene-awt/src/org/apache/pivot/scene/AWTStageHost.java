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
package org.apache.pivot.scene;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;

import org.apache.pivot.scene.Stage;

/**
 * AWT scene graph host component.
 */
public class AWTStageHost extends java.awt.Component {
    private static final long serialVersionUID = 0;

    private Stage stage = new Stage() {
        @Override
        public void invalidate() {
            AWTStageHost.this.invalidate();
        }

        @Override
        public void repaintHost(int x, int y, int width, int height) {
            AWTStageHost.this.repaint(x, y, width, height);
        }

        @Override
        public Graphics getHostGraphics() {
            Graphics2D graphics2D = (Graphics2D)AWTStageHost.this.getGraphics();

            Graphics graphics = null;
            if (graphics2D != null) {
                graphics2D.clipRect(0, 0, getWidth(), getHeight());
                graphics = new AWTGraphics(graphics2D);
            }

            return graphics;
        }

        @Override
        public void requestNativeFocus() {
            requestFocusInWindow();
        }

        @Override
        public Object getNativeHost() {
            return AWTStageHost.this;
        }
    };

    private Node mouseCapturer = null;
    private Node focusOwner = null;

    private boolean doubleBuffered = true;
    private boolean disableVolatileBuffer = true;

    private boolean debugPaint = false;
    private boolean debugFocus = false;

    private boolean paintPending = false;

    public AWTStageHost() {
        enableEvents(AWTEvent.COMPONENT_EVENT_MASK
            | AWTEvent.FOCUS_EVENT_MASK
            | AWTEvent.MOUSE_EVENT_MASK
            | AWTEvent.MOUSE_MOTION_EVENT_MASK
            | AWTEvent.MOUSE_WHEEL_EVENT_MASK
            | AWTEvent.KEY_EVENT_MASK);

        try {
            System.setProperty("sun.awt.noerasebackground", "true");
            System.setProperty("sun.awt.erasebackgroundonresize", "false");
        } catch (SecurityException exception) {
            // No-op
        }

        setFocusTraversalKeysEnabled(false);

        // Listen for changes to the font desktop hints property
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.addPropertyChangeListener("awt.font.desktophints", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                invalidate();
            }
        });
    }

    public Stage getStage() {
        return stage;
    }

    @Override
    public boolean isDoubleBuffered() {
        return doubleBuffered;
    }

    public void setDoubleBuffered(boolean doubleBuffered) {
        this.doubleBuffered = doubleBuffered;
    }

    public boolean getDisableVolatileBuffer() {
        return disableVolatileBuffer;
    }

    public void setDisableVolatileBuffer(boolean disableVolatileBuffer) {
        this.disableVolatileBuffer = disableVolatileBuffer;
    }

    public boolean getDebugPaint() {
        return debugPaint;
    }

    public void setDebugPaint(boolean debugPaint) {
        this.debugPaint = debugPaint;
    }

    public boolean getDebugFocus() {
        return debugFocus;
    }

    public void setDebugFocus(boolean debugFocus) {
        this.debugFocus = debugFocus;

        // TODO

        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(stage.getPreferredWidth(), stage.getPreferredHeight());
    }

    @Override
    public void repaint(int x, int y, int width, int height) {
        // Ensure that the repaint call is properly bounded (some
        // implementations of AWT do not properly clip the repaint call
        // when x or y is negative: the negative value is converted to 0,
        // but the width/height is not adjusted)
        if (x < 0) {
            width = Math.max(width + x, 0);
            x = 0;
        }

        if (y < 0) {
            height = Math.max(height + y, 0);
            y = 0;
        }

        if (width > 0
            && height > 0) {
            super.repaint(x, y, width, height);

            paintPending = true;
        }
    }

    @Override
    public void paint(java.awt.Graphics graphics) {
        // Intersect the clip region with the bounds of this component
        // (for some reason, AWT does not do this automatically)
        graphics.clipRect(0, 0, getWidth(), getHeight());

        java.awt.Rectangle clipBounds = graphics.getClipBounds();
        if (clipBounds != null
            && !clipBounds.isEmpty()) {
            Graphics2D graphics2D = (Graphics2D)graphics;

            try {
                if (doubleBuffered) {
                    if (disableVolatileBuffer
                        || !paintVolatileBuffered(graphics2D)) {
                        if (!paintBuffered(graphics2D)) {
                            paintUnbuffered(graphics2D);
                        }
                    }
                } else {
                    paintUnbuffered(graphics2D);
                }

                if (debugPaint) {
                    Random random = new Random();
                    graphics.setColor(new java.awt.Color(random.nextInt(256),
                        random.nextInt(256), random.nextInt(256), 75));
                    graphics.fillRect(0, 0, getWidth(), getHeight());
                }
            } catch (RuntimeException exception) {
                exception.printStackTrace();
                throw exception;
            }
        }

        paintPending = false;
    }

    @Override
    public void update(java.awt.Graphics graphics) {
        paint(graphics);
    }

    /**
     * Attempts to paint the stage using an offscreen buffer.
     *
     * @param graphics
     * The source graphics context.
     *
     * @return
     * <tt>true</tt> if the stage was painted using the offscreen
     * buffer; <tt>false</tt>, otherwise.
     */
    private boolean paintBuffered(Graphics2D graphics) {
        boolean painted = false;

        // Paint the stage into an offscreen buffer
        GraphicsConfiguration gc = graphics.getDeviceConfiguration();
        java.awt.Rectangle clipBounds = graphics.getClipBounds();
        java.awt.image.BufferedImage bufferedImage =
            gc.createCompatibleImage(clipBounds.width, clipBounds.height, Transparency.OPAQUE);

        if (bufferedImage != null) {
            Graphics2D bufferedImageGraphics = (Graphics2D)bufferedImage.getGraphics();
            bufferedImageGraphics.setClip(0, 0, clipBounds.width, clipBounds.height);
            bufferedImageGraphics.translate(-clipBounds.x, -clipBounds.y);

            try {
                paintUnbuffered(bufferedImageGraphics);
                graphics.drawImage(bufferedImage, clipBounds.x, clipBounds.y, this);
            } finally {
                bufferedImageGraphics.dispose();
            }

            painted = true;
        }

        return painted;
    }

    /**
     * Attempts to paint the stage using a volatile offscreen buffer.
     *
     * @param graphics
     * The source graphics context.
     *
     * @return
     * <tt>true</tt> if the stage was painted using the offscreen
     * buffer; <tt>false</tt>, otherwise.
     */
    private boolean paintVolatileBuffered(Graphics2D graphics) {
        boolean painted = false;

        // Paint the stage into a volatile offscreen buffer
        GraphicsConfiguration gc = graphics.getDeviceConfiguration();
        java.awt.Rectangle clipBounds = graphics.getClipBounds();
        java.awt.image.VolatileImage volatileImage =
            gc.createCompatibleVolatileImage(clipBounds.width, clipBounds.height,
                Transparency.OPAQUE);

        if (volatileImage != null) {
            int valid = volatileImage.validate(gc);

            if (valid == java.awt.image.VolatileImage.IMAGE_OK
                || valid == java.awt.image.VolatileImage.IMAGE_RESTORED) {
                Graphics2D volatileImageGraphics = volatileImage.createGraphics();
                volatileImageGraphics.setClip(0, 0, clipBounds.width, clipBounds.height);
                volatileImageGraphics.translate(-clipBounds.x, -clipBounds.y);

                try {
                    paintUnbuffered(volatileImageGraphics);
                    graphics.drawImage(volatileImage, clipBounds.x, clipBounds.y, this);
                } finally {
                    volatileImageGraphics.dispose();
                }

                painted = !volatileImage.contentsLost();
            }
        }

        return painted;
    }

    /**
     * Paints the stage without any buffering.
     *
     * @param graphics
     */
    private void paintUnbuffered(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        AWTGraphics awtGraphics = new AWTGraphics(graphics);
        stage.paint(awtGraphics);
    }

    @Override
    protected void processComponentEvent(ComponentEvent event) {
        super.processComponentEvent(event);

        switch (event.getID()) {
            case ComponentEvent.COMPONENT_RESIZED: {
                stage.setSize(Math.max(getWidth(), 0), Math.max(getHeight(), 0));
                break;
            }
        }
    }

    @Override
    protected void processFocusEvent(FocusEvent event) {
        super.processFocusEvent(event);

        switch(event.getID()) {
            case FocusEvent.FOCUS_GAINED: {
                if (focusOwner != null
                    && focusOwner.isShowing()
                    && !focusOwner.isBlocked()) {
                    focusOwner.requestFocus();
                }

                focusOwner = null;

                break;
            }

            case FocusEvent.FOCUS_LOST: {
                focusOwner = Node.getFocusedNode();
                Node.clearFocus();

                break;
            }
        }
    }

    @Override
    protected void processMouseEvent(MouseEvent event) {
        super.processMouseEvent(event);

        int x = event.getX();
        int y = event.getY();

        // Set the mouse button state
        int mouseButtons = 0x00;

        int modifiersEx = event.getModifiersEx();
        if ((modifiersEx & MouseEvent.BUTTON1_DOWN_MASK) > 0) {
            mouseButtons |= Mouse.Button.LEFT.getMask();
        }

        if ((modifiersEx & MouseEvent.BUTTON2_DOWN_MASK) > 0) {
            mouseButtons |= Mouse.Button.MIDDLE.getMask();
        }

        if ((modifiersEx & MouseEvent.BUTTON3_DOWN_MASK) > 0) {
            mouseButtons |= Mouse.Button.RIGHT.getMask();
        }

        Mouse.setButtons(mouseButtons);

        // Get the button associated with this event
        Mouse.Button button = null;
        switch (event.getButton()) {
            case MouseEvent.BUTTON1: {
                button = Mouse.Button.LEFT;
                break;
            }

            case MouseEvent.BUTTON2: {
                button = Mouse.Button.MIDDLE;
                break;
            }

            case MouseEvent.BUTTON3: {
                button = Mouse.Button.RIGHT;
                break;
            }
        }

        // Process the event
        boolean consumed = false;
        try {
            switch(event.getID()) {
                case MouseEvent.MOUSE_ENTERED: {
                    stage.mouseEntered();
                    break;
                }

                case MouseEvent.MOUSE_EXITED: {
                    stage.mouseExited();
                    break;
                }

                case MouseEvent.MOUSE_PRESSED: {
                    consumed = stage.mousePressed(button, x, y);
                    break;
                }

                case MouseEvent.MOUSE_RELEASED: {
                    consumed = stage.mouseReleased(button, x, y);
                    break;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        if (consumed) {
            event.consume();
        }
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent event) {
        super.processMouseMotionEvent(event);

        if (!paintPending) {
            int x = event.getX();
            int y = event.getY();

            // Process the event
            boolean consumed = false;
            try {
                switch (event.getID()) {
                    case MouseEvent.MOUSE_MOVED: {
                        mouseCapturer = null;

                        consumed = stage.mouseMoved(x, y, false);
                        break;
                    }

                    case MouseEvent.MOUSE_DRAGGED: {
                        if (mouseCapturer == null) {
                            mouseCapturer = stage.getDescendantAt(x, y);
                        }

                        Point location = mouseCapturer.mapPointFromAncestor(stage, x, y);
                        consumed = mouseCapturer.mouseMoved(location.x, location.y, true);
                        break;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            if (consumed) {
                event.consume();
            }
        }
    }

    @Override
    protected void processMouseWheelEvent(MouseWheelEvent event) {
        super.processMouseWheelEvent(event);

        // Get the event coordinates
        int x = event.getX();
        int y = event.getY();

        // Get the scroll type
        Mouse.ScrollType scrollType = null;
        switch (event.getScrollType()) {
            case MouseWheelEvent.WHEEL_BLOCK_SCROLL: {
                scrollType = Mouse.ScrollType.BLOCK;
                break;
            }

            case MouseWheelEvent.WHEEL_UNIT_SCROLL: {
                scrollType = Mouse.ScrollType.UNIT;
                break;
            }
        }

        // Process the event
        boolean consumed = false;
        try {
            switch (event.getID()) {
                case MouseEvent.MOUSE_WHEEL: {
                    consumed = stage.mouseWheelScrolled(scrollType, event.getScrollAmount(),
                        event.getWheelRotation(), x, y);
                    break;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        if (consumed) {
            event.consume();
        }
    }

    @Override
    protected void processKeyEvent(KeyEvent event) {
        super.processKeyEvent(event);

        if (focusOwner != null
            && !focusOwner.isBlocked()) {
            int modifiersEx = event.getModifiersEx();
            int awtKeyLocation = event.getKeyLocation();

            // Set the keyboard modifier state
            int keyboardModifiers = 0;
            if ((modifiersEx & KeyEvent.SHIFT_DOWN_MASK) > 0) {
                keyboardModifiers |= Keyboard.Modifier.SHIFT.getMask();
            }

            // Ignore Control when Alt-Graphics is pressed
            if ((modifiersEx & KeyEvent.CTRL_DOWN_MASK) > 0
                && ((modifiersEx & KeyEvent.ALT_DOWN_MASK) == 0
                    || awtKeyLocation == KeyEvent.KEY_LOCATION_RIGHT)) {
                keyboardModifiers |= Keyboard.Modifier.CTRL.getMask();
            }

            if ((modifiersEx & KeyEvent.ALT_DOWN_MASK) > 0) {
                keyboardModifiers |= Keyboard.Modifier.ALT.getMask();
            }

            if ((modifiersEx & KeyEvent.META_DOWN_MASK) > 0) {
                keyboardModifiers |= Keyboard.Modifier.META.getMask();
            }

            Keyboard.setModifiers(keyboardModifiers);

            // Get the key location
            Keyboard.KeyLocation keyLocation = null;
            switch (awtKeyLocation) {
                case KeyEvent.KEY_LOCATION_STANDARD: {
                    keyLocation = Keyboard.KeyLocation.STANDARD;
                    break;
                }

                case KeyEvent.KEY_LOCATION_LEFT: {
                    keyLocation = Keyboard.KeyLocation.LEFT;
                    break;
                }

                case KeyEvent.KEY_LOCATION_RIGHT: {
                    keyLocation = Keyboard.KeyLocation.RIGHT;
                    break;
                }

                case KeyEvent.KEY_LOCATION_NUMPAD: {
                    keyLocation = Keyboard.KeyLocation.KEYPAD;
                    break;
                }
            }

            // Process the event
            boolean consumed = false;
            try {
                switch (event.getID()) {
                    case KeyEvent.KEY_PRESSED: {
                        int keyCode = event.getKeyCode();
                        consumed = focusOwner.keyPressed(keyCode, keyLocation);
                        break;
                    }

                    case KeyEvent.KEY_RELEASED: {
                        int keyCode = event.getKeyCode();
                        consumed = focusOwner.keyReleased(keyCode, keyLocation);
                        break;
                    }

                    case KeyEvent.KEY_TYPED: {
                        char keyChar = event.getKeyChar();
                        consumed = focusOwner.keyTyped(keyChar);
                        break;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            if (consumed) {
                event.consume();
            }
        }
    }
}
