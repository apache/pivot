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
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import pivot.collections.adapter.ListAdapter;

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
    public class NativeDragHandler implements DragHandler {
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
            try {
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    content = transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    content = new ListAdapter<File>((java.util.List<File>)content);
                } else {
                    DataFlavor[] dataFlavors = transferable.getTransferDataFlavors();
                    if (dataFlavors.length > 0) {
                        content = transferable.getTransferData(dataFlavors[0]);
                    }
                }
            } catch (UnsupportedFlavorException exception) {
            } catch (IOException exception) {
            }

            return content;
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

            DropHandler dropHandler = null;
            while (dropTarget != null) {
                dropHandler = dropTarget.getDropHandler();

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
                dropAction = dropHandler.getDropAction(dropTarget,
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
                    dropHandler.drop(dropTarget, dropLocation.x, dropLocation.y);

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
    private DragHandler dragHandler = null;

    private DropTargetDragEvent dropTargetDragEvent = null;

    public static final int DRAG_THRESHOLD = 4;

    public DragDropManager(Display display) {
        this.display = display;

        ApplicationContext applicationContext = display.getApplicationContext();
        java.awt.Container displayHost = applicationContext.getDisplayHost();
        new DropTarget(displayHost, new DropTargetHandler());
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

    protected void mouseOver() {
    }

    protected void mouseOut() {
    }

    protected void mouseMove(int x, int y) {
        if (isActive()) {
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
                // A drop handler was found
                Point dropLocation = dropTarget.mapPointFromAncestor(display, x, y);
                dropAction = dropHandler.getDropAction(dropTarget,
                    dropLocation.x, dropLocation.y);

                if (dropAction != null) {
                    // Drop the content
                    dropHandler.drop(dropTarget, dropLocation.x, dropLocation.y);
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
    }

    protected void keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
    }

    protected void keyReleased(int keyCode, Keyboard.KeyLocation keyLocation) {
    }
}
