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
package pivot.wtk.content;

import pivot.wtk.TablePane;
import pivot.wtk.Menu;

/**
 * TODO This renderer should query the skin for "gutter" style so it knows
 * where to paint the icon.
 *
 * TODO Define get/setPaintIconInGutter() methods.
 *
 * @author gbrown
 *
 */
public class MenuItemDataRenderer extends TablePane implements Menu.ItemDataRenderer {
    public void render(Object item, Menu menu, boolean checked,
        boolean disabled, boolean highlighted) {
        // TODO
    }

    public PropertyDictionary getProperties() {
        // TODO
        return null;
    }
}
