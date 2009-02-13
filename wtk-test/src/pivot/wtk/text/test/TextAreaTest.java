/*
 * Copyright (c) 2009 VMware, Inc.
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
package pivot.wtk.text.test;

import java.io.InputStream;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Border;
import pivot.wtk.Display;
import pivot.wtk.Frame;
import pivot.wtk.ScrollPane;
import pivot.wtk.TextArea;
import pivot.wtk.text.Document;
import pivot.wtk.text.ImageNode;
import pivot.wtk.text.Paragraph;
import pivot.wtk.text.PlainTextSerializer;
import pivot.wtk.text.TextNode;

public class TextAreaTest implements Application {
    private Frame frame = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        PlainTextSerializer serializer = new PlainTextSerializer("UTF-8");
        InputStream inputStream = PlainTextSerializerTest.class.getResourceAsStream("pivot.txt");

        Document document = null;
        try {
            document = (Document)serializer.readObject(inputStream);
        } catch(Exception exception) {
            System.out.println(exception);
        }

        document = new Document();

        Paragraph p1 = new Paragraph();
        TextNode t1 = new TextNode("ABCD");
        p1.add(t1);
        document.add(p1);

        Paragraph p2 = new Paragraph();
        TextNode t2 = new TextNode("");
        p2.add(t2);
        document.add(p2);

        Paragraph p3 = new Paragraph();
        ImageNode i3 = new ImageNode("pivot/wtk/text/test/IMG_0767_2.jpg");
        p3.add(i3);
        document.add(p3);

        Paragraph p4 = new Paragraph();
        TextNode t4 = new TextNode("1234");
        p4.add(t4);
        document.add(p4);

        System.out.println(p2.getOffset());
        System.out.println(p3.getOffset());

        TextArea textArea = new TextArea();
        textArea.setDocument(document);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPane.ScrollBarPolicy.FILL);
        scrollPane.setVerticalScrollBarPolicy(ScrollPane.ScrollBarPolicy.FILL_TO_CAPACITY);
        scrollPane.setView(textArea);

        Border border = new Border();
        border.getStyles().put("padding", 0);
        border.setContent(scrollPane);

        frame = new Frame(border);
        frame.setTitle("Test");
        frame.setPreferredSize(640, 480);
        frame.open(display);

        t2.insertText('a', 0);
        t2.insertText('b', 1);
        t2.insertText('c', 2);
    }

    public boolean shutdown(boolean optional) {
        frame.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
