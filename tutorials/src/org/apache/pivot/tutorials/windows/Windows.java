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
package org.apache.pivot.tutorials.windows;

import java.io.IOException;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.Window;

public class Windows implements Application {
    private Display display = null;

    @Override
    public void startup(Display displayArgument, Map<String, String> properties) throws Exception {
        this.display = displayArgument;

        int x = 0;
        int y = 0;

        for (int i = 0; i < 3; i++) {
            BXMLSerializer bxmlSerializer = new BXMLSerializer();
            bxmlSerializer.getNamespace().put("application", this);

            Frame frame;
            try {
                frame = (Frame) bxmlSerializer.readObject(Windows.class, "frame.bxml");
            } catch (SerializationException exception) {
                throw new RuntimeException(exception);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }

            frame.setTitle("Frame " + (i + 1));
            frame.setLocation(x, y);
            x += 20;
            y += 20;

            frame.open(displayArgument);
        }
    }

    @Override
    public boolean shutdown(boolean optional) {
        for (int i = display.getLength() - 1; i >= 0; i--) {
            Window window = (Window) display.get(i);
            window.close();
        }

        return false;
    }

    public Window load(String fileName) throws SerializationException, IOException {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        return (Window) bxmlSerializer.readObject(Windows.class, fileName);
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Windows.class, args);
    }

}
