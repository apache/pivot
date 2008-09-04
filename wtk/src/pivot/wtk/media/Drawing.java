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
package pivot.wtk.media;

import java.awt.Graphics2D;

import pivot.collections.Sequence;
import pivot.wtk.media.drawing.Shape;

public class Drawing extends Image {
    public class ShapeSequence implements Sequence<Shape> {
        public int add(Shape shape) {
            // TODO Auto-generated method stub
            return 0;
        }

        public void insert(Shape shape, int index) {
            // TODO Auto-generated method stub

        }

        public Shape update(int index, Shape shape) {
            // TODO Auto-generated method stub
            return null;
        }

        public int remove(Shape shape) {
            // TODO Auto-generated method stub
            return 0;
        }

        public Sequence<Shape> remove(int index, int count) {
            // TODO Auto-generated method stub
            return null;
        }

        public Shape get(int index) {
            // TODO Auto-generated method stub
            return null;
        }

        public int indexOf(Shape shape) {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getLength() {
            // TODO Auto-generated method stub
            return 0;
        }
    }

    public int getWidth() {
        // TODO
        return 0;
    }

    public int getHeight() {
        // TODO
        return 0;
    }

    public void paint(Graphics2D graphics) {
        // TODO Apply a scale based on the difference between the preferred
        // size and the actual size
    }

    public ShapeSequence getShapes() {
        // TODO
        return null;
    }
}
