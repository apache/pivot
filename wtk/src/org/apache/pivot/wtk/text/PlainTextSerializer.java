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
package org.apache.pivot.wtk.text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.pivot.serialization.Serializer;

/**
 * Implementation of the {@link Serializer} interface that reads and writes
 * a plain text document.
 */
public class PlainTextSerializer implements Serializer<Document> {
    private Charset charset = null;

    public static final String MIME_TYPE = "text/plain";
    public static final int BUFFER_SIZE = 2048;

    public PlainTextSerializer() {
        this(Charset.defaultCharset());
    }

    public PlainTextSerializer(String charsetName) {
        this(charsetName == null ? Charset.defaultCharset() : Charset.forName(charsetName));
    }

    public PlainTextSerializer(Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("charset is null.");
        }

        this.charset = charset;
    }

    @Override
    public Document readObject(InputStream inputStream) throws IOException {
        Reader reader = new InputStreamReader(inputStream, charset);
        Document document = readObject(reader);

        return document;
    }

    public Document readObject(Reader reader) throws IOException {
        Document document = new Document();

        BufferedReader bufferedReader = new BufferedReader(reader, BUFFER_SIZE);

        String line = bufferedReader.readLine();
        while (line != null) {
            document.add(new Paragraph(line));
            line = bufferedReader.readLine();
        }

        return document;
    }

    @Override
    public void writeObject(Document document, OutputStream outputStream) throws IOException {
        Writer writer = new OutputStreamWriter(outputStream, charset);
        writeObject(document, writer);
    }

    public void writeObject(Document document, Writer writer) throws IOException {
        writeValue(document, writer);
    }

    private void writeValue(Object object, Writer writer) throws IOException {
        if (writer == null) {
            throw new IllegalArgumentException("writer is null.");
        }

        BufferedWriter bufferedWriter = new BufferedWriter(writer, BUFFER_SIZE);

        if (object instanceof Element) {
            Element element = (Element)object;

            for (Node node : element) {
                writeValue(node, writer);
            }

            if (element instanceof Paragraph) {
                bufferedWriter.newLine();
            }
        } else {
            String text;

            if (object instanceof TextNode) {
                TextNode textNode = (TextNode)object;
                text = textNode.getText();
            } else {
                text = object.toString();
            }

            bufferedWriter.write(text);
        }

        bufferedWriter.flush();
    }

    @Override
    public String getMIMEType(Document document) {
        return MIME_TYPE + "; charset=" + charset.name();
    }
}
