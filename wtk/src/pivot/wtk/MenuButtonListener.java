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
 * <p>Menu button listener interface.</p>
 *
 * @author gbrown
 */
public interface MenuButtonListener {
    /**
     * Called when a menu button's menu data has changed.
     *
     * @param menuButton
     * @param previousMenuData
     */
    public void menuDataChanged(MenuButton menuButton, Menu.ItemGroup previousMenuData);

    /**
     * Called when a menu button's repeatable flag has changed.
     *
     * @param menuButton
     */
    public void repeatableChanged(MenuButton menuButton);
}
