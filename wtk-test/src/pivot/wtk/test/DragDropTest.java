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
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.Dimensions;
import pivot.wtk.DragDropManager;
import pivot.wtk.DragHandler;
import pivot.wtk.DropAction;
import pivot.wtk.DropHandler;
import pivot.wtk.Frame;
import pivot.wtk.ImageView;
import pivot.wtk.Visual;
import pivot.wtk.media.Image;

public class DragDropTest implements Application {
    private Frame frame1 = new Frame();
    private Frame frame2 = new Frame();

    private static final Color IMAGE_VIEW_BACKGROUND_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color IMAGE_VIEW_DROP_HIGHLIGHT_COLOR = new Color(0xf0, 0xe6, 0x8c);

    private static class ImageDragHandler implements DragHandler {
        ImageView imageView = null;
        private Image image = null;
        private Dimensions offset = null;

        public boolean beginDrag(Component component, int x, int y) {
            imageView = (ImageView)component;
            image = imageView.getImage();

            if (image != null) {
                imageView.setImage((Image)null);
                offset = new Dimensions(x - (imageView.getWidth() - image.getWidth()) / 2,
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

        public Dimensions getOffset() {
            return offset;
        }

        public int getSupportedDropActions() {
            return DropAction.MOVE.getMask();
        }
    }

    private static class ImageDropHandler implements DropHandler {
        public DropAction drop(Component component, int x, int y) {
            DropAction dropAction = null;

            Object dragContent = DragDropManager.getInstance().getContent();
            if (dragContent instanceof Image) {
                ImageView imageView = (ImageView)component;
                imageView.setImage((Image)dragContent);
                imageView.getStyles().put("backgroundColor", IMAGE_VIEW_BACKGROUND_COLOR);
                dropAction = DropAction.MOVE;
            }

            return dropAction;
        }
    }

    private static class ImageMouseHandler implements ComponentMouseListener {
        public void mouseMove(Component component, int x, int y) {
            // No-op
        }

        public void mouseOver(Component component) {
            DragDropManager dragDropManager = DragDropManager.getInstance();
            if (dragDropManager.isActive()) {
                Object dragContent = dragDropManager.getContent();
                if (dragContent instanceof Image) {
                    component.getStyles().put("backgroundColor", IMAGE_VIEW_DROP_HIGHLIGHT_COLOR);
                }
            }
        }

        public void mouseOut(Component component) {
            component.getStyles().put("backgroundColor", IMAGE_VIEW_BACKGROUND_COLOR);
        }
    }

    public void startup() throws Exception {
        frame1.setTitle("Frame 1");
        frame1.setPreferredSize(160, 120);
        frame1.getStyles().put("resizable", false);

        ImageDragHandler imageDragHandler = new ImageDragHandler();
        ImageDropHandler imageDropHandler = new ImageDropHandler();
        ImageMouseHandler imageMouseHandler = new ImageMouseHandler();

        ImageView imageView1 = new ImageView();
        imageView1.setImage(Image.load(getClass().getResource("go-home.png")));
        imageView1.setDragHandler(imageDragHandler);
        imageView1.setDropHandler(imageDropHandler);
        imageView1.getComponentMouseListeners().add(imageMouseHandler);
        imageView1.getStyles().put("backgroundColor", IMAGE_VIEW_BACKGROUND_COLOR);
        frame1.setContent(imageView1);
        frame1.open();

        frame2.setTitle("Frame 2");
        frame2.setPreferredSize(160, 120);
        frame2.setLocation(180, 0);

        ImageView imageView2 = new ImageView();
        imageView2.setDragHandler(imageDragHandler);
        imageView2.setDropHandler(imageDropHandler);
        imageView2.getComponentMouseListeners().add(imageMouseHandler);
        imageView2.getStyles().put("backgroundColor", IMAGE_VIEW_BACKGROUND_COLOR);
        frame2.setContent(imageView2);

        frame2.open();
    }

    public void shutdown() throws Exception {
        frame1.close();
        frame2.close();
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }
}
