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
package org.apache.pivot.wtk.skin.terra;

import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.TextAreaSkin;

/**
 * Terra text area skin.
 */
public class TerraTextAreaSkin extends TextAreaSkin {
    public TerraTextAreaSkin() {
        setColor(1);
        setInactiveColor(7);
        setSelectionColor(4);
        setSelectionBackgroundColor(14);
        setInactiveSelectionColor(1);
        setInactiveSelectionBackgroundColor(9);
    }

    public final void setColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setColor(theme.getColor(color));
    }

    public final void setInactiveColor(int inactiveColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setInactiveColor(theme.getColor(inactiveColor));
    }

    public final void setBackgroundColor(int backgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(backgroundColor));
    }

    public final void setSelectionColor(int backgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setSelectionColor(theme.getColor(backgroundColor));
    }

    public final void setSelectionBackgroundColor(int backgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setSelectionBackgroundColor(theme.getColor(backgroundColor));
    }

    public final void setInactiveSelectionColor(int backgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setInactiveSelectionColor(theme.getColor(backgroundColor));
    }

    public final void setInactiveSelectionBackgroundColor(int backgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setInactiveSelectionBackgroundColor(theme.getColor(backgroundColor));
    }
}
