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

/**
 * Interface representing a visual that is used in layout.
 */
public interface ConstrainedVisual extends Visual {
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
}
