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
package org.apache.pivot.scene.text;

import org.apache.pivot.bxml.DefaultProperty;
import org.apache.pivot.scene.Extents;
import org.apache.pivot.scene.Font;
import org.apache.pivot.scene.Graphics;
import org.apache.pivot.scene.Node;

/**
 * Node representing a block of optionally-wrapped text.
 */
@DefaultProperty("text")
public class TextBox extends Node {
    private CharSequence text = ""; // TODO Default to null?

    /*
    private Font font = DEFAULT_FONT;
    private Paint fill = new SolidColorPaint(Color.BLACK);
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
    private VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
    private boolean wrap = false;
    */

    public static final Font DEFAULT_FONT = null; // TODO Get default system font

    public CharSequence getText() {
        return text;
    }

    public void setText(CharSequence text) {
        // TODO Allow null?

        // TODO Fire event
    }

    @Override
    public Extents getExtents() {
        // TODO
        return null;
    }

    @Override
    public boolean contains(int x, int y) {
        // TODO
        return false;
    }

    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public int getPreferredWidth(int height) {
        // TODO Calculate and cache preferred width
        return 0;
    }

    @Override
    public int getPreferredHeight(int width) {
        // TODO Calculate and cache preferred height
        return 0;
    }

    @Override
    public int getBaseline(int width, int height) {
        // TODO
        return 0;
    }

    @Override
    public void validate() {
        if (!isValid()) {
            // TODO Relayout text

            // TODO Set extents
        }

        super.validate();
    }

    @Override
    public void layout() {
        // TODO
    }

    @Override
    public void paint(Graphics graphics) {
        // TODO
    }
}
