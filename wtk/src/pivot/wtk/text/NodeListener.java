/*
 * Copyright (c) 2009 VMware, Inc.
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
package pivot.wtk.text;

/**
 * Node listener interface.
 *
 * @author gbrown
 */
public interface NodeListener {
    /**
     * Called when a node's parent has changed, either as a result of being
     * added to or removed from an element.
     *
     * @param node
     * @param previousParent
     */
    public void parentChanged(Node node, Element previousParent);

    /**
     * Called when a node's offset has changed within it's parent element.
     *
     * @param node
     * @param previousOffset
     */
    public void offsetChanged(Node node, int previousOffset);

    /**
     * Called when a range has been added to a node.
     *
     * @param node
     * @param range
     * @param offset
     */
    public void rangeInserted(Node node, Node range, int offset);

    /**
     * Called when a range has been removed from a node.
     * @param node
     * @param offset
     * @param range
     */
    public void rangeRemoved(Node node, int offset, Node range);
}
