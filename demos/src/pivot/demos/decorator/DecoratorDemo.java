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
package pivot.demos.decorator;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.Display;
import pivot.wtk.Frame;
import pivot.wtk.Window;
import pivot.wtk.effects.FadeDecorator;
import pivot.wtk.effects.ReflectionDecorator;
import pivot.wtkx.WTKXSerializer;

public class DecoratorDemo implements Application {
    private Window reflectionWindow = null;
    private Frame fadeFrame = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();

        reflectionWindow =
            new Window((Component)wtkxSerializer.readObject(getClass().getResource("reflection.wtkx")));
        reflectionWindow.setTitle("Reflection Window");
        reflectionWindow.getDecorators().add(new ReflectionDecorator());
        reflectionWindow.setLocation(20, 20);
        reflectionWindow.open(display);

        fadeFrame =
            new Frame((Component)wtkxSerializer.readObject(getClass().getResource("translucent.wtkx")));
        fadeFrame.setTitle("Translucent Window");

        final FadeDecorator fadeDecorator = new FadeDecorator();
        fadeFrame.getDecorators().update(0, fadeDecorator);

        fadeFrame.getComponentMouseListeners().add(new ComponentMouseListener() {
            public boolean mouseMove(Component component, int x, int y) {
                return false;
            }

            public void mouseOver(Component component) {
                fadeDecorator.setOpacity(0.9f);
                component.repaint();
            }

            public void mouseOut(Component component) {
                fadeDecorator.setOpacity(0.5f);
                component.repaint();
            }
        });

        fadeFrame.setLocation(80, 80);
        fadeFrame.open(display);
    }

    public boolean shutdown(boolean optional) {
        reflectionWindow.close();
        fadeFrame.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
