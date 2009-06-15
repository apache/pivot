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
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.media.Image;


/**
 * Default tree node implementation.
 *
 * @author gbrown
 */
public class TreeNode {
    private Image icon = null;
    private String text = null;

    public TreeNode() {
        this(null, null);
    }

    public TreeNode(Image icon) {
        this(icon, null);
    }

    public TreeNode(String text) {
        this(null, text);
    }

    public TreeNode(Image icon, String text) {
        this.icon = icon;
        this.text = text;
    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    public void setIcon(URL iconURL) {
        Image icon = (Image)ApplicationContext.getResourceCache().get(iconURL);

        if (icon == null) {
            icon = Image.load(iconURL);
            ApplicationContext.getResourceCache().put(iconURL, icon);
        }

        setIcon(icon);
    }

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
