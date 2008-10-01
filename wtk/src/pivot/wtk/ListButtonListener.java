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

import pivot.collections.List;

/**
 * List button listener list interface.
 *
 * @author gbrown
 */
public interface ListButtonListener {
    /**
     * Called when a list button's list data has changed.
     *
     * @param listButton
     * @param previousListData
     */
    public void listDataChanged(ListButton listButton, List<?> previousListData);

    /**
     * Called when a list button's item renderer has changed.
     *
     * @param listButton
     * @param previousItemRenderer
     */
    public void itemRendererChanged(ListButton listButton, ListView.ItemRenderer previousItemRenderer);

    /**
     * Called when a list button's selected value key has changed.
     *
     * @param listButton
     * @param previousSelectedValueKey
     */
    public void selectedValueKeyChanged(ListButton listButton, String previousSelectedValueKey);
}
