/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.wtk.skin.obsidian;

import java.awt.Font;

import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.media.Image;


/**
 * Obsidian theme.
 */
public final class ObsidianTheme extends Theme {
    private Font font = new Font("Verdana", Font.PLAIN, 11);

    public ObsidianTheme() {
        componentSkinMap.put(PushButton.class, ObsidianPushButtonSkin.class);
        componentSkinMap.put(RadioButton.class, ObsidianRadioButtonSkin.class);
    }

    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public Image getMessageIcon(MessageType messageType) {
        return null;
    }

    @Override
    public Image getSmallMessageIcon(MessageType messageType) {
        return null;
    }
}
