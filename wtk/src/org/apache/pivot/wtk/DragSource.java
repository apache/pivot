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
package org.apache.pivot.wtk;

/**
 * Interface representing a drag source.
 */
public interface DragSource {
    /**
     * Called by the framework to initiate a drag operation.
     *
     * @param component
     * @param x
     * @param y
     *
     * @return
     * <tt>true</tt> to accept the drag; <tt>false</tt> to reject it.
     */
    public boolean beginDrag(Component component, int x, int y);

    /**
     * Called by the framework to terminate a drag operation.
     *
     * @param component
     * @param dropAction
     */
    public void endDrag(Component component, DropAction dropAction);

    /**
     * Returns the drag source's native flag.
     *
     * @return
     * If <tt>true</tt>, the drag will be executed via the native OS. Otherwise,
     * it will be executed locally.
     */
    public boolean isNative();

    /**
     * Returns the drag content.
     */
    public LocalManifest getContent();

    /**
     * Returns a visual representing the drag content.
     *
     * @return
     * The drag visual, or <tt>null</tt> for no visual.
     */
    public Visual getRepresentation();

    /**
     * Returns the offset of the mouse pointer within the drag visual. Not
     * required unless a representation is specified.
     *
     * @return
     * The mouse offset within the drag visual, or <tt>null</tt> if no visual
     * is specified.
     */
    public Point getOffset();

    /**
     * Returns the drop actions supported by this drag source.
     *
     * @return
     * A bitfield of the supported drop actions.
     */
    public int getSupportedDropActions();
}
