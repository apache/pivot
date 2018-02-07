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
package org.apache.pivot.tests;

import java.awt.Font;
import java.io.IOException;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.DragSource;
import org.apache.pivot.wtk.DropAction;
import org.apache.pivot.wtk.DropTarget;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.LocalManifest;
import org.apache.pivot.wtk.Manifest;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Visual;

public class NativeDragDropTest implements Application {
    private Frame frame = null;

    @Override
    public void startup(final Display display, Map<String, String> properties) throws Exception {
        final Label label = new Label("http://pivot.apache.org/");
        label.getStyles().put(Style.font, new Font("Arial", Font.PLAIN, 24));
        label.getStyles().put(Style.horizontalAlignment, HorizontalAlignment.CENTER);
        label.getStyles().put(Style.verticalAlignment, VerticalAlignment.CENTER);

        label.setDragSource(new DragSource() {
            private LocalManifest content = null;

            @Override
            public boolean beginDrag(Component component, int x, int y) {
                content = new LocalManifest();
                content.putText(label.getText());
                return true;
            }

            @Override
            public void endDrag(Component component, DropAction dropAction) {
                content = null;
            }

            @Override
            public boolean isNative() {
                return true;
            }

            @Override
            public LocalManifest getContent() {
                return content;
            }

            @Override
            public Visual getRepresentation() {
                return null;
            }

            @Override
            public Point getOffset() {
                return null;
            }

            @Override
            public int getSupportedDropActions() {
                return DropAction.COPY.getMask();
            }
        });

        label.setDropTarget(new DropTarget() {
            @Override
            public DropAction dragEnter(Component component, Manifest dragContent,
                int supportedDropActions, DropAction userDropAction) {
                DropAction dropAction = null;

                if (dragContent.containsText()) {
                    frame.getStyles().put(Style.backgroundColor, "#ffcccc");
                    dropAction = DropAction.COPY;
                }

                return dropAction;
            }

            @Override
            public void dragExit(Component component) {
                frame.getStyles().put(Style.backgroundColor, "#ffffff");
            }

            @Override
            public DropAction dragMove(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (dragContent.containsText() ? DropAction.COPY : null);
            }

            @Override
            public DropAction userDropActionChange(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (dragContent.containsText() ? DropAction.COPY : null);
            }

            @Override
            public DropAction drop(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                DropAction dropAction = null;

                if (dragContent.containsText()) {
                    Label labelLocal = (Label) component;
                    try {
                        labelLocal.setText(dragContent.getText());
                    } catch (IOException exception) {
                        System.err.println(exception);
                    }
                }

                dragExit(component);

                return dropAction;
            }
        });

        frame = new Frame(label);
        frame.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (frame != null) {
            frame.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(NativeDragDropTest.class, args);
    }
}
