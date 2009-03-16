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
import pivot.wtk.skin.DisplaySkin;

/**
 * Container that serves as the root of a component hierarchy.
 *
 * @author gbrown
 */
public final class Display extends Container {
    private class ValidateCallback implements Runnable {
        public void run() {
            validate();
        }
    }

    private ApplicationContext applicationContext = null;

    protected Display(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        super.setSkin(new DisplaySkin());
    }

    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public DragDropManager getDragDropManager() {
        return applicationContext.getDragDropManager();
    }

    @Override
    protected void setSkin(Skin skin) {
        throw new UnsupportedOperationException("Can't replace Display skin.");
    }

    @Override
    public void setLocation(int x, int y) {
        throw new UnsupportedOperationException("Can't change the location of the display.");
    }

    @Override
    public void invalidate() {
        if (isValid()) {
            super.invalidate();
            ApplicationContext.queueCallback(new ValidateCallback());
        }
    }

    @Override
    public void repaint(int x, int y, int width, int height, boolean immediate) {
        if (immediate) {
            Graphics2D graphics = applicationContext.getGraphics();
            graphics.clipRect(x, y, width, height);
            paint(graphics);
            graphics.dispose();
        } else {
            applicationContext.repaint(x, y, width, height);
        }
    }

    @Override
    public void insert(Component component, int index) {
        if (!(component instanceof Window)) {
            throw new IllegalArgumentException("component must be an instance "
                                               + "of " + Window.class);
        }

        super.insert(component, index);
    }
}
