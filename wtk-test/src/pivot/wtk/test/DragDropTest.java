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
package pivot.wtk.test;

import java.awt.Color;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Display;
import pivot.wtk.Frame;
import pivot.wtk.ImageView;
import pivot.wtk.media.Image;

public class DragDropTest implements Application {
    private Frame frame1 = new Frame();
    private Frame frame2 = new Frame();

    private static final Color IMAGE_VIEW_BACKGROUND_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color IMAGE_VIEW_DROP_HIGHLIGHT_COLOR = new Color(0xf0, 0xe6, 0x8c);

    public void startup(Display display, Dictionary<String, String> properties) throws Exception {
        frame1.setTitle("Frame 1");
        frame1.setPreferredSize(160, 120);
        frame1.getStyles().put("resizable", false);

        ImageView imageView1 = new ImageView();
        imageView1.setImage(Image.load(getClass().getResource("go-home.png")));
        imageView1.getStyles().put("backgroundColor", IMAGE_VIEW_BACKGROUND_COLOR);
        frame1.setContent(imageView1);
        frame1.open(display);

        frame2.setTitle("Frame 2");
        frame2.setPreferredSize(160, 120);
        frame2.setLocation(180, 0);

        ImageView imageView2 = new ImageView();
        imageView2.getStyles().put("backgroundColor", IMAGE_VIEW_BACKGROUND_COLOR);
        frame2.setContent(imageView2);

        frame2.open(display);
    }

    public boolean shutdown(boolean optional) {
        frame1.close();
        frame2.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
