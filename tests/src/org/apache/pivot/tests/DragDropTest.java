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

import java.awt.Color;
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
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.LocalManifest;
import org.apache.pivot.wtk.Manifest;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.Visual;
import org.apache.pivot.wtk.media.Image;

public class DragDropTest implements Application {
    private Frame frame1 = new Frame();
    private Frame frame2 = new Frame();

    private static final Color IMAGE_VIEW_BACKGROUND_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color IMAGE_VIEW_DROP_HIGHLIGHT_COLOR = new Color(0xf0, 0xe6, 0x8c);

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        frame1.setTitle("Frame 1");
        frame1.setPreferredSize(160, 120);
        frame1.getStyles().put(Style.resizable, false);

        DragSource imageDragSource = new DragSource() {
            private Image image = null;
            private Point offset = null;
            private LocalManifest content = null;

            @Override
            public boolean beginDrag(Component component, int x, int y) {
                ImageView imageView = (ImageView) component;
                image = imageView.getImage();

                if (image != null) {
                    imageView.setImage((Image) null);
                    content = new LocalManifest();
                    content.putImage(image);
                    offset = new Point(x - (imageView.getWidth() - image.getWidth()) / 2, y
                        - (imageView.getHeight() - image.getHeight()) / 2);
                }

                return (image != null);
            }

            @Override
            public void endDrag(Component component, DropAction dropAction) {
                if (dropAction == null) {
                    ImageView imageView = (ImageView) component;
                    imageView.setImage(image);
                }

                image = null;
                offset = null;
                content = null;
            }

            @Override
            public boolean isNative() {
                return false;
            }

            @Override
            public LocalManifest getContent() {
                return content;
            }

            @Override
            public Visual getRepresentation() {
                return image;
            }

            @Override
            public Point getOffset() {
                return offset;
            }

            @Override
            public int getSupportedDropActions() {
                return DropAction.MOVE.getMask();
            }
        };

        DropTarget imageDropTarget = new DropTarget() {
            @Override
            public DropAction dragEnter(Component component, Manifest dragContent,
                int supportedDropActions, DropAction userDropAction) {
                DropAction dropAction = null;

                ImageView imageView = (ImageView) component;
                if (imageView.getImage() == null && dragContent.containsImage()
                    && DropAction.MOVE.isSelected(supportedDropActions)) {
                    dropAction = DropAction.MOVE;
                    component.getStyles().put(Style.backgroundColor, IMAGE_VIEW_DROP_HIGHLIGHT_COLOR);
                }

                return dropAction;
            }

            @Override
            public void dragExit(Component component) {
                component.getStyles().put(Style.backgroundColor, IMAGE_VIEW_BACKGROUND_COLOR);
            }

            @Override
            public DropAction dragMove(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (dragContent.containsImage() ? DropAction.MOVE : null);
            }

            @Override
            public DropAction userDropActionChange(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (dragContent.containsImage() ? DropAction.MOVE : null);
            }

            @Override
            public DropAction drop(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                DropAction dropAction = null;

                if (dragContent.containsImage()) {
                    ImageView imageView = (ImageView) component;
                    try {
                        imageView.setImage(dragContent.getImage());
                        dropAction = DropAction.MOVE;
                    } catch (IOException exception) {
                        System.err.println(exception);
                    }
                }

                dragExit(component);

                return dropAction;
            }
        };

        ImageView imageView1 = new ImageView();
        imageView1.setImage(Image.load(getClass().getResource("go-home.png")));
        imageView1.getStyles().put(Style.backgroundColor, IMAGE_VIEW_BACKGROUND_COLOR);
        imageView1.setDragSource(imageDragSource);
        imageView1.setDropTarget(imageDropTarget);

        frame1.setContent(imageView1);
        frame1.open(display);

        frame2.setTitle("Frame 2");
        frame2.setPreferredSize(160, 120);
        frame2.setLocation(180, 0);

        ImageView imageView2 = new ImageView();
        imageView2.getStyles().put(Style.backgroundColor, IMAGE_VIEW_BACKGROUND_COLOR);
        imageView2.setDragSource(imageDragSource);
        imageView2.setDropTarget(imageDropTarget);

        frame2.setContent(imageView2);

        frame2.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (frame1 != null) {
            frame1.close();
        }

        if (frame2 != null) {
            frame2.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(DragDropTest.class, args);
    }
}
