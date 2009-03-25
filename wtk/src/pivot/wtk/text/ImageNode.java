/*
 * Copyright (c) 2009 VMware, Inc.
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
package pivot.wtk.text;

import java.net.URL;

import pivot.util.ListenerList;
import pivot.wtk.media.Image;

/**
 * Node representing an image.
 * <p>
 * TODO Add a URL setter for the image property.
 *
 * @author gbrown
 */
public class ImageNode extends Node {
    private static class ImageNodeListenerList extends ListenerList<ImageNodeListener>
        implements ImageNodeListener {
        public void imageChanged(ImageNode imageNode, Image previousImage) {
            for (ImageNodeListener listener : this) {
                listener.imageChanged(imageNode, previousImage);
            }
        }
    }

    private Image image = null;

    private ImageNodeListenerList imageNodeListeners = new ImageNodeListenerList();

    public ImageNode() {
    }

    public ImageNode(ImageNode imageNode) {
        setImage(imageNode.getImage());
    }

    public ImageNode(Image image) {
        setImage(image);
    }

    public ImageNode(URL image) {
        setImage(image);
    }

    public ImageNode(String image) {
        setImage(image);
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        Image previousImage = this.image;

        if (previousImage != image) {
            this.image = image;
            imageNodeListeners.imageChanged(this, previousImage);
        }
    }

    public void setImage(URL image) {
        if (image == null) {
            throw new IllegalArgumentException("image is null.");
        }

        setImage(Image.load(image));
    }

    public void setImage(String image) {
        if (image == null) {
            throw new IllegalArgumentException("image is null.");
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        setImage(classLoader.getResource(image));
    }

    @Override
    public int getCharacterCount() {
        return 1;
    }

    @Override
    public void insertRange(Node range, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node removeRange(int offset, int span) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getRange(int offset, int characterCount) {
        if (offset < 0
            || offset > 1) {
            throw new IndexOutOfBoundsException();
        }

        if (characterCount != 1) {
            throw new IllegalArgumentException("Invalid characterCount.");
        }

        return new ImageNode(this);
    }

    @Override
    public Node duplicate(boolean recursive) {
        return new ImageNode(this);
    }

    public ListenerList<ImageNodeListener> getImageNodeListeners() {
        return imageNodeListeners;
    }
}
