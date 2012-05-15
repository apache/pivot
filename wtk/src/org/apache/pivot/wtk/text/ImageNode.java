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
package org.apache.pivot.wtk.text;

import java.net.URL;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.media.Image;

/**
 * Node representing an image.
 */
public class ImageNode extends Node {
    private static class ImageNodeListenerList extends ListenerList<ImageNodeListener>
        implements ImageNodeListener {
        @Override
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

    /**
     * Sets the image node's current image by URL.
     * <p>
     * If the icon already exists in the application context resource cache,
     * the cached value will be used. Otherwise, the icon will be loaded
     * synchronously and added to the cache.
     *
     * @param imageURL
     * The location of the image to set.
     */
    public void setImage(URL imageURL) {
        if (imageURL == null) {
            throw new IllegalArgumentException("imageURL is null.");
        }

        Image imageLocal = (Image)ApplicationContext.getResourceCache().get(imageURL);

        if (imageLocal == null) {
            try {
                imageLocal = Image.load(imageURL);
            } catch (TaskExecutionException exception) {
                throw new IllegalArgumentException(exception);
            }

            ApplicationContext.getResourceCache().put(imageURL, imageLocal);
        }

        setImage(imageLocal);
    }

    /**
     * Sets the image node's icon by {@linkplain ClassLoader#getResource(String)
     * resource name}.
     *
     * @param imageName
     * The resource name of the image to set.
     *
     * @see #setImage(URL)
     */
    public void setImage(String imageName) {
        if (imageName == null) {
            throw new IllegalArgumentException("imageName is null.");
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(imageName.substring(1));
        if (url == null) {
            throw new IllegalArgumentException("cannot find image resource " + imageName);
        }
        setImage(url);
    }

    @Override
    public char getCharacterAt(int offset) {
        return 0x00;
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
