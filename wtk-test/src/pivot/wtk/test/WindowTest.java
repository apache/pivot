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
package pivot.wtk.test;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Display;
import pivot.wtk.Frame;

public class WindowTest implements Application {
    Frame window1 = new Frame();
    Frame window2 = new Frame();

    public void startup(Display display, Dictionary<String, String> properties) {
        window1.setTitle("Window 1");
        window1.setPreferredSize(320, 240);
        window1.open(display);

        Frame window1a = new Frame();
        window1a.setTitle("Window 1 A");
        window1a.setPreferredSize(160, 120);
        window1a.open(window1);

        Frame window1ai = new Frame();
        window1ai.setTitle("Window 1 A I");
        window1ai.setPreferredSize(160, 60);
        window1ai.open(window1a);

        Frame window1aii = new Frame();
        window1aii.setTitle("Window 1 A II");
        window1aii.setPreferredSize(160, 60);
        window1aii.open(window1a);

        Frame window1b = new Frame();
        window1b.setTitle("Window 1 B");
        window1b.setPreferredSize(160, 120);
        window1b.setLocation(20, 20);
        window1b.open(window1);

        Frame window1bi = new Frame();
        window1bi.setTitle("Window 1 B I");
        window1bi.setPreferredSize(160, 60);
        window1bi.open(window1b);

        Frame window1bii = new Frame();
        window1bii.setTitle("Window 1 B II");
        window1bii.setPreferredSize(160, 60);
        window1bii.open(window1b);
    }

    public boolean shutdown(boolean optional) {
        window1.close();
        window2.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
