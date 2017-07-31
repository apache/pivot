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
import org.apache.pivot.wtk.skin.BorderSkin;

/**
 * Terra Border skin. Deals with colors that depend on
 * the current theme.
 */
public class TerraBorderSkin extends BorderSkin {
    public TerraBorderSkin() {
        setBackgroundColor(4);
        setColor(7);
        setTitleColor(12);
    }

    public final void setColor(int color) {
        Theme theme = currentTheme();
        setColor(theme.getColor(color));
    }

    public final void setTitleColor(int titleColor) {
        Theme theme = currentTheme();
        setTitleColor(theme.getColor(titleColor));
    }

}
