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

import java.awt.Color;

import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.ImageView;

/**
 * List button renderer for displaying color swatches.
 */
public class ListButtonColorItemRenderer extends ImageView
    implements Button.DataRenderer {
    private ListViewColorItemRenderer.ColorBadge colorBadge =
        new ListViewColorItemRenderer.ColorBadge();

    public ListButtonColorItemRenderer() {
        setImage(colorBadge);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    @Override
    public void render(Object data, Button button, boolean highlighted) {
        Color color;
        if (data == null) {
            color = Color.WHITE;
        } else if (data instanceof ColorItem) {
            ColorItem colorItem = (ColorItem)data;
            color = colorItem.getColor();
        } else if (data instanceof Color) {
            color = (Color)data;
        } else {
            color = GraphicsUtilities.decodeColor(data.toString());
        }

        colorBadge.setColor(button.isEnabled() ?
            color : new Color(color.getRed(), color.getGreen(), color.getBlue(), 0x99));
    }

    @Override
    public String toString(Object data) {
        return null;
    }
}
