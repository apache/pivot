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

    private Point dragLocation = null;
    private DragSource dragSource = null;

    public static final int DRAG_THRESHOLD = 4;

    protected Display(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        super.setSkin(new DisplaySkin());
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    protected DragSource getActiveDragSource() {
        return dragSource;
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

        if (dragSource != null) {
            Visual representation = dragSource.getRepresentation();

            if (representation != null) {
                Point offset = dragSource.getOffset();
                int tx = dragLocation.x - offset.x;
                int ty = dragLocation.y - offset.y;

                Graphics2D representationGraphics = (Graphics2D)graphics.create(tx, ty,
                    representation.getWidth(), representation.getHeight());

                representation.paint(representationGraphics);

                representationGraphics.dispose();
            }
        }
    }

    @Override
    protected boolean mouseMove(int x, int y) {
        boolean consumed = super.mouseMove(x, y);

        if (!consumed) {
            if (dragSource == null) {
                if (dragLocation != null) {
                    if (Math.abs(x - dragLocation.x) > DRAG_THRESHOLD
                        || Math.abs(y - dragLocation.y) > DRAG_THRESHOLD) {
                        Component descendant = getDescendantAt(dragLocation.x,
                            dragLocation.y);

                        // Look for a drag source
                        while (descendant != null) {
                            dragSource = descendant.getDragSource();

                            if (dragSource == null) {
                                descendant = descendant.getParent();
                            } else {
                                break;
                            }
                        }

                        if (dragSource == null) {
                            // A drag source could not be found, so stop looking
                            // until the next mouse down event
                            dragLocation = null;
                        } else {
                            // A drag handler was found; begin the drag
                            Point componentDragLocation = descendant.mapPointFromAncestor(this,
                                dragLocation.x, dragLocation.y);

                            if (dragSource.beginDrag(descendant,
                                componentDragLocation.x, componentDragLocation.y)) {
                                if (dragSource.isNative()) {
                                    // Start a native drag
                                    applicationContext.startDrag(dragSource);
                                    dragLocation = null;
                                    dragSource = null;
                                } else {
                                    // Start a local drag
                                    Mouse.setCursor(Cursor.DEFAULT);

                                    Object dragContent = dragSource.getContent();
                                    Mouse.setDragContentType(dragContent.getClass());
                                    Mouse.setSupportedDropActions(dragSource.getSupportedDropActions());
                                }
                            } else {
                                // The drag source rejected the drag
                                dragLocation = null;
                                dragSource = null;
                            }
                        }
                    }
                }
            } else {
                // A drag is currently in progress
                Visual representation = dragSource.getRepresentation();

                if (representation != null) {
                    Point offset = dragSource.getOffset();

                    repaint(dragLocation.x - offset.x,
                        dragLocation.y - offset.y,
                        representation.getWidth(), representation.getHeight());

                    repaint(x - offset.x, y - offset.y,
                        representation.getWidth(), representation.getHeight());
                }

                if (dragLocation != null) {
                    dragLocation.x = x;
                    dragLocation.y = y;
                }
            }
        }

        return consumed;
    }

    @Override
    protected boolean mouseDown(Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(button, x, y);

        if (!consumed) {
            dragLocation = new Point(x, y);
        }

        return consumed;
    }

    @Override
    protected boolean mouseUp(Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(button, x, y);

        if (!consumed) {
            if (dragSource != null) {
                Component descendant = getDescendantAt(x, y);

                // Look for a drop target
                DropTarget dropTarget = null;
                while (descendant != null) {
                    dropTarget = descendant.getDropTarget();

                    if (dropTarget == null) {
                        descendant = descendant.getParent();
                    } else {
                        break;
                    }
                }

                DropAction dropAction = null;

                if (dropTarget != null) {
                    // A drop target was found
                    Object dragContent = dragSource.getContent();
                    int supportedDropActions = dragSource.getSupportedDropActions();
                    Point dropLocation = descendant.mapPointFromAncestor(this, x, y);

                    dropAction = dropTarget.getDropAction(descendant, dragContent.getClass(),
                        supportedDropActions, dropLocation.x, dropLocation.y);

                    if (dropAction != null
                        && dropAction.isSelected(supportedDropActions)) {
                        // Drop the content
                        dropTarget.drop(descendant, dragContent, dropLocation.x, dropLocation.y);
                    }
                }

                Visual representation = dragSource.getRepresentation();

                if (representation != null) {
                    Point offset = dragSource.getOffset();
                    repaint(dragLocation.x - offset.x, dragLocation.y - offset.y,
                        representation.getWidth(), representation.getHeight());
                }

                // End the drag
                dragSource.endDrag(dropAction);
                dragSource = null;

                dragLocation = null;

                Mouse.setDragContentType(null);
                Mouse.setSupportedDropActions(0);

                Mouse.setCursor(descendant == null ? Cursor.DEFAULT : descendant.getCursor());
            }
        }

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
