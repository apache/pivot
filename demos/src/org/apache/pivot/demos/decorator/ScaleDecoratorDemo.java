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
package org.apache.pivot.demos.decorator;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseWheelListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.effects.ScaleDecorator;

public class ScaleDecoratorDemo extends Application.Adapter {
    private Window scaleWindow;
    @BXML private ImageView imageView;
    @BXML private ScaleDecorator scaleDecorator;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        scaleWindow = (Window) bxmlSerializer.readObject(DecoratorDemo.class,
            "scale_window.bxml");
        bxmlSerializer.bind(this);

        imageView.getComponentMouseWheelListeners().add(new ComponentMouseWheelListener() {
            @Override
            public boolean mouseWheel(Component component, Mouse.ScrollType scrollType, int scrollAmount,
                                      int wheelRotation, int x, int y) {
                // Note: both scale values are the same
                float currentScale = scaleDecorator.getScaleX();
                if (wheelRotation < 0) {
                    // UP == zoom in, make scale larger
                    scaleDecorator.setScale(currentScale * 2.0f);
                } else {
                    // DOWN == zoom out, make scale smaller
                    scaleDecorator.setScale(currentScale / 2.0f);
                }
                component.repaint();
                return true;
            }
        });

        scaleWindow.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (scaleWindow != null) {
            scaleWindow.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(ScaleDecoratorDemo.class, args);
    }

}
