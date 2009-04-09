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
package pivot.wtk.test;

import java.awt.Color;
import java.awt.GradientPaint;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Display;
import pivot.wtk.ScrollPane;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

/**
 * Tests the effectiveness of layout caching.
 *
 * @author tvolkert
 */
public class LayoutCacheTest implements Application {
    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties) throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(getClass().getResource("layoutCacheTest.wtkx"));

        ScrollPane scrollPane = (ScrollPane)wtkxSerializer.getObjectByName("scrollPane");
        scrollPane.getStyles().put("backgroundPaint", new GradientPaint(0, 0,
            new Color(0x7e, 0xb4, 0xda), 0, 1000, new Color(0x00, 0x25, 0x3f)));

        window.open(display);
    }

    public boolean shutdown(boolean optional) throws Exception {
        if (window != null) {
            window.close();
        }

        window = null;
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
