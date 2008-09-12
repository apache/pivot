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

/**
 * <p>Provides framework support for drag/drop operations.</p>
 *
 * <p>TODO If Escape is pressed, cancel drag (call endDrag(null))</p>
 *
 * <p>TODO Paint an appropriate overlay icon if drop action is copy or link</p>
 *
 * <p>TODO ApplicationContext.DisplayHost should marshall/unmarshall native
 * drag/drop content when a drag out/over occurs, respectively?</p>
 *
 * @author gbrown
 */
public final class DragDropManager {
    private ApplicationContext applicationContext = null;

    private Point dragLocation = null;
    private DragHandler dragHandler = null;

    public static final int DRAG_THRESHOLD = 4;

    protected DragDropManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public boolean isActive() {
        return (dragHandler != null);
    }

    public Object getContent() {
        if (!isActive()) {
            throw new IllegalStateException();
        }

        return dragHandler.getContent();
    }

    public Visual getRepresentation() {
        if (!isActive()) {
            throw new IllegalStateException();
        }

        return dragHandler.getRepresentation();
    }

    public Dimensions getOffset() {
        if (!isActive()) {
            throw new IllegalStateException();
        }

        return dragHandler.getOffset();
    }

    public int getSupportedDropActions() {
        if (!isActive()) {
            throw new IllegalStateException();
        }

        return dragHandler.getSupportedDropActions();
    }

    public static DragDropManager getCurrent() {
        if (ApplicationContext.active == null) {
            throw new IllegalStateException("No current application context.");
        }

        return ApplicationContext.active.getDragDropManager();
    }

    public void paint(Graphics2D graphics) {
        if (isActive()) {
            Visual representation = getRepresentation();

            if (representation != null) {
                Dimensions offset = getOffset();
                int tx = dragLocation.x - offset.width;
                int ty = dragLocation.y - offset.height;

                Graphics2D representationGraphics = (Graphics2D)graphics.create(tx, ty,
                    representation.getWidth(), representation.getHeight());

                representation.paint(representationGraphics);

                representationGraphics.dispose();
            }
        }
    }

    protected void mouseMove(int x, int y) {
        if (isActive()) {
            invalidate(x, y);
        } else {
            if (dragLocation != null) {
                if (Math.abs(x - dragLocation.x) > DRAG_THRESHOLD
                    || Math.abs(y - dragLocation.y) > DRAG_THRESHOLD) {
                    Display display = applicationContext.getDisplay();
                    Component dragSource = display.getDescendantAt(dragLocation.x,
                        dragLocation.y);

                    // Look for a drag handler
                    dragHandler = null;
                    while (dragSource != null) {
                        dragHandler = dragSource.getDragHandler();

                        if (dragHandler == null) {
                            dragSource = dragSource.getParent();
                        } else {
                            break;
                        }
                    }

                    if (dragHandler == null) {
                        // A drag handler could not be found, so stop looking
                        // until the next mouse down event
                        dragLocation = null;
                    } else {
                        // A drag handler was found; begin the drag
                        Mouse.setCursor(Cursor.DEFAULT);
                        Point componentDragLocation = dragSource.mapPointFromAncestor(display,
                            dragLocation.x, dragLocation.y);

                        boolean drag = dragHandler.beginDrag(dragSource,
                            componentDragLocation.x, componentDragLocation.y);

                        if (!drag) {
                            // The drag source rejected the drag
                            dragLocation = null;
                            dragHandler = null;
                        }
                    }
                }
            }
        }
    }

    protected void mouseDown(Mouse.Button button, int x, int y) {
        dragLocation = new Point(x, y);
    }

    protected void mouseUp(Mouse.Button button, int x, int y) {
        if (isActive()) {
            Display display = applicationContext.getDisplay();
            Component dropTarget = display.getDescendantAt(x, y);

            // Look for a drop handler
            DropHandler dropHandler = null;
            while (dropTarget != null) {
                dropHandler = dropTarget.getDropHandler();

                if (dropHandler == null) {
                    dropTarget = dropTarget.getParent();
                } else {
                    break;
                }
            }

            DropAction dropAction = null;

            if (dropHandler != null) {
                // A drop handler was found; drop the content
                Point componentDropLocation = dropTarget.mapPointFromAncestor(display, x, y);
                dropAction = dropHandler.drop(dropTarget,
                    componentDropLocation.x, componentDropLocation.y);
            }

            invalidate(x, y);

            // End the drag
            dragHandler.endDrag(dropAction);
            dragHandler = null;

            Mouse.setCursor(dropTarget == null ? Cursor.DEFAULT : dropTarget.getCursor());
        }

        dragLocation = null;
    }

    protected void keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
    }

    protected void keyReleased(int keyCode, Keyboard.KeyLocation keyLocation) {
    }

    private void invalidate(int x, int y) {
        Visual representation = getRepresentation();
        Dimensions offset = getOffset();

        applicationContext.repaint(dragLocation.x - offset.width,
            dragLocation.y - offset.height,
            representation.getWidth(), representation.getHeight());

        dragLocation.x = x;
        dragLocation.y = y;

        applicationContext.repaint(dragLocation.x - offset.width,
            dragLocation.y - offset.height,
            representation.getWidth(), representation.getHeight());
    }
}
