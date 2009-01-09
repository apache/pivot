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
import pivot.wtk.text.Element;
import pivot.wtk.text.Node;
import pivot.wtk.text.NodeListener;
import pivot.wtk.text.Paragraph;
import pivot.wtk.text.PlainTextSerializer;

public class RangeTest {
    private static Document document = null;

    public static void main(String[] args) {
        document = new Document();
        document.getNodeListeners().add(new NodeListener() {
            public void parentChanged(Node node, Element previousParent) {
            }

            public void offsetChanged(Node node, int previousOffset) {
            }

            public void rangeInserted(Node node, Node range, int offset) {
                System.out.println(range.getClass().getName() + "("
                    + range.getCharacterCount() + ") inserted at " + offset);
                dumpRange(0, document.getCharacterCount());
            }

            public void rangeRemoved(Node node, int offset, Node range) {
                System.out.println(range.getClass().getName() + " ("
                    + range.getCharacterCount() + ") removed at " + offset);
                dumpRange(0, document.getCharacterCount());
            }
        });

        document.add(new Paragraph("ABCDE"));
        document.add(new Paragraph("FGH"));
        document.add(new Paragraph("IJKLMNO"));
        document.add(new Paragraph("PQRS"));
        document.add(new Paragraph("TUVWX"));
        document.add(new Paragraph("YZ"));
        document.dumpOffsets();

        dumpIndexAt(2);
        dumpIndexAt(14);
        dumpIndexAt(25);

        dumpRange(1, 1);
        dumpRange(1, 4);
        dumpRange(3, 7);
        dumpRange(4, 2);

        document.removeRange(1, 3);

        Document range = new Document();
        range.add(new Paragraph("123"));

        document.insertRange(range, 1);

        document.removeRange(0, 6);

        document.insert(new Paragraph("00101001"), 3);

        document.remove(2, 2);

        document.dumpOffsets();

        // TODO Test getDescendantAt() and getPathAt() methods

        // TODO Test normalize() method
    }

    public static void dumpIndexAt(int offset) {
        System.out.println("Index at " + offset + ": " + document.getIndexAt(offset) + "\n");
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
