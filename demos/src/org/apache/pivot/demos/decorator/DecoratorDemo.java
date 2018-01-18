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

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.effects.FadeDecorator;

public class DecoratorDemo implements Application {
    private Window reflectionWindow = null;
    private Frame translucentFrame = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        reflectionWindow = (Window) bxmlSerializer.readObject(DecoratorDemo.class,
            "reflection_window.bxml");
        translucentFrame = (Frame) bxmlSerializer.readObject(DecoratorDemo.class,
            "translucent_frame.bxml");

        final FadeDecorator fadeDecorator = new FadeDecorator();
        translucentFrame.getDecorators().insert(fadeDecorator, 0);

        translucentFrame.getComponentMouseListeners().add(new ComponentMouseListener() {
            @Override
            public void mouseOver(Component component) {
                fadeDecorator.setOpacity(0.9f);
                component.repaint();
            }

            @Override
            public void mouseOut(Component component) {
                fadeDecorator.setOpacity(0.5f);
                component.repaint();
            }
        });

        reflectionWindow.open(display);
        translucentFrame.open(reflectionWindow);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (reflectionWindow != null) {
            reflectionWindow.close();
        }

        if (translucentFrame != null) {
            translucentFrame.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(DecoratorDemo.class, args);
    }

}
