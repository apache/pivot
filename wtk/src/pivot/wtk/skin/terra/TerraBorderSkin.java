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

import pivot.wtk.Theme;
import pivot.wtk.skin.BorderSkin;

/**
 * Terra border skin.
 *
 * @author gbrown
 */
public class TerraBorderSkin extends BorderSkin {
    public TerraBorderSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(1));
        setColor(theme.getColor(2));
        setTitleColor(theme.getColor(7));
    }

    public void setColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setColor(theme.getColor(color));
    }

    public void setTitleColor(int titleColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setTitleColor(theme.getColor(titleColor));
    }

    public void setBackgroundColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(color));
    }
}
