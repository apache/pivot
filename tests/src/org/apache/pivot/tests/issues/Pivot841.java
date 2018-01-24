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
package org.apache.pivot.tests.issues;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextArea.Paragraph;
import org.apache.pivot.wtk.TextArea.ParagraphListener;
import org.apache.pivot.wtk.TextAreaContentListener;
import org.apache.pivot.wtk.Window;

public class Pivot841 implements Application {

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        TextArea textArea = new TextArea();
        textArea.setText("abcxyz");

        final ParagraphListener paragraphListener = new ParagraphListener.Adapter() {
            @Override
            public void textInserted(Paragraph paragraph, int index, int count) {
                System.out.println("Text inserted\n\tparagraph content: '"
                    + paragraph.getCharacters() + "" + "'\n\tindex: " + index + "\n\tcount: "
                    + count);
            }

            @Override
            public void textRemoved(Paragraph paragraph, int index, int count) {
                System.out.println("Text removed\n\tparagraph content: '"
                    + paragraph.getCharacters() + "'\n\tindex: " + index + "\n\tcount: " + count);
            }
        };

        textArea.getParagraphs().get(0).getParagraphListeners().add(paragraphListener);
        textArea.getTextAreaContentListeners().add(new TextAreaContentListener() {
            @Override
            public void paragraphInserted(TextArea textAreaArgument, int index) {
                Paragraph paragraph = textAreaArgument.getParagraphs().get(index);
                System.out.println("Paragraph inserted\n\tparagraph content: '"
                    + paragraph.getCharacters() + "'\n\tindex: " + index);

                paragraph.getParagraphListeners().add(paragraphListener);
            }
        });

        Window window = new Window(textArea);
        window.open(display);
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Pivot841.class, args);
    }

}
