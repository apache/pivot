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
import pivot.wtk.Display;
import pivot.wtk.DragSource;
import pivot.wtk.DropAction;
import pivot.wtk.DropTarget;
import pivot.wtk.Frame;
import pivot.wtk.ImageView;
import pivot.wtk.Point;
import pivot.wtk.Visual;
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

        DragSource imageDragSource = new DragSource() {
            ImageView imageView = null;
            private Image image = null;
            private Point offset = null;

            public boolean beginDrag(Component component, int x, int y) {
                imageView = (ImageView)component;
                image = imageView.getImage();

                if (image != null) {
                    imageView.setImage((Image)null);
                    offset = new Point(x - (imageView.getWidth() - image.getWidth()) / 2,
                        y - (imageView.getHeight() - image.getHeight()) / 2);
                }

                return (image != null);
            }

            public void endDrag(DropAction dropAction) {
                if (dropAction == null) {
                    imageView.setImage(image);
                }
            }

            public Object getContent() {
                return image;
            }

            public Visual getRepresentation() {
                return image;
            }

            public Point getOffset() {
                return offset;
            }

            public int getSupportedDropActions() {
                return DropAction.MOVE.getMask();
            }
        };

        DropTarget imageDropTarget = new DropTarget() {
            public boolean isDrop(Component component, Class<?> dragContentType,
                DropAction dropAction, int x, int y) {
                return (Image.class.isAssignableFrom(dragContentType)
                    && dropAction == DropAction.MOVE);
            }

            public void highlightDrop(Component component, boolean highlight) {
                component.getStyles().put("backgroundColor", highlight ?
                    IMAGE_VIEW_DROP_HIGHLIGHT_COLOR : IMAGE_VIEW_BACKGROUND_COLOR);
            }

            public void updateDropHighlight(Component component, Class<?> dragContentType,
                DropAction dropAction, int x, int y) {
                // No-op
            }

            public void drop(Component component, Object dragContent, DropAction dropAction,
                int x, int y) {
                ImageView imageView = (ImageView)component;
                imageView.setImage((Image)dragContent);
            }
        };

        ImageView imageView1 = new ImageView();
        imageView1.setImage(Image.load(getClass().getResource("go-home.png")));
        imageView1.getStyles().put("backgroundColor", IMAGE_VIEW_BACKGROUND_COLOR);
        imageView1.setDragSource(imageDragSource);
        imageView1.setDropTarget(imageDropTarget);

        frame1.setContent(imageView1);
        frame1.open(display);

        frame2.setTitle("Frame 2");
        frame2.setPreferredSize(160, 120);
        frame2.setLocation(180, 0);

        ImageView imageView2 = new ImageView();
        imageView2.getStyles().put("backgroundColor", IMAGE_VIEW_BACKGROUND_COLOR);
        imageView2.setDragSource(imageDragSource);
        imageView2.setDropTarget(imageDropTarget);

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
