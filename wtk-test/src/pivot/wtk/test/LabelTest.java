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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Decorator;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Label;
import pivot.wtk.FlowPane;
import pivot.wtk.Insets;
import pivot.wtk.Orientation;
import pivot.wtk.TextDecoration;
import pivot.wtk.Window;

public class LabelTest implements Application {
    private Window window = null;

    public void startup() throws Exception {
        window = new Window();
        window.setTitle("Label Test");

        String line1 = "There's a lady who's sure all that glitters is gold, and "
            + "she's buying a stairway to heaven. When she gets there she knows, "
            + "if the stores are closed, with a word she can get what she came "
            + "for. Woe oh oh oh oh oh and she's buying a stairway to heaven. "
            + "There's a sign on the wall, but she wants to be sure, and you know "
            + "sometimes words have two meanings. In a tree by the brook there's "
            + "a songbird who sings, sometimes all of our thoughts are misgiven. "
            + "Woe oh oh oh oh oh and she's buying a stairway to heaven.";
        String line2 = "And as we wind on down the road, our shadows taller than "
            + "our souls, there walks a lady we all know who shines white light "
            + "and wants to show how everything still turns to gold; and if you "
            + "listen very hard the tune will come to you at last when all are "
            + "one and one is all: to be a rock and not to roll.";

        FlowPane flowPane = new FlowPane(Orientation.HORIZONTAL);
        flowPane.setDecorator(new Decorator() {
            Graphics2D graphics = null;

            public Graphics2D prepare(Component component, Graphics2D graphics) {
                this.graphics = graphics;

                graphics = (Graphics2D)graphics.create();
                graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                graphics.scale(0.5f, 0.5f);

                return graphics;
            }

            public void update() {
                graphics.setColor(Color.RED);
                graphics.fillRect(0, 0, 10, 10);
            }
        });

        Label label1 = new Label(line1);
        label1.getStyles().put("wrapText", true);
        label1.getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
        flowPane.getComponents().add(label1);

        Label label2 = new Label(line2);
        label2.getStyles().put("wrapText", true);
        label2.getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
        label2.getStyles().put("textDecoration", TextDecoration.UNDERLINE);
        flowPane.getComponents().add(label2);

        flowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.JUSTIFY);
        flowPane.getStyles().put("padding", new Insets(10));

        window.setContent(flowPane);

        window.setPreferredWidth(200);
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
