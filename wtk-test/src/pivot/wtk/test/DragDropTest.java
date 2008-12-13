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
import java.awt.image.BufferedImage;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.Sequence;
import pivot.util.concurrent.TaskExecutionException;
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
import pivot.wtk.data.ByteArrayTransport;
import pivot.wtk.data.Manifest;
import pivot.wtk.data.Transport;
import pivot.wtk.media.BufferedImageSerializer;
import pivot.wtk.media.Image;
import pivot.wtk.media.Picture;

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
            private Picture picture = null;
            private Point offset = null;
            private ArrayList<Transport> content = null;

            public boolean beginDrag(Component component, int x, int y) {
                ImageView imageView = (ImageView)component;
                Image image = imageView.getImage();
                if (image instanceof Picture) {
                    picture = (Picture)image;
                }

                if (picture != null) {
                    imageView.setImage((Image)null);
                    content = new ArrayList<Transport>();

                    BufferedImageSerializer serializer = new BufferedImageSerializer();
                    serializer.setOutputFormat(BufferedImageSerializer.Format.PNG);
                    content.add(new ByteArrayTransport(picture.getBufferedImage(), serializer));

                    offset = new Point(x - (imageView.getWidth() - image.getWidth()) / 2,
                        y - (imageView.getHeight() - image.getHeight()) / 2);
                }

                return (picture != null);
            }

            public void endDrag(Component component, DropAction dropAction) {
                if (dropAction == null) {
                    ImageView imageView = (ImageView)component;
                    imageView.setImage(picture);
                }

                picture = null;
                offset = null;
                content = null;
            }

            public boolean isNative() {
                return false;
            }

            public Sequence<Transport> getContent() {
                return content;
            }

            public Visual getRepresentation() {
                return picture;
            }

            public Point getOffset() {
                return offset;
            }

            public int getSupportedDropActions() {
                return DropAction.MOVE.getMask();
            }
        };

        DropTarget imageDropTarget = new DropTarget() {
            private int contentIndex = -1;

            public DropAction dragEnter(Component component, Manifest dragContent,
                int supportedDropActions, DropAction userDropAction) {
                DropAction dropAction = null;

                ImageView imageView = (ImageView)component;
                if (imageView.getImage() == null
                    && DropAction.MOVE.isSelected(supportedDropActions)) {
                    for (int i = 0, n = dragContent.getLength(); i < n; i++) {
                        String mimeType = dragContent.getMIMEType(i);
                        if (mimeType.startsWith(BufferedImageSerializer.Format.PNG.getMIMEType())) {
                            contentIndex = i;
                            break;
                        }
                    }
                }

                if (contentIndex != -1) {
                    dropAction = DropAction.MOVE;
                    component.getStyles().put("backgroundColor", IMAGE_VIEW_DROP_HIGHLIGHT_COLOR);
                }

                return dropAction;
            }

            public void dragExit(Component component) {
                component.getStyles().put("backgroundColor", IMAGE_VIEW_BACKGROUND_COLOR);
                contentIndex = -1;
            }

            public DropAction dragMove(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (contentIndex == -1 ? null : DropAction.MOVE);
            }

            public DropAction userDropActionChange(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (contentIndex == -1 ? null : DropAction.MOVE);
            }

            public DropAction drop(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                DropAction dropAction = null;

                if (contentIndex != -1) {
                    ImageView imageView = (ImageView)component;

                    BufferedImageSerializer serializer = new BufferedImageSerializer();
                    Manifest.ReadTask readTask = new Manifest.ReadTask(dragContent, contentIndex, serializer);

                    try {
                        imageView.setImage(new Picture((BufferedImage)readTask.execute()));
                        dropAction = DropAction.MOVE;
                    } catch(TaskExecutionException exception) {
                        // No-op; we couldn't set the image
                    }
                }

                dragExit(component);

                return dropAction;
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
