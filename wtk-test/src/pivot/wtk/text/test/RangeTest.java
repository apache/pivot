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
        document.add(new Paragraph("PQRS"));
        document.add(new Paragraph("TUVWX"));
        document.add(new Paragraph("YZ"));
        dumpRange(0, document.getCharacterCount());
        document.dumpOffsets();

        dumpIndexAt(2);
        dumpIndexAt(17);
        dumpIndexAt(31);

        dumpRange(1, 1);
        dumpRange(1, 6);
        dumpRange(3, 9);
        dumpRange(4, 3);

        document.removeRange(1, 12);
        dumpRange(0, document.getCharacterCount());
        document.dumpOffsets();

        Document range = new Document();
        range.add(new Paragraph("123"));

        document.insertRange(range, 4);
        dumpRange(0, document.getCharacterCount());
        document.dumpOffsets();

        document.removeRange(0, 6);
        dumpRange(0, document.getCharacterCount());
        document.dumpOffsets();

        document.insert(new Paragraph("00101001"), 3);
        dumpRange(0, document.getCharacterCount());
        document.dumpOffsets();

        document.remove(2, 2);
        dumpRange(0, document.getCharacterCount());
        document.dumpOffsets();

        dumpPathAt(0);
        dumpPathAt(2);
        dumpPathAt(3);
        dumpPathAt(5);
        dumpPathAt(7);

        dumpDescendantAt(0);
        dumpDescendantAt(2);
        dumpDescendantAt(3);
        dumpDescendantAt(5);
        dumpDescendantAt(7);
    }

    public static void dumpIndexAt(int offset) {
        System.out.println("Index at " + offset + ": " + document.getIndexAt(offset));
    }

    public static void dumpPathAt(int offset) {
        System.out.println("Path at " + offset + ": " + document.getPathAt(offset));
    }

    public static void dumpDescendantAt(int offset) {
        System.out.println("Descendant at " + offset + ": " + document.getDescendantAt(offset));
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
