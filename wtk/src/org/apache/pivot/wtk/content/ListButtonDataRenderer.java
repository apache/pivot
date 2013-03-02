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

import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.HorizontalAlignment;

/**
 * Default list button data renderer.
 * <p>
 * TODO Add showIcon property to this class so the size of the button doesn't
 * change when changing selection between items with and without icons.
 */
public class ListButtonDataRenderer extends ButtonDataRenderer {
    public ListButtonDataRenderer() {
        getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
    }

    @Override
    public void render(final Object data, final Button button, boolean highlight) {
        Object dataMutable = data;
        if (dataMutable == null) {
            dataMutable = "";
        } else {
            if (dataMutable instanceof ListItem) {
                ListItem listItem = (ListItem)dataMutable;
                dataMutable = new ButtonData(listItem.getIcon(), listItem.getText());
            }
        }

        super.render(dataMutable, button, highlight);
    }
}
