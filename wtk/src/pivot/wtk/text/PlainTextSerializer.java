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
package pivot.wtk.text;

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

import pivot.serialization.SerializationException;
import pivot.serialization.Serializer;

/**
 * Implementation of the {@link Serializer} interface that reads data from
 * and writes data to a plain text file.
 * <p>
 * TODO Add support for serializing elements/text nodes. This implies that
 * the return value of readObject() should be a Document, not a String; the
 * inverse applies to writeObject(). Be sure to update the test app, which
 * currently assumes Strings.
 *
 * @author gbrown
 */
public class PlainTextSerializer implements Serializer {
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

    public Object readObject(InputStream inputStream) throws IOException,
        SerializationException {
        Reader reader = new InputStreamReader(inputStream, charset);
        Object object = readObject(reader);

        return object;
    }

    public Object readObject(Reader reader)
        throws IOException, SerializationException {
        Document document = new Document();

        BufferedReader bufferedReader = new BufferedReader(reader, BUFFER_SIZE);

        String line = bufferedReader.readLine();
        while (line != null) {
            document.add(new Paragraph(line));
            line = bufferedReader.readLine();
        }

        return document;
    }

    public void writeObject(Object object, OutputStream outputStream)
        throws IOException, SerializationException {
        Writer writer = new OutputStreamWriter(outputStream, charset);
        writeObject(object, writer);
    }

    public void writeObject(Object object, Writer writer)
        throws IOException, SerializationException {
        if (writer == null) {
            throw new IllegalArgumentException("writer is null.");
        }

        BufferedWriter bufferedWriter = new BufferedWriter(writer, BUFFER_SIZE);

        if (object instanceof Element) {
            Element element = (Element)object;

            for (Node node : element) {
                writeObject(node, writer);
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

    public String getMIMEType(Object object) {
        return MIME_TYPE + "; charset=" + charset.name();
    }
}
