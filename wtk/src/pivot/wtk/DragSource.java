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

/**
 * Interface representing the source of a drag/drop operation.
 *
 * @author gbrown
 */
public interface DragSource {
    /**
     * Called when a drag operation is initiated.
     *
     * @param component
     * @param x
     * @param y
     *
     * @return
     * <tt>true</tt> to begin the drag, or <tt>false</tt> to reject the
     * operation.
     */
    public boolean beginDrag(Component component, int x, int y);

    /**
     * Called when a drag operation completes.
     *
     * @param dropAction
     */
    public void endDrag(DropAction dropAction);

    /**
     * Returns the drag content.
     */
    public Object getContent();

    /**
     * Returns a visual representation of the drag content.
     *
     * @return
     * A visual that represents the drag content, or <tt>null</tt> for no
     * visual.
     */
    public Visual getRepresentation();

    /**
     * Returns the offset from the mouse pointer location at which the
     * representation should be drawn.
     *
     * @return
     * The offset of the mouse pointer within the representation visual; may
     * be <tt>null</tt> if the content has no visual representation.
     */
    public Dimensions getOffset();

    /**
     * Returns the drop actions supported by this drag source.
     */
    public int getSupportedDropActions();
}
