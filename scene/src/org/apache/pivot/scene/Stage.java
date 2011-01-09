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
 * Root node of a scene graph hierarchy.
 * <p>
 * TODO How to transfer focus in/out of the stage to the native host?
 */
public abstract class Stage extends Group {
    @Override
    protected void setGroup(Group group) {
        throw new UnsupportedOperationException("Stage can't have a parent.");
    }

    @Override
    public void setLocation(int x, int y) {
        throw new UnsupportedOperationException("Can't change the location of the stage.");
    }

    @Override
    public void setVisible(boolean visible) {
        throw new UnsupportedOperationException("Can't change the visibility of the stage.");
    }

    @Override
    public void repaint(int x, int y, int width, int height, boolean immediate) {
        if (immediate) {
            Graphics graphics = getHostGraphics();

            if (graphics != null) {
                graphics.clip(x, y, width, height);
                paint(graphics);
                graphics.dispose();
            }
        } else {
            repaintHost(x, y, width, height);
        }
    }

    public abstract void repaintHost(int x, int y, int width, int height);
    public abstract Graphics getHostGraphics();

    public abstract void requestNativeFocus();

    public abstract Object getNativeHost();
}
