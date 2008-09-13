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
package pivot.wtk.skin.terra;

import pivot.wtk.Component;
import pivot.wtk.MenuPopup;
import pivot.wtk.skin.WindowSkin;

/**
 * <p>Menu popup skin.</p>
 *
 * <p>TODO Complete this class.</p>
 *
 * <p>TODO Implement skin methods.</p>
 *
 * <p>TODO Create a panorama and add the component's Menu to it.</p>
 *
 * @author gbrown
 */
public class MenuPopupSkin extends WindowSkin  {
    public void install(Component component) {
        validateComponentType(component, MenuPopup.class);

        super.install(component);
    }
}
