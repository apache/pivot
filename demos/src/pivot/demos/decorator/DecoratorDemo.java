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
package pivot.demos.decorator;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.Frame;
import pivot.wtk.Window;
import pivot.wtk.effects.FadeDecorator;
import pivot.wtkx.Bindable;

public class DecoratorDemo extends Bindable implements Application {
    @Load(name="reflection.wtkx") private Window reflectionWindow;
    @Load(name="translucent.wtkx") private Frame translucentFrame;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        bind();

        reflectionWindow.open(display);

        final FadeDecorator fadeDecorator = new FadeDecorator();
        translucentFrame.getDecorators().insert(fadeDecorator, 0);

        translucentFrame.getComponentMouseListeners().add(new ComponentMouseListener.Adapter() {
            public void mouseOver(Component component) {
                fadeDecorator.setOpacity(0.9f);
                component.repaint();
            }

            public void mouseOut(Component component) {
                fadeDecorator.setOpacity(0.5f);
                component.repaint();
            }
        });

        translucentFrame.open(display);
    }

    public boolean shutdown(boolean optional) {
        if (reflectionWindow != null) {
            reflectionWindow.close();
        }

        if (translucentFrame != null) {
            translucentFrame.close();
        }

        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(DecoratorDemo.class, args);
    }
}
