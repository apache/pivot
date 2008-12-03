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
package pivot.wtk;

import java.awt.Graphics2D;
import pivot.wtk.skin.DisplaySkin;

/**
 * Container that serves as the root of a component hierarchy.
 *
 * @author gbrown
 */
public final class Display extends Container {
    private class ValidateCallback implements Runnable {
        public void run() {
            validate();
        }
    }

    private ApplicationContext applicationContext;

    private Point mouseDownLocation = null;
    private Point dragLocation = null;

    protected Display(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        super.setSkin(new DisplaySkin());
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    protected void setSkin(Skin skin) {
        throw new UnsupportedOperationException("Can't replace Display skin.");
    }

    @Override
    public void setLocation(int x, int y) {
        throw new UnsupportedOperationException("Can't change the location of the display.");
    }

    @Override
    public void invalidate() {
        if (isValid()) {
            super.invalidate();
            ApplicationContext.queueCallback(new ValidateCallback());
        }
    }

    @Override
    public void repaint(int x, int y, int width, int height, boolean immediate) {
        if (immediate) {
            Graphics2D graphics = applicationContext.getGraphics();
            graphics.clipRect(x, y, width, height);
            paint(graphics);
            graphics.dispose();
        } else {
            applicationContext.repaint(x, y, width, height);
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        if (dragLocation != null) {
            Visual dragRepresentation = Mouse.getDragRepresentation();

            if (dragRepresentation != null) {
                Point dragOffset = Mouse.getDragOffset();
                int tx = dragLocation.x - dragOffset.x;
                int ty = dragLocation.y - dragOffset.y;

                graphics.translate(tx, ty);
                dragRepresentation.paint(graphics);
            }
        }
    }

    @Override
    protected boolean mouseMove(int x, int y) {
        boolean consumed = super.mouseMove(x, y);

        int dragThreshold = ApplicationContext.getDragThreshold();

        if (dragLocation == null) {
            // A drag has not yet started
            if (mouseDownLocation != null) {
                if (Math.abs(x - mouseDownLocation.x) > dragThreshold
                    || Math.abs(y - mouseDownLocation.y) > dragThreshold) {
                    Component descendant = getDescendantAt(mouseDownLocation.x,
                        mouseDownLocation.y);

                    if (descendant == null) {
                        // Nothing to drag
                        mouseDownLocation = null;
                    } else {
                        mouseDownLocation = descendant.mapPointFromAncestor(this,
                            mouseDownLocation.x, mouseDownLocation.y);

                        while (descendant != null
                            && !descendant.mouseDrag(mouseDownLocation.x, mouseDownLocation.y)) {
                            mouseDownLocation.x += descendant.getX();
                            mouseDownLocation.y += descendant.getY();

                            descendant = descendant.getParent();
                        }

                        dragLocation = new Point(x, y);
                    }
                }
            }
        } else {
            // A drag is currently in progress
            Visual dragRepresentation = Mouse.getDragRepresentation();

            if (dragRepresentation != null) {
                Point dragOffset = Mouse.getDragOffset();

                repaint(dragLocation.x - dragOffset.x, dragLocation.y - dragOffset.y,
                    dragRepresentation.getWidth(), dragRepresentation.getHeight());

                repaint(x - dragOffset.x, y - dragOffset.y,
                    dragRepresentation.getWidth(), dragRepresentation.getHeight());
            }

            dragLocation.x = x;
            dragLocation.y = y;
        }

        return consumed;
    }

    @Override
    protected boolean mouseDown(Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(button, x, y);

        mouseDownLocation = new Point(x, y);

        return consumed;
    }

    @Override
    protected boolean mouseUp(Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(button, x, y);

        if (dragLocation != null) {
            Component descendant = getDescendantAt(x, y);

            while (descendant != null
                && !descendant.mouseDrop(x, y)) {
                descendant = descendant.getParent();
            }

            Visual dragRepresentation = Mouse.getDragRepresentation();

            if (dragRepresentation != null) {
                Point dragOffset = Mouse.getDragOffset();

                repaint(dragLocation.x - dragOffset.x, dragLocation.y - dragOffset.y,
                    dragRepresentation.getWidth(), dragRepresentation.getHeight());
            }
        }

        if (Mouse.isDrag()) {
            Mouse.drop(null);
        }

        mouseDownLocation = null;
        dragLocation = null;

        return consumed;
    }

    @Override
    protected boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(keyCode, keyLocation);

        if (dragLocation != null
            && Mouse.isDrag()
            && keyCode == Keyboard.KeyCode.ESCAPE) {
            Mouse.drop(null);

            Visual dragRepresentation = Mouse.getDragRepresentation();

            if (dragRepresentation != null) {
                Point dragOffset = Mouse.getDragOffset();

                repaint(dragLocation.x - dragOffset.x, dragLocation.y - dragOffset.y,
                    dragRepresentation.getWidth(), dragRepresentation.getHeight());
            }
        }

        mouseDownLocation = null;
        dragLocation = null;

        return consumed;
    }

    @Override
    public void insert(Component component, int index) {
        if (!(component instanceof Window)) {
            throw new IllegalArgumentException("component must be an instance "
               + "of " + Window.class);
        }

        super.insert(component, index);
    }
}
