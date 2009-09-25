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
package org.apache.pivot.wtk.content;

import java.net.URL;

import org.apache.pivot.util.ThreadUtilities;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.media.Image;


/**
 * Default button data implementation.
 */
public class ButtonData {
    private Image icon;
    private String text;

    public ButtonData() {
        this(null, null);
    }

    public ButtonData(Image icon) {
        this(icon, null);
    }

    public ButtonData(String text) {
        this(null, text);
    }

    public ButtonData(Image icon, String text) {
        this.icon = icon;
        this.text = text;
    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    /**
     * Sets the button data's icon by URL.
     * <p>
     * <b>Note</b>: Using this signature will cause an entry to be added in the
     * application context's {@linkplain ApplicationContext#getResourceCache()
     * resource cache} if one does not already exist.
     *
     * @param iconURL
     * The location of the icon to set.
     */
    public void setIcon(URL iconURL) {
        Image icon = (Image)ApplicationContext.getResourceCache().get(iconURL);

        if (icon == null) {
            try {
                icon = Image.load(iconURL);
            } catch (TaskExecutionException exception) {
                throw new IllegalArgumentException(exception);
            }

            ApplicationContext.getResourceCache().put(iconURL, icon);
        }

        setIcon(icon);
    }

    /**
     * Sets the button data's icon by {@linkplain ClassLoader#getResource(String)
     * resource name}.
     * <p>
     * <b>Note</b>: Using this signature will cause an entry to be added in the
     * application context's {@linkplain ApplicationContext#getResourceCache()
     * resource cache} if one does not already exist.
     *
     * @param iconName
     * The resource name of the icon to set.
     */
    public void setIcon(String iconName) {
        ClassLoader classLoader = ThreadUtilities.getClassLoader();
        setIcon(classLoader.getResource(iconName));
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
