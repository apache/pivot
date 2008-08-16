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
package pivot.wtk.effects;

import pivot.wtk.Decorator;
import pivot.wtk.Dimensions;
import pivot.wtk.Rectangle;
import pivot.wtk.Visual;

/**
 * Abstract base class for decorators. Assumes that the decorator does not
 * change the size of the visual.
 *
 * @author gbrown
 */
public abstract class AbstractDecorator implements Decorator {
    protected Visual visual = null;

    public void prepare(Visual visual) {
        this.visual = visual;
    }

    public Rectangle getDirtyRegion(Rectangle bounds) {
        return bounds;
    }

    public int getWidth() {
        return visual.getWidth();
    }

    public int getHeight() {
        return visual.getHeight();
    }

    public void setSize(int width, int height) {
        visual.setSize(width, height);
    }

    public int getPreferredWidth(int height) {
        return visual.getPreferredWidth(height);
    }

    public int getPreferredHeight(int width) {
        return visual.getPreferredHeight(width);
    }

    public Dimensions getPreferredSize() {
        return visual.getPreferredSize();
    }
}
