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
package pivot.wtk.media.drawing;

import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Shape representing a block of text.
 * <p>
 * TODO We may need to specify a font here - otherwise, we won't be able to
 * calculate the bounds.
 */
public class Text extends Shape {
    private String text = null;
    private Font font = null;
    private int wrapWidth = -1;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        // TODO We may need to throw if null
        this.font = font;
    }

    public int getWrapWidth() {
        return wrapWidth;
    }

    public void setWrapWidth(int wrapWidth) {
        if (wrapWidth < -1) {
            throw new IllegalArgumentException(wrapWidth
                + " is not a valid value for wrap width.");
        }

        this.wrapWidth = wrapWidth;
    }

    @Override
    public boolean contains(int x, int y) {
        // TODO
        return false;
    }

    public void paint(Graphics2D graphics) {
        // TODO
    }
}
