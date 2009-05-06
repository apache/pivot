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
package pivot.tutorials.drawing;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.ImageView;
import pivot.wtk.Window;
import pivot.wtk.media.Drawing;
import pivot.wtk.media.drawing.Shape;
import pivot.wtkx.Bindable;

public class RotateLine extends Bindable implements Application {
    @Load("rotate_line.wtkd") private Drawing drawing;
    @Bind(resource="drawing") private Shape.Rotate rotation;

    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception{
        bind();

        ApplicationContext.scheduleRecurringCallback(new Runnable() {
            public void run() {
                int angle = (int)rotation.getAngle();
                angle = (angle + 6) % 360;
                rotation.setAngle(angle);
            }
        }, 1000);

        window = new Window(new ImageView(drawing));
        window.setMaximized(true);
        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(RotateLine.class, args);
    }
}
