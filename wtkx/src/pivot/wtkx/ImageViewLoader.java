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
package pivot.wtkx;

import org.w3c.dom.Element;
import pivot.wtk.Component;
import pivot.wtk.ImageView;
import pivot.wtk.media.Image;

/**
 * TODO Add an asynchronous attribute to control how images are loaded?
 *
 * @author gbrown
 */
class ImageViewLoader extends Loader {
    public static final String IMAGE_VIEW_TAG = "ImageView";
    public static final String SRC_ATTRIBUTE = "src";

    protected Component load(Element element, ComponentLoader rootLoader)
        throws LoadException {
        ImageView imageView = new ImageView();
        if (element.hasAttribute(SRC_ATTRIBUTE)) {
            String src = element.getAttribute(SRC_ATTRIBUTE);
            imageView.setImage(Image.load(rootLoader.getResource(src)));
        }

        return imageView;
    }
}
