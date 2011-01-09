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
package org.apache.pivot.scene.shape;

import org.apache.pivot.scene.Extents;
import org.apache.pivot.scene.Graphics;

/**
 * Shape representing an ellipse.
 */
public class Ellipse extends Shape {
    @Override
    public boolean contains(int x, int y) {
        // TODO
        return true;
    }

    @Override
    public Extents getExtents() {
        return new Extents(0, getWidth(), 0, getHeight());
    }

    @Override
    protected void drawShape(Graphics graphics) {
        graphics.drawEllipse(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    protected void fillShape(Graphics graphics) {
        graphics.fillEllipse(getX(), getY(), getWidth(), getHeight());
    }
}
