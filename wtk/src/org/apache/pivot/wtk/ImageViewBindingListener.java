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

import org.apache.pivot.util.ListenerList;

/**
 * Image view binding listener.
 */
public interface ImageViewBindingListener {
    /**
     * Image view binding listeners.
     */
    public static class Listeners extends ListenerList<ImageViewBindingListener>
        implements ImageViewBindingListener {
        @Override
        public void imageKeyChanged(ImageView imageView, String previousImageKey) {
            forEach(listener -> listener.imageKeyChanged(imageView, previousImageKey));
        }

        @Override
        public void imageBindTypeChanged(ImageView imageView, BindType previousImageBindType) {
            forEach(listener -> listener.imageBindTypeChanged(imageView, previousImageBindType));
        }

        @Override
        public void imageBindMappingChanged(ImageView imageView,
            ImageView.ImageBindMapping previousImageBindMapping) {
            forEach(listener -> listener.imageBindMappingChanged(imageView, previousImageBindMapping));
        }
    }

    /**
     * Image view binding listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements ImageViewBindingListener {
        @Override
        public void imageKeyChanged(ImageView imageView, String previousImageKey) {
            // empty block
        }

        @Override
        public void imageBindTypeChanged(ImageView imageView, BindType previousImageBindType) {
            // empty block
        }

        @Override
        public void imageBindMappingChanged(ImageView imageView,
            ImageView.ImageBindMapping previousImageBindMapping) {
            // empty block
        }
    }

    /**
     * Called when an image view's image key has changed.
     *
     * @param imageView        The image view whose binding has changed.
     * @param previousImageKey The previous binding key for the image.
     */
    public void imageKeyChanged(ImageView imageView, String previousImageKey);

    /**
     * Called when a image views's image bind type has changed.
     *
     * @param imageView             The image view whose binding has changed.
     * @param previousImageBindType The previous bind type for the image.
     */
    public void imageBindTypeChanged(ImageView imageView, BindType previousImageBindType);

    /**
     * Called when an image view's bind mapping has changed.
     *
     * @param imageView                The image view whose binding has changed.
     * @param previousImageBindMapping The previous bind mapping for the image.
     */
    public void imageBindMappingChanged(ImageView imageView,
        ImageView.ImageBindMapping previousImageBindMapping);
}
