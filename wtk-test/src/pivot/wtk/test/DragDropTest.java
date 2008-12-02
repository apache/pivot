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
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.Dimensions;
import pivot.wtk.Display;
import pivot.wtk.DragSource;
import pivot.wtk.DropAction;
import pivot.wtk.DropTarget;
import pivot.wtk.Frame;
import pivot.wtk.ImageView;
import pivot.wtk.Mouse;
import pivot.wtk.Visual;
import pivot.wtk.media.Image;

public class DragDropTest implements Application {
    private Frame frame1 = new Frame();
    private Frame frame2 = new Frame();

    private static final Color IMAGE_VIEW_BACKGROUND_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color IMAGE_VIEW_DROP_HIGHLIGHT_COLOR = new Color(0xf0, 0xe6, 0x8c);

    private static class ImageDragHandler implements DragSource {
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

        public Class<?> getContentType() {
            return image.getClass();
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

    private static class ImageDropHandler implements DropTarget {
        public DropAction getDropAction(Component component, Class<?> contentType,
            int supportedDropActions, int x, int y) {
            DropAction dropAction = null;

            if (Image.class.isAssignableFrom(contentType)
                && DropAction.MOVE.isSelected(supportedDropActions)) {
                dropAction = DropAction.MOVE;
            }

            return dropAction;
        }

        public void drop(Component component, Object content, int x, int y) {
            ImageView imageView = (ImageView)component;
            imageView.setImage((Image)content);
            imageView.getStyles().put("backgroundColor", IMAGE_VIEW_BACKGROUND_COLOR);
        }
    }

    private static class ImageMouseHandler implements ComponentMouseListener {
        public boolean mouseMove(Component component, int x, int y) {
            return false;
        }

        public void mouseOver(Component component) {
            Class<?> dragContentType = Mouse.getDragContentType();

            if (dragContentType != null
                && Image.class.isAssignableFrom(dragContentType)) {
                component.getStyles().put("backgroundColor", IMAGE_VIEW_DROP_HIGHLIGHT_COLOR);
            }
        }

        public void mouseOut(Component component) {
            component.getStyles().put("backgroundColor", IMAGE_VIEW_BACKGROUND_COLOR);
        }
    }

    public void startup(Display display, Dictionary<String, String> properties) throws Exception {
        frame1.setTitle("Frame 1");
        frame1.setPreferredSize(160, 120);
        frame1.getStyles().put("resizable", false);

        ImageDragHandler imageDragHandler = new ImageDragHandler();
        ImageDropHandler imageDropHandler = new ImageDropHandler();
        ImageMouseHandler imageMouseHandler = new ImageMouseHandler();

        ImageView imageView1 = new ImageView();
        imageView1.setImage(Image.load(getClass().getResource("go-home.png")));
        imageView1.setDragSource(imageDragHandler);
        imageView1.setDropTarget(imageDropHandler);
        imageView1.getComponentMouseListeners().add(imageMouseHandler);
        imageView1.getStyles().put("backgroundColor", IMAGE_VIEW_BACKGROUND_COLOR);
        frame1.setContent(imageView1);
        frame1.open(display);

        frame2.setTitle("Frame 2");
        frame2.setPreferredSize(160, 120);
        frame2.setLocation(180, 0);

        ImageView imageView2 = new ImageView();
        imageView2.setDragSource(imageDragHandler);
        imageView2.setDropTarget(imageDropHandler);
        imageView2.getComponentMouseListeners().add(imageMouseHandler);
        imageView2.getStyles().put("backgroundColor", IMAGE_VIEW_BACKGROUND_COLOR);
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
