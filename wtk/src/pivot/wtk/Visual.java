/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk;

import java.awt.Graphics2D;

/**
 * Interface representing a visual. A visual is an object that can be drawn
 * to the screen or other output device.
 *
 * @version 1.2 (11/1/2007)
 */
public interface Visual {
    /**
     * Returns the visual's render width (the width it will use when
     * painting).
     */
    public int getWidth();

    /**
     * Returns the visual's render height (the height it will use when
     * painting).
     */
    public int getHeight();

    /**
     * Sets the visual's render size.
     *
     * @param width
     * @param height
     */
    public void setSize(int width, int height);

    /**
     * Returns the visual's preferred width given the provided height
     * constraint.
     *
     * @param height
     * The height by which to constrain the preferred width, or <tt>-1</tt>
     * for no constraint.
     */
    public int getPreferredWidth(int height);

    /**
     * Returns the visual's preferred height given the provided width
     * constraint.
     *
     * @param width
     * The width by which to constrain the preferred height, or <tt>-1</tt>
     * for no constraint.
     */
    public int getPreferredHeight(int width);

    /**
     * Returns the visual's unconstrained preferred size.
     */
    public Dimensions getPreferredSize();

    /**
     * Draws the visual.
     *
     * @param graphics
     * The graphics context in which to paint the visual.
     */
    public void paint(Graphics2D graphics);
}
