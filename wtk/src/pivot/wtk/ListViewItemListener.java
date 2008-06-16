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

public interface ListViewItemListener {
    /**
     * Called when an item has been inserted into the list view.
     *
     * @param listView
     * The source of the event.
     *
     * @param index
     * The index of the item that was inserted.
     */
    public void itemInserted(ListView listView, int index);

    /**
     * Called when items have been removed from the list view.
     *
     * @param listView
     * The source of the event.
     *
     * @param index
     * The first index affected by the event.
     *
     * @param count
     * The number of items that were removed, or <tt>-1</tt> if all items
     * were removed.
     */
    public void itemsRemoved(ListView listView, int index, int count);

    /**
     * Called when an item in the list view has been updated.
     *
     * @param listView
     * The source of the event.
     *
     * @param index
     * The first index affected by the event.
     */
    public void itemUpdated(ListView listView, int index);

    /**
     * Called when the items in a list have been sorted.
     *
     * @param listView
     * The source of the event.
     */
    public void itemsSorted(ListView listView);
}
