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

import java.awt.Color;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseDragListener;
import pivot.wtk.ComponentMouseDropListener;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.Display;
import pivot.wtk.Frame;
import pivot.wtk.ImageView;
import pivot.wtk.Mouse;
import pivot.wtk.MouseDragListener;
import pivot.wtk.Point;
import pivot.wtk.media.Image;

public class DragDropTest implements Application {
    private Frame frame1 = new Frame();
    private Frame frame2 = new Frame();

    private static final Color IMAGE_VIEW_BACKGROUND_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color IMAGE_VIEW_DROP_HIGHLIGHT_COLOR = new Color(0xf0, 0xe6, 0x8c);

    public void startup(Display display, Dictionary<String, String> properties) throws Exception {
        frame1.setTitle("Frame 1");
        frame1.setPreferredSize(160, 120);
        frame1.getStyles().put("resizable", false);

        ComponentMouseDragListener imageDragListener = new ComponentMouseDragListener() {
            public boolean mouseDrag(Component component, int x, int y) {
                final ImageView imageView = (ImageView)component;
                final Image image = imageView.getImage();

                if (image != null) {
                    imageView.setImage((Image)null);
                    Point offset = new Point(x - (imageView.getWidth() - image.getWidth()) / 2,
                        y - (imageView.getHeight() - image.getHeight()) / 2);

                    Mouse.drag(image, Mouse.DropAction.MOVE.getMask(), image, offset,
                        new MouseDragListener() {
                            public void mouseDrop(Mouse.DropAction dropAction) {
                                if (dropAction == null) {
                                    imageView.setImage(image);
                                }
                            }
                    });
                }

                return false;
            }
        };

        ComponentMouseDropListener imageDropListener = new ComponentMouseDropListener() {
            public boolean mouseDrop(Component component, int x, int y) {
                Class<?> dragContentType = Mouse.getDragContentType();

                if (dragContentType != null
                    && Image.class.isAssignableFrom(dragContentType)
                    && Mouse.isValidDropAction(Mouse.DropAction.MOVE)) {
                    ImageView imageView = (ImageView)component;

                    if (imageView.getImage() == null) {
                        imageView.setImage((Image)Mouse.drop(Mouse.DropAction.MOVE));
                        imageView.getStyles().put("backgroundColor", IMAGE_VIEW_BACKGROUND_COLOR);
                    }
                }

                return false;
            }
        };

        ComponentMouseListener imageMouseListener = new ComponentMouseListener() {
            public boolean mouseMove(Component component, int x, int y) {
                return false;
            }

            public void mouseOver(Component component) {
                Class<?> dragContentType = Mouse.getDragContentType();

                if (dragContentType != null
                    && Image.class.isAssignableFrom(dragContentType)) {
                    ImageView imageView = (ImageView)component;

                    if (imageView.getImage() == null) {
                        component.getStyles().put("backgroundColor", IMAGE_VIEW_DROP_HIGHLIGHT_COLOR);
                    }
                }
            }

            public void mouseOut(Component component) {
                component.getStyles().put("backgroundColor", IMAGE_VIEW_BACKGROUND_COLOR);
            }
        };

        ImageView imageView1 = new ImageView();
        imageView1.setImage(Image.load(getClass().getResource("go-home.png")));
        imageView1.getStyles().put("backgroundColor", IMAGE_VIEW_BACKGROUND_COLOR);
        imageView1.getComponentMouseDragListeners().add(imageDragListener);
        imageView1.getComponentMouseDropListeners().add(imageDropListener);
        imageView1.getComponentMouseListeners().add(imageMouseListener);

        frame1.setContent(imageView1);
        frame1.open(display);

        frame2.setTitle("Frame 2");
        frame2.setPreferredSize(160, 120);
        frame2.setLocation(180, 0);

        ImageView imageView2 = new ImageView();
        imageView2.getStyles().put("backgroundColor", IMAGE_VIEW_BACKGROUND_COLOR);
        imageView2.getComponentMouseDragListeners().add(imageDragListener);
        imageView2.getComponentMouseDropListeners().add(imageDropListener);
        imageView2.getComponentMouseListeners().add(imageMouseListener);

        frame2.setContent(imageView2);

        frame2.open(display);
    }

    public boolean shutdown(boolean optional) {
        frame1.close();
        frame2.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
