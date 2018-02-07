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
package org.apache.pivot.tests;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.TextDecoration;

public class LabelTest implements Application {
    private Frame frame = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        frame = new Frame();
        frame.setTitle("Label Test");

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
            + "one and one is all:\nto be a rock and not to roll.";

        BoxPane boxPane = new BoxPane(Orientation.VERTICAL);

        Label label1 = new Label(line1);
        label1.getStyles().put(Style.wrapText, true);
        label1.getStyles().put(Style.horizontalAlignment, HorizontalAlignment.LEFT);
        boxPane.add(label1);

        Label label2 = new Label(line2); // strikethrough
        label2.getStyles().put(Style.wrapText, true);
        label2.getStyles().put(Style.horizontalAlignment, HorizontalAlignment.LEFT);
        label2.getStyles().put(Style.textDecoration, TextDecoration.STRIKETHROUGH);
        boxPane.add(label2);

        Label label3 = new Label(line2); // disabled
        label3.getStyles().put(Style.wrapText, true);
        label3.getStyles().put(Style.horizontalAlignment, HorizontalAlignment.LEFT);
        label3.setEnabled(false);
        boxPane.add(label3);

        boxPane.getStyles().put(Style.fill, true);
        boxPane.getStyles().put(Style.padding, new Insets(10));

        frame.setContent(boxPane);
        frame.setPreferredSize(340, 400);

        frame.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (frame != null) {
            frame.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(LabelTest.class, args);
    }
}
