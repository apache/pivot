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

import java.awt.Graphics2D;

import pivot.wtk.Bounds;

/**
 * Shape representing a rectangle.
 *
 * @author gbrown
 */
public class Rectangle extends Shape {
    @Override
    public Bounds getBounds() {
        // TODO
        return null;
    }

    @Override
    public boolean contains(int x, int y) {
        // TODO
        return false;
    }

    public void draw(Graphics2D graphics) {
        // TODO
    }

    public int getWidth() {
        // TODO
        return 0;
    }

    public int getHeight() {
        // TODO
        return 0;
    }

    public void setSize(int width, int height) {
        // TODO Call invalidateBounds(); note that we need to call this
        // instance's method and not just the parent because a transform
        // may be applied and we'll need to recalculate the transformed bounds

        // TODO Set internal width, height
    }
}
