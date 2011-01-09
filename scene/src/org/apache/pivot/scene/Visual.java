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
package org.apache.pivot.scene;

/**
 * Interface representing an object that can be drawn to the screen or
 * other output device.
 */
public interface Visual {
    /**
     * Returns the visual's width.
     */
    public int getWidth();

    /**
     * Returns the visual's height.
     */
    public int getHeight();

    /**
     * Sets the visual's size.
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
     * Returns the baseline for a given width and height.
     *
     * @param width
     * @param height
     *
     * @return
     * The baseline relative to the origin of this visual, or <tt>-1</tt> if
     * this visual does not have a baseline.
     */
    public int getBaseline(int width, int height);

    /**
     * Paints the visual.
     *
     * @param graphics
     * The graphics context in which to paint the visual.
     */
    public void paint(Graphics graphics);
}
