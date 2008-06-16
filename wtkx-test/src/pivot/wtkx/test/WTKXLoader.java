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
package pivot.wtkx.test;

import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.Window;
import pivot.wtkx.ComponentLoader;

public class WTKXLoader implements Application {
    private Window window = null;

    public static final String WTKX_RESOURCE_KEY = "wtkxResource";
    public static final String RESOURCE_BUNDLE_KEY = "resourceBundle";

    public void startup() throws Exception {
        ComponentLoader.initialize();

        ComponentLoader componentLoader = new ComponentLoader();

        ApplicationContext applicationContext = ApplicationContext.getInstance();
        ApplicationContext.PropertyDictionary properties = applicationContext.getProperties();

        String wtkxResourceName = (String)properties.get(WTKX_RESOURCE_KEY);
        if (wtkxResourceName == null) {
            throw new Exception("You must provide a value for the \""
                + WTKX_RESOURCE_KEY + "\" startup property.");
        }

        String resourceBundleBaseName = (String)properties.get(RESOURCE_BUNDLE_KEY);
        Component component = componentLoader.load(wtkxResourceName, resourceBundleBaseName);

        window = new Window();
        window.getAttributes().put(Display.MAXIMIZED_ATTRIBUTE, Boolean.TRUE);
        window.setContent(component);

        window.open();
    }

    public void shutdown() throws Exception {
        window.close();
    }

    public void resume() throws Exception {
    }

    public void suspend() throws Exception {
    }
}
