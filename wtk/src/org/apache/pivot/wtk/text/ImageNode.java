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
 * <p>
 * TODO Add a URL setter for the image property.
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
     * <b>Note</b>: Using this signature will cause an entry to be added in the
     * application context's {@linkplain ApplicationContext#getResourceCache()
     * resource cache} if one does not already exist.
     *
     * @param imageURL
     * The location of the image to set.
     */
    public void setImage(URL imageURL) {
        if (imageURL == null) {
            throw new IllegalArgumentException("imageURL is null.");
        }

        Image image = (Image)ApplicationContext.getResourceCache().get(imageURL);

        if (image == null) {
            try {
                image = Image.load(imageURL);
            } catch (TaskExecutionException exception) {
                throw new IllegalArgumentException(exception);
            }

            ApplicationContext.getResourceCache().put(imageURL, image);
        }

        setImage(image);
    }

    /**
     * Sets the image node's icon by {@linkplain ClassLoader#getResource(String)
     * resource name}.
     * <p>
     * <b>Note</b>: Using this signature will cause an entry to be added in the
     * application context's {@linkplain ApplicationContext#getResourceCache()
     * resource cache} if one does not already exist.
     *
     * @param image
     * The resource name of the image to set.
     */
    public void setImage(String image) {
        if (image == null) {
            throw new IllegalArgumentException("image is null.");
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        setImage(classLoader.getResource(image));
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
