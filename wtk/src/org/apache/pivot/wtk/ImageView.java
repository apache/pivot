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
package org.apache.pivot.wtk;

import java.net.URL;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.ThreadUtilities;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.media.Image;


/**
 * Component that displays an image.
 */
public class ImageView extends Component {
    private static class ImageViewListenerList extends ListenerList<ImageViewListener>
        implements ImageViewListener {
        @Override
        public void imageChanged(ImageView imageView, Image previousImage) {
            for (ImageViewListener listener : this) {
                listener.imageChanged(imageView, previousImage);
            }
        }
    }

    private Image image = null;

    private ImageViewListenerList imageViewListeners = new ImageViewListenerList();

    /**
     * Creates an empty image view.
     */
    public ImageView() {
        this(null);
    }

    /**
     * Creates an image view with the given image.
     *
     * @param image
     * The initial image to set, or <tt>null</tt> for no image.
     */
    public ImageView(Image image) {
        setImage(image);

        installThemeSkin(ImageView.class);
    }

    /**
     * Returns the image view's current image.
     *
     * @return
     * The current image, or <tt>null</tt> if no image is set.
     */
    public Image getImage() {
        return image;
    }

    /**
     * Sets the image view's current image.
     *
     * @param image
     * The image to set, or <tt>null</tt> for no image.
     */
    public void setImage(Image image) {
        Image previousImage = this.image;

        if (previousImage != image) {
            this.image = image;
            imageViewListeners.imageChanged(this, previousImage);
        }
    }

    /**
     * Sets the image view's current image by URL.
     * <p>
     * <b>Note</b>: Using this signature will cause an entry to be added in the
     * application context's {@linkplain ApplicationContext#getResourceCache()
     * resource cache} if one does not already exist.
     *
     * @param imageURL
     * The location of the image to set.
     */
    public final void setImage(URL imageURL) {
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
     * Sets the image view's icon by {@linkplain ClassLoader#getResource(String)
     * resource name}.
     * <p>
     * <b>Note</b>: Using this signature will cause an entry to be added in the
     * application context's {@linkplain ApplicationContext#getResourceCache()
     * resource cache} if one does not already exist.
     *
     * @param image
     * The resource name of the image to set.
     */
    public final void setImage(String image) {
        if (image == null) {
            throw new IllegalArgumentException("image is null.");
        }

        ClassLoader classLoader = ThreadUtilities.getClassLoader();
        setImage(classLoader.getResource(image));
    }

    /**
     * Returns the image view listener list.
     *
     * @return
     * The image view listener list.
     */
    public ListenerList<ImageViewListener> getImageViewListeners() {
        return imageViewListeners;
    }
}
