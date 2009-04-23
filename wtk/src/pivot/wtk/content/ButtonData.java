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
package pivot.wtk.content;

import java.net.URL;

import pivot.util.ThreadUtilities;
import pivot.wtk.ApplicationContext;
import pivot.wtk.media.Image;

/**
 * Default button data implementation.
 *
 * @author gbrown
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
