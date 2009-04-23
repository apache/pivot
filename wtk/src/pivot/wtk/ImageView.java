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
package pivot.wtk;

import java.net.URL;

import pivot.util.ListenerList;
import pivot.util.ThreadUtilities;
import pivot.wtk.media.Image;

/**
 * Component that displays an image.
 * <p>
 * TODO Load images asynchronously in setImage()?
 *
 * @author gbrown
 */
public class ImageView extends Component {
    private static class ImageViewListenerList extends ListenerList<ImageViewListener>
        implements ImageViewListener {
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

        installSkin(ImageView.class);
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
     *
     * @param image
     * The location of the image to set.
     */
    public void setImage(URL image) {
        if (image == null) {
            throw new IllegalArgumentException("image is null.");
        }

        setImage(Image.load(image));
    }

    /**
     * Sets the image view's current image by resource name.
     *
     * @param image
     * The resource name of the image to set.
     */
    public void setImage(String image) {
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
