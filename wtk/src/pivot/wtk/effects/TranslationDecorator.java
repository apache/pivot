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

import java.awt.Graphics2D;

import pivot.wtk.Bounds;
import pivot.wtk.Component;
import pivot.wtk.Decorator;
import pivot.wtk.Point;

/**
 * <p>Decorator that translates the paint origin of its component.</p>
 *
 * @author gbrown
 */
public class TranslationDecorator implements Decorator {
    private int x = 0;
    private int y = 0;

    public TranslationDecorator() {
    }

    public TranslationDecorator(int x, int y) {
        setOffset(x, y);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Point getOffset() {
        return new Point(x, y);
    }

    public void setOffset(Point offset) {
        if (offset == null) {
            throw new IllegalArgumentException("offset is null.");
        }

        setOffset(offset.x, offset.y);
    }

    public void setOffset(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Graphics2D prepare(Component component, Graphics2D graphics) {
        graphics.translate(x, y);
        return graphics;
    }

    public void update() {
    }

    public Bounds getAffectedArea(Component component, int x, int y, int width, int height) {
        Bounds affectedArea = new Bounds(x + this.x, y + this.y, width, height);
        affectedArea.intersect(component.getBounds());

        return affectedArea;
    }
}
