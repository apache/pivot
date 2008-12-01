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
 * Interface representing the destination of a drag/drop operation.
 *
 * @author gbrown
 */
public interface DropTarget {
    /**
     * Called to obtain the drop action the target would use if an item were
     * to be dropped at a given location.
     *
     * @param component
     * @param contentType
     * @param x
     * @param y
     *
     * @return
     * The drop action to be used, or <tt>null</tt> for no drop action.
     */
    public DropAction getDropAction(Component component, Class<?> contentType, int x, int y);

    /**
     * Called when an item is dropped during a drag/drop operation.
     *
     * @param component
     * @param content
     * @param x
     * @param y
     */
    public void drop(Component component, Object content, int x, int y);
}
