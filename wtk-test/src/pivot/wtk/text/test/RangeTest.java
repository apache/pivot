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

import pivot.wtk.text.Document;
import pivot.wtk.text.Node;
import pivot.wtk.text.Paragraph;
import pivot.wtk.text.PlainTextSerializer;

public class RangeTest {
    public static void main(String[] args) {
        Document document = new Document();
        document.add(new Paragraph("Hello, World!"));
        document.add(new Paragraph("ABCDE"));
        document.add(new Paragraph("1234"));

        PlainTextSerializer serializer = new PlainTextSerializer();

        try {
            serializer.writeObject(document, System.out);

            Node range = document.getRange(3, 14);
            serializer.writeObject(range, System.out);
        } catch(Exception exception) {
            System.out.println(exception);
        }
    }
}
