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
package org.apache.pivot.wtk;

import java.awt.Graphics2D;

/**
 * Interface representing a "visual". A visual is an object that can be drawn
 * to the screen or other output device.
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
     * Returns the visual's baseline.
     *
     * @return
     * The baseline relative to the origin of the visual, or <tt>-1</tt> if
     * this visual does not have a baseline.
     */
    public int getBaseline();

    /**
     * Paints the visual.
     *
     * @param graphics
     * The graphics context in which to paint the visual.
     */
    public void paint(Graphics2D graphics);
}
