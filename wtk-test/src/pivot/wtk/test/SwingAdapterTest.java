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
import pivot.wtkx.WTKXSerializer;

public class SwingAdapterTest implements Application {
    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer;
        Frame frame;

        wtkxSerializer = new WTKXSerializer();
        frame = (Frame)wtkxSerializer.readObject(getClass().getResource("swing_adapter_test.wtkx"));
        frame.open(display);

        wtkxSerializer = new WTKXSerializer();
        frame = (Frame)wtkxSerializer.readObject(getClass().getResource("swing_adapter_test.wtkx"));
        frame.open(display);
        frame.setLocation(20, 20);
    }

    public boolean shutdown(boolean optional) {
        return true;
    }

    public void resume() {
    }


    public void suspend() {
    }
}
