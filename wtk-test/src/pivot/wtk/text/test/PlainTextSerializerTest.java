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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import pivot.wtk.text.Document;
import pivot.wtk.text.Paragraph;
import pivot.wtk.text.PlainTextSerializer;

public class PlainTextSerializerTest {
    public static void main(String[] args) {
        test1();
        test2();
    }

    public static void test1() {
        Document document = new Document();
        document.add(new Paragraph("Hello, World!"));
        document.add(new Paragraph("ABC"));
        document.add(new Paragraph("123"));

        PlainTextSerializer serializer = new PlainTextSerializer();

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            serializer.writeObject(document, byteArrayOutputStream);
            byteArrayOutputStream.close();

            byte[] data = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            document = (Document)serializer.readObject(byteArrayInputStream);

            serializer.writeObject(document, System.out);
        } catch(Exception exception) {
            System.out.println(exception);
        }
    }

    public static void test2() {
        PlainTextSerializer serializer = new PlainTextSerializer("UTF-8");
        InputStream inputStream = PlainTextSerializerTest.class.getResourceAsStream("jabberwocky.txt");

        try {
            serializer.writeObject(serializer.readObject(inputStream), System.out);
        } catch(Exception exception) {
            System.out.println(exception);
        }
    }
}
