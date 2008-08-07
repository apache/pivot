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
package pivot.tutorials.lists;

import java.net.URL;
import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.ImageView;
import pivot.wtk.ListButton;
import pivot.wtk.ListButtonSelectionListener;
import pivot.wtk.Window;
import pivot.wtk.media.Image;
import pivot.wtkx.WTKXSerializer;

public class ListButtons implements Application {
    private class ListButtonSelectionHandler
        implements ListButtonSelectionListener {
        @SuppressWarnings("unchecked")
        public void selectedIndexChanged(ListButton listButton, int previousIndex) {
            int index = listButton.getSelectedIndex();

            if (index != -1) {
                Object item = listButton.getListData().get(index);
                Dictionary<String, Object> dictionary =
                    (Dictionary<String, Object>)item;

                // Get the image URL for the selected item
                String imageName = (String)dictionary.get("imageName");
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                URL imageURL = classLoader.getResource((String)imageName);

                ApplicationContext applicationContext =
                    ApplicationContext.getInstance();

                // If the image has not been added to the resource cache yet,
                // add it
                Image image =
                    (Image)applicationContext.getResources().get(imageURL);

                if (image == null) {
                    image = Image.load(imageURL);
                    applicationContext.getResources().put(imageURL, image);
                }

                // Update the image
                imageView.setImage(image);
            }
        }

    }

    private ImageView imageView = null;
    private Window window = null;

    public void startup() throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        Component content =
            (Component)wtkxSerializer.readObject("pivot/tutorials/lists/list_buttons.wtkx");

        imageView = (ImageView)wtkxSerializer.getObjectByName("imageView");

        ListButton listButton =
            (ListButton)wtkxSerializer.getObjectByName("listButton");

        listButton.getListButtonSelectionListeners().add(new
            ListButtonSelectionHandler());

        listButton.setSelectedIndex(0);

        window = new Window();
        window.setContent(content);
        window.setMaximized(true);
        window.open();
    }

    public void shutdown() throws Exception {
        window.close();
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }
}
