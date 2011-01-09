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
package org.apache.pivot.ui;

import java.util.HashMap;

import org.apache.pivot.scene.Font;
import org.apache.pivot.util.Service;

/**
 * Base class for Pivot themes. A theme defines a complete "look and feel"
 * for a Pivot application.
 * <p>
 * Note that concrete Theme implementations should be declared as final. If
 * multiple third-party libraries attempted to extend a theme, it would cause a
 * conflict, as only one could be used in any given application.
 * <p>
 * IMPORTANT All skin mappings must be added to the map, even non-static inner
 * classes. Otherwise, the component's base class will attempt to install its
 * own skin, which will result in the addition of duplicate listeners.
 */
public abstract class Theme {
    protected HashMap<Class<? extends Component>, Class<? extends Skin>> componentSkinMap =
        new HashMap<Class<? extends Component>, Class<? extends Skin>>();

    private static Theme theme = null;

    /**
     * The service provider name (see {@link Service#getProvider(String)}).
     */
    public static final String PROVIDER_NAME = "org.apache.pivot.wtk.Theme";

    static {
        theme = (Theme)Service.getProvider(PROVIDER_NAME);

        if (theme == null) {
            throw new ThemeNotFoundException();
        }
    }

    public Theme() {
        // TODO Create non-themed mappings
    }

    public final Class<? extends Skin> getSkinClass(Class<? extends Component> componentClass) {
        return componentSkinMap.get(componentClass);
    }

    public abstract Font getFont();
    public abstract void setFont(Font font);

    /**
     * Returns the skin class responsible for skinning the specified component
     * class.
     *
     * @param componentClass
     * The component class.
     *
     * @return
     * The skin class, or <tt>null</tt> if no skin mapping exists for the
     * component class.
     */
    public Class<? extends Skin> get(Class<? extends Component> componentClass) {
        if (componentClass == null) {
            throw new IllegalArgumentException("Component class is null.");
        }

        return componentSkinMap.get(componentClass);
    }

    /**
     * Sets the skin class responsible for skinning the specified component
     * class.
     *
     * @param componentClass
     * The component class.
     *
     * @param skinClass
     * The skin class.
     */
    public void set(Class<? extends Component> componentClass, Class<? extends Skin> skinClass) {
        if (componentClass == null) {
            throw new IllegalArgumentException("Component class is null.");
        }

        if (skinClass == null) {
            throw new IllegalArgumentException("Skin class is null.");
        }

        componentSkinMap.put(componentClass, skinClass);
    }

    /**
     * Gets the current theme, as determined by the {@linkplain #PROVIDER_NAME
     * theme provider}.
     *
     * @throws IllegalStateException
     * If a theme has not been installed.
     */
    public static Theme getTheme() {
        if (theme == null) {
            throw new IllegalStateException("No installed theme.");
        }

        return theme;
    }
}
