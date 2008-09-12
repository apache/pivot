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
 * <p>Menu popup listener interface.</p>
 *
 * @author gbrown
 */
public interface MenuPopupListener {
    /**
     * Called when a menu popup's menu data has changed.
     *
     * @param menuPopup
     * The source of the event.
     *
     * @param previousMenuData
     * The previous menu data.
     */
    public void menuDataChanged(MenuPopup menuPopup, Menu.ItemGroup previousMenuData);
}
