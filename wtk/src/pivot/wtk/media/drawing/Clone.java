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
package pivot.wtk.media.drawing;

import java.awt.Graphics2D;

import pivot.wtk.Bounds;

/**
 * Shape representing a copy of another shape.
 * <p>
 * TODO Throw in setStroke() and setFill()?
 */
public class Clone extends Shape {
    private Shape source = null;

    public Shape getSource() {
        return source;
    }

    public void setSource(Shape source) {
        this.source = source;
    }

    @Override
    public Bounds getUntransformedBounds() {
        return (source == null) ? new Bounds(0, 0, 0, 0) : source.getUntransformedBounds();
    }

    @Override
    public void fill(Graphics2D graphics) {
        if (source != null) {
            source.fill(graphics);
        }
    }

    @Override
    public void stroke(Graphics2D graphics) {
        if (source != null) {
            source.stroke(graphics);
        }
    }

    @Override
    public boolean contains(int x, int y) {
        return (source == null) ? false : source.contains(x, y);
    }
}
