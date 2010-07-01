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
package org.apache.pivot.tests;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.media.Drawing;
import org.apache.pivot.wtk.media.drawing.Canvas;
import org.apache.pivot.wtk.media.drawing.Shape;

public class DrawingTest implements Application {
    private Window window = null;
    private ImageView imageView = null;
    private ComponentMouseButtonListener imageViewMouseButtonListener = new ComponentMouseButtonListener.Adapter() {
        @Override
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            Drawing drawing = (Drawing)imageView.getImage();
            Canvas canvas = drawing.getCanvas();
            Shape descendant = canvas.getDescendantAt(x, y);

            System.out.println(descendant);

            return false;
        }
    };

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer beanSerializer = new BXMLSerializer();
        window = (Window)beanSerializer.readObject(this, "drawing_test.bxml");
        imageView = (ImageView)beanSerializer.get("imageView");

        imageView.getComponentMouseButtonListeners().add(imageViewMouseButtonListener);

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(DrawingTest.class, args);
    }
}
