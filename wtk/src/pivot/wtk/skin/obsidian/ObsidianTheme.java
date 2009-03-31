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
package pivot.wtk.skin.obsidian;

import java.awt.Font;

import pivot.wtk.MessageType;
import pivot.wtk.PushButton;
import pivot.wtk.RadioButton;
import pivot.wtk.Theme;
import pivot.wtk.media.Image;

/**
 * Obsidian theme.
 *
 * @author gbrown
 */
public final class ObsidianTheme extends Theme {
    private Font font = new Font("Verdana", Font.PLAIN, 11);

    public ObsidianTheme() {
        componentSkinMap.put(PushButton.class, ObsidianPushButtonSkin.class);
        componentSkinMap.put(RadioButton.class, ObsidianRadioButtonSkin.class);
    }

    protected void install() {
    }

    protected void uninstall() {
    }

    public Font getFont() {
        return font;
    }

    public Image getMessageIcon(MessageType messageType) {
    	return null;
    }

    public Image getSmallMessageIcon(MessageType messageType) {
    	return null;
    }
}
