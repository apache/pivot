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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;

/**
 * Provides framework support for drag/drop operations.
 * <p>
 * TODO If Escape is pressed, cancel drag (call endDrag(null)).
 * <p>
 * TODO Paint an appropriate overlay icon if drop action is copy or link.
 *
 * @author gbrown
 */
public final class DragDropManager {
    /**
     * Handles display mouse events.
     *
     * @author gbrown
     */
    private class DisplayMouseHandler
        implements ComponentMouseListener, ComponentMouseButtonListener {
        public boolean mouseMove(Component component, int x, int y) {
            if (isActive()) {
                // A drag is currently in progress
                Visual representation = getRepresentation();
                if (representation != null) {
                    Dimensions offset = getOffset();

                    display.repaint(dragLocation.x - offset.width,
                        dragLocation.y - offset.height,
                        representation.getWidth(), representation.getHeight());

                    display.repaint(x - offset.width, y - offset.height,
                        representation.getWidth(), representation.getHeight());
                }

                if (dragLocation != null) {
                    dragLocation.x = x;
                    dragLocation.y = y;
                }
            } else {
                if (dragLocation != null) {
                    if (Math.abs(x - dragLocation.x) > DRAG_THRESHOLD
                        || Math.abs(y - dragLocation.y) > DRAG_THRESHOLD) {
                        Component dragSource = display.getDescendantAt(dragLocation.x,
                            dragLocation.y);

                        // Look for a drag handler
                        dragHandler = null;
                        while (dragSource != null) {
                            dragHandler = dragSource.getDragSource();

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

            return false;
        }

        public void mouseOver(Component component) {
        }

        public void mouseOut(Component component) {
        }

        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            dragLocation = new Point(x, y);
            return false;
        }

        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            if (isActive()) {
                Component dropTarget = display.getDescendantAt(x, y);

                // Look for a drop handler
                DropTarget dropHandler = null;
                while (dropTarget != null) {
                    dropHandler = dropTarget.getDropTarget();

                    if (dropHandler == null) {
                        dropTarget = dropTarget.getParent();
                    } else {
                        break;
                    }
                }

                DropAction dropAction = null;

                if (dropHandler != null) {
                    // A drop handler was found
                    Point dropLocation = dropTarget.mapPointFromAncestor(display, x, y);
                    dropAction = dropHandler.getDropAction(dropTarget, getContentType(),
                        dropLocation.x, dropLocation.y);

                    if (dropAction != null) {
                        // Drop the content
                        dropHandler.drop(dropTarget, getContent(), dropLocation.x, dropLocation.y);
                    }
                }

                Visual representation = getRepresentation();
                if (representation != null) {
                    Dimensions offset = getOffset();
                    display.repaint(dragLocation.x - offset.width,
                        dragLocation.y - offset.height,
                        representation.getWidth(), representation.getHeight());
                }

                // End the drag
                dragHandler.endDrag(dropAction);
                dragHandler = null;

                Mouse.setCursor(dropTarget == null ? Cursor.DEFAULT : dropTarget.getCursor());
            }

            dragLocation = null;

            return false;
        }

        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            return false;
        }
    }

    /**
     * Paints drag representations on display.
     *
     * @author gbrown
     */
    private class DragOverlayDecorator implements Decorator {
        private Graphics2D graphics = null;

        public Graphics2D prepare(Component component, Graphics2D graphics) {
            this.graphics = graphics;
            return graphics;
        }

        public void update() {
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

        public Bounds getAffectedArea(Component component, int x, int y, int width, int height) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Drag handler wrapper around native AWT drag source.
     *
     * @author gbrown
     */
    private class NativeDragHandler implements DragSource {
        public boolean beginDrag(Component component, int x, int y) {
            throw new UnsupportedOperationException();
        }

        public void endDrag(DropAction dropAction) {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings("unchecked")
        public Object getContent() {
            Object content = null;

            Transferable transferable = dropTargetDragEvent.getTransferable();
            DataFlavor[] dataFlavors = transferable.getTransferDataFlavors();

            if (dataFlavors.length > 0) {
                try {
                    content = transferable.getTransferData(dataFlavors[0]);
                } catch (UnsupportedFlavorException exception) {
                } catch (IOException exception) {
                }
            }

            return content;
        }

        public Class<?> getContentType() {
            Class<?> contentType = null;

            Transferable transferable = dropTargetDragEvent.getTransferable();
            DataFlavor[] dataFlavors = transferable.getTransferDataFlavors();

            if (dataFlavors.length > 0) {
                contentType = dataFlavors[0].getRepresentationClass();
            }

            System.out.println(contentType);
            return contentType;
        }

        public Visual getRepresentation() {
            return null;
        }

        public Dimensions getOffset() {
            throw new UnsupportedOperationException();
        }

        public int getSupportedDropActions() {
            int supportedDropActions = 0;

            int awtDropAction = dropTargetDragEvent.getDropAction();

            if ((awtDropAction & DnDConstants.ACTION_COPY) == DnDConstants.ACTION_COPY) {
                supportedDropActions |= DropAction.COPY.getMask();
            }

            if ((awtDropAction & DnDConstants.ACTION_MOVE) == DnDConstants.ACTION_MOVE) {
                supportedDropActions |= DropAction.MOVE.getMask();
            }

            if ((awtDropAction & DnDConstants.ACTION_LINK) == DnDConstants.ACTION_LINK) {
                supportedDropActions |= DropAction.LINK.getMask();
            }

            return supportedDropActions;
        }
    }

    /**
     * Implementation of native AWT drop target listener.
     *
     * @author gbrown
     */
    private class DropTargetHandler implements DropTargetListener {
        public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
            dragHandler = new NativeDragHandler();
            DragDropManager.this.dropTargetDragEvent = dropTargetDragEvent;
        }

        public void dragExit(DropTargetEvent dropTargetEvent) {
            dragHandler = null;
        }

        public void dragOver(DropTargetDragEvent dropTargetDragEvent) {
            // No-op
        }

        public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent) {
            DragDropManager.this.dropTargetDragEvent = dropTargetDragEvent;
        }

        public void drop(DropTargetDropEvent dropTargetDropEvent) {
            // Look for a drop handler
            int x = Mouse.getX();
            int y = Mouse.getY();

            Component dropTarget = display.getDescendantAt(x, y);

            DropTarget dropHandler = null;
            while (dropTarget != null) {
                dropHandler = dropTarget.getDropTarget();

                if (dropHandler == null) {
                    dropTarget = dropTarget.getParent();
                } else {
                    break;
                }
            }

            if (dropHandler != null) {
                // A drop handler was found
                DropAction dropAction = null;

                Point dropLocation = dropTarget.mapPointFromAncestor(display, x, y);
                dropAction = dropHandler.getDropAction(dropTarget, getContentType(),
                    dropLocation.x, dropLocation.y);

                if (dropAction != null) {
                    int awtDropAction = 0;

                    switch(dropAction) {
                        case COPY: {
                            awtDropAction = DnDConstants.ACTION_COPY;
                            break;
                        }

                        case MOVE: {
                            awtDropAction = DnDConstants.ACTION_MOVE;
                            break;
                        }

                        case LINK: {
                            awtDropAction = DnDConstants.ACTION_LINK;
                            break;
                        }
                    }

                    // Drop the content
                    dropTargetDropEvent.acceptDrop(awtDropAction);
                    dropHandler.drop(dropTarget, getContent(), dropLocation.x, dropLocation.y);

                    dropTargetDropEvent.dropComplete(true);
                }
            } else {
                dropTargetDropEvent.rejectDrop();
            }

            // Clear the drag handler
            dragHandler = null;
        }
    }

    private Display display;

    private Point dragLocation = null;
    private DragSource dragHandler = null;

    private DropTargetDragEvent dropTargetDragEvent = null;

    public static final int DRAG_THRESHOLD = 4;

    protected DragDropManager(Display display) {
        this.display = display;

        DisplayMouseHandler displayMouseHandler = new DisplayMouseHandler();
        display.getComponentMouseListeners().add(displayMouseHandler);
        display.getComponentMouseButtonListeners().add(displayMouseHandler);

        display.getDecorators().add(new DragOverlayDecorator());

        ApplicationContext applicationContext = display.getApplicationContext();
        java.awt.Container displayHost = applicationContext.getDisplayHost();
        new java.awt.dnd.DropTarget(displayHost, new DropTargetHandler());
    }

    public boolean isActive() {
        return (dragHandler != null);
    }

    public Object getContent() {
        return (dragHandler == null) ? null : dragHandler.getContent();
    }

    public Class<?> getContentType() {
        return (dragHandler == null) ? null : dragHandler.getContentType();
    }

    public Visual getRepresentation() {
        return (dragHandler == null) ? null : dragHandler.getRepresentation();
    }

    public Dimensions getOffset() {
        return (dragHandler == null) ? null : dragHandler.getOffset();
    }

    public int getSupportedDropActions() {
        return (dragHandler == null) ? null : dragHandler.getSupportedDropActions();
    }
}
