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
package org.apache.pivot.demos.memorygame;

import org.apache.pivot.wtk.content.ButtonData;
import org.apache.pivot.wtk.media.Image;

public class MemGameButtonData extends ButtonData {
    private String defaultURL;
    private String buttonURL;

    public MemGameButtonData(String defaultURL, String buttonURL) {
        super();
        this.defaultURL = defaultURL;
        this.buttonURL = buttonURL;
        setDefaultURL();
    }

    public void setDefaultURL() {
        this.setIcon(defaultURL);
    }

    public void setButtonURL() {
        this.setIcon(buttonURL);
    }

    public Image getButtonURL() {
        return this.getIcon();
    }

}
