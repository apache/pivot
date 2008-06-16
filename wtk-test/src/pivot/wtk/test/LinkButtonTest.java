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

import pivot.wtk.Application;
import pivot.wtk.FlowPane;
import pivot.wtk.LinkButton;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.Window;
import pivot.wtk.content.ButtonData;
import pivot.wtk.media.Image;

public class LinkButtonTest implements Application {
    private Window window = new Window();

    public void startup() throws Exception {
        FlowPane flowPane = new FlowPane();
        flowPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        flowPane.getStyles().put("spacing", 8);

        Image image = Image.load(getClass().getResource("go-home.png"));

        LinkButton linkButton = null;

        linkButton = new LinkButton("ABCDE");
        flowPane.getComponents().add(linkButton);

        linkButton = new LinkButton(image);
        flowPane.getComponents().add(linkButton);

        linkButton = new LinkButton(new ButtonData(image, "12345"));
        flowPane.getComponents().add(linkButton);

        window.setContent(flowPane);
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
