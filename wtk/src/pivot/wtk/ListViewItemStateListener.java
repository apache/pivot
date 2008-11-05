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

import pivot.util.Vote;

/**
 * List view item state listener interface.
 *
 * @author gbrown
 * @author tvolkert
 */
public interface ListViewItemStateListener {
    /**
     * Called to preview an item disabled state change.
     *
     * @param listView
     * @param index
     */
    public Vote previewItemDisabledChange(ListView listView, int index);

    /**
     * Called when an item disabled change event has been canceled.
     *
     * @param listView
     * @param index
     * @param reason
     */
    public void itemDisabledChangeVetoed(ListView listView, int index, Vote reason);

    /**
     * Called when an item's disabled state has changed.
     *
     * @param listView
     * @param index
     */
    public void itemDisabledChanged(ListView listView, int index);

    /**
     * Called to preview an item checked state change.
     *
     * @param listView
     * @param index
     */
    public Vote previewItemCheckedChange(ListView listView, int index);

    /**
     * Called when an item checked change event has been canceled.
     *
     * @param listView
     * @param index
     * @param reason
     */
    public void itemCheckedChangeVetoed(ListView listView, int index, Vote reason);

    /**
     * Called when an item's disabled state has changed.
     *
     * @param listView
     * @param index
     */
    public void itemCheckedChanged(ListView listView, int index);
}
