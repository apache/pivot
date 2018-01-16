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

import org.apache.pivot.util.ImageUtils;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.media.Image;

/**
 * Node representing an image to be inserted into a {@link TextPane}.
 */
public class ImageNode extends Block {
    private Image image = null;

    private ImageNodeListener.Listeners imageNodeListeners = new ImageNodeListener.Listeners();

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
     * Sets the image node's current image by URL. <p> If the icon already
     * exists in the application context resource cache, the cached value will
     * be used. Otherwise, the icon will be loaded synchronously and added to
     * the cache.
     *
     * @param imageURL The location of the image to set.
     */
    public void setImage(URL imageURL) {
        setImage(Image.loadFromCache(imageURL));
    }

    /**
     * Sets the image node's icon by
     * {@linkplain ClassLoader#getResource(String) resource name}.
     *
     * @param imageName The resource name of the image to set.
     * @see #setImage(URL)
     * @see ImageUtils#findByName(String)
     */
    public void setImage(String imageName) {
        setImage(ImageUtils.findByName(imageName));
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
    public Element getRange(int offset, int characterCount) {
        Utils.checkIndexBounds(offset, 0, 1);

        if (characterCount != 1) {
            throw new IllegalArgumentException("Invalid characterCount.");
        }

        return new ImageNode(this);
    }

    @Override
    public Element duplicate(boolean recursive) {
        return new ImageNode(this);
    }

    public ListenerList<ImageNodeListener> getImageNodeListeners() {
        return imageNodeListeners;
    }
}
