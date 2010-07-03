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

import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.media.Image;

/**
 * Default table header data implementation.
 */
public class TableViewHeaderData {
    private Image icon = null;
    private String text = null;

    public TableViewHeaderData() {
        this(null, null);
    }

    public TableViewHeaderData(Image icon) {
        this(icon, null);
    }

    public TableViewHeaderData(String text) {
        this(null, text);
    }

    public TableViewHeaderData(Image icon, String text) {
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
     * Sets the header data's icon by URL.
     * <p>
     * <b>Note</b>: Using this signature will cause an entry to be added in the
     * application context's {@linkplain ApplicationContext#getResourceCache()
     * resource cache} if one does not already exist.
     *
     * @param iconURL
     * The location of the icon to set.
     */
    public void setIcon(URL iconURL) {
        if (iconURL == null) {
            throw new IllegalArgumentException("iconURL is null.");
        }

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
     * Sets the header data's icon by {@linkplain ClassLoader#getResource(String)
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
        if (iconName == null) {
            throw new IllegalArgumentException("iconName is null.");
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        setIcon(classLoader.getResource(iconName));
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
