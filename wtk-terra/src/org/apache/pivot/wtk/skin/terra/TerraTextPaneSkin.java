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
import org.apache.pivot.wtk.skin.TextPaneSkin;

/**
 * Terra text area skin. This part of the skin deals with color
 * selection that is specific to the theme.
 */
public class TerraTextPaneSkin extends TextPaneSkin {
    public TerraTextPaneSkin() {
        setColor(1);
        setBackgroundColor(11);
        setInactiveColor(7);
        setSelectionColor(4);
        setSelectionBackgroundColor(14);
        setInactiveSelectionColor(1);
        setInactiveSelectionBackgroundColor(9);
    }

    public final void setColor(int color) {
        Theme theme = currentTheme();
        setColor(theme.getColor(color));
    }

    public final void setInactiveColor(int inactiveColor) {
        Theme theme = currentTheme();
        setInactiveColor(theme.getColor(inactiveColor));
    }

    public final void setSelectionColor(int selectionColor) {
        Theme theme = currentTheme();
        setSelectionColor(theme.getColor(selectionColor));
    }

    public final void setSelectionBackgroundColor(int selectionBackgroundColor) {
        Theme theme = currentTheme();
        setSelectionBackgroundColor(theme.getColor(selectionBackgroundColor));
    }

    public final void setInactiveSelectionColor(int inactiveSelectionColor) {
        Theme theme = currentTheme();
        setInactiveSelectionColor(theme.getColor(inactiveSelectionColor));
    }

    public final void setInactiveSelectionBackgroundColor(int inactiveSelectionBackgroundColor) {
        Theme theme = currentTheme();
        setInactiveSelectionBackgroundColor(theme.getColor(inactiveSelectionBackgroundColor));
    }
}
