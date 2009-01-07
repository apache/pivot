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
import pivot.wtk.text.Paragraph;
import pivot.wtk.text.PlainTextSerializer;

public class RangeTest {
    private static Document document = null;

    public static void main(String[] args) {
        document = new Document();
        document.add(new Paragraph("ABCDE"));
        document.add(new Paragraph("FGH"));
        document.add(new Paragraph("IJKLMNO"));

        dumpRange(1, 1);
        dumpRange(1, 4);
        dumpRange(3, 7);
        dumpRange(4, 2);

        System.out.println(document.getIndexAt(2));
        System.out.println(document.getIndexAt(14));

        // TODO Test removal of spanning ranges
        dumpRange(0, 6);
        document.removeRange(1, 3);
        dumpRange(0, 6);

        Document range = new Document();
        range.add(new Paragraph("123"));
        document.insertRange(range, 1);
        dumpRange(0, 6);
    }

    public static void dumpRange(int offset, int characterCount) {
        PlainTextSerializer serializer  = new PlainTextSerializer();

        System.out.println("Range " + offset + ":" + characterCount);

        try {
            serializer.writeObject(document.getRange(offset, characterCount), System.out);
        } catch(Exception exception) {
            System.out.println(exception);
        }

        System.out.println();
    }
}
