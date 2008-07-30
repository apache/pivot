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
package pivot.wtk;

import pivot.util.ListenerList;
import pivot.wtk.media.Image;

@ComponentInfo(icon="ImageView.png")
public class ImageView extends Component {
    private class ImageViewListenerList extends ListenerList<ImageViewListener>
        implements ImageViewListener {
        public void imageChanged(ImageView imageView, Image previousImage) {
            for (ImageViewListener listener : this) {
                listener.imageChanged(imageView, previousImage);
            }
        }
    }

    private Image image = null;

    private ImageViewListenerList imageViewListeners = new ImageViewListenerList();

    public ImageView() {
        this(null);
    }

    public ImageView(Image image) {
        setImage(image);

        installSkin(ImageView.class);
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        Image previousImage = this.image;

        if (previousImage != image) {
            this.image = image;
            imageViewListeners.imageChanged(this, previousImage);
        }
    }

    public ListenerList<ImageViewListener> getImageViewListeners() {
        return imageViewListeners;
    }
}
