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
package org.apache.pivot.wtk;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtkx.WTKXSerializer;

/**
 * Script application loader.
 *
 * @author gbrown
 */
public class ScriptApplication implements Application {
    private Window window = null;

    public static final String SRC_ARGUMENT = "src";

    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        String src = null;

        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        for (String property : properties) {
            if (property.equals(SRC_ARGUMENT)) {
                src = properties.get(property);
            } else {
                wtkxSerializer.put(property, properties.get(property));
            }
        }

        if (src == null) {
            throw new IllegalArgumentException(SRC_ARGUMENT + " argument is required.");
        }

        window = (Window)wtkxSerializer.readObject(src);
        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        window.close();
        return true;
    }

    public void resume() {
    }

    public void suspend() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(ScriptApplication.class, args);
    }
}
