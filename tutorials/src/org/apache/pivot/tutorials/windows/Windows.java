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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.ButtonData;
import org.apache.pivot.wtkx.WTKXSerializer;

public class Windows implements Application {
    private Window desktop = null;
    private PushButton newFrameButton = null;
    private BoxPane windowButtonBoxPane = null;

    private int frameNumber = 1;
    private int frameX = 0;
    private int frameY = 0;

    @Override
    public void startup(final Display display, Map<String, String> properties) throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        desktop = (Window)wtkxSerializer.readObject(this, "desktop.wtkx");

        display.getContainerListeners().add(new ContainerListener.Adapter() {
            @Override
            public void componentInserted(Container container, int index) {
                final Window window = (Window)container.get(index);

                if (window.getOwner() == desktop) {
                    final PushButton windowButton = new PushButton();
                    windowButton.getStyles().put("toolbar", true);

                    ButtonData windowButtonData = new ButtonData(window.getIcon(), window.getTitle());
                    windowButton.setButtonData(windowButtonData);
                    windowButton.getUserData().put("window", window);

                    windowButton.getButtonPressListeners().add(new ButtonPressListener() {
                        @Override
                        public void buttonPressed(Button button) {
                            window.moveToFront();
                        }
                    });

                    windowButtonBoxPane.add(windowButton);

                    ApplicationContext.queueCallback(new Runnable() {
                        @Override
                        public void run() {
                            windowButton.scrollAreaToVisible(0, 0, windowButton.getWidth(), windowButton.getHeight());
                        }
                    });
                }
            }

            @Override
            public void componentsRemoved(Container container, int index, Sequence<Component> removed) {
                removed = new ArrayList<Component>(removed);

                for (int i = windowButtonBoxPane.getLength() - 1; i >= 0; i--) {
                    PushButton windowButton = (PushButton)windowButtonBoxPane.get(i);
                    Window window = (Window)windowButton.getUserData().get("window");

                    for (int j = 0, n = removed.getLength(); j < n; j++) {
                        if (window == removed.get(j)) {
                            windowButtonBoxPane.remove(i, 1);
                            removed.remove(j, 1);
                            break;
                        }
                    }

                    if (removed.getLength() == 0) {
                        break;
                    }
                }
            }
        });

        newFrameButton = (PushButton)wtkxSerializer.get("newFrameButton");
        newFrameButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                WTKXSerializer wtkxSerializer = new WTKXSerializer();

                Frame frame;
                try {
                    frame = (Frame)wtkxSerializer.readObject(Windows.this, "frame.wtkx");
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }

                frame.setTitle("Frame " + frameNumber);
                frameNumber++;

                frame.setLocation(frameX, frameY);
                frameX += 20;
                if (frameX > display.getWidth()) {
                    frameX = 0;
                }

                frameY += 20;
                if (frameY > display.getHeight()) {
                    frameY = 0;
                }

                frame.open(desktop);
            }
        });

        windowButtonBoxPane = (BoxPane)wtkxSerializer.get("windowButtonBoxPane");

        desktop.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (desktop != null) {
            desktop.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Windows.class, args);
    }
}
