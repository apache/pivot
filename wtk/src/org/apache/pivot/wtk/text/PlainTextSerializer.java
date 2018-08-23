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
import org.apache.pivot.util.Constants;
import org.apache.pivot.util.Utils;

/**
 * Implementation of the {@link Serializer} interface that reads and writes a
 * plain text document.
 */
public class PlainTextSerializer implements Serializer<Document> {
    private Charset charset = null;

    public static final String MIME_TYPE = "text/plain";

    private boolean expandTabs = false;
    private int tabWidth = 4;

    private int bufferSize = Constants.BUFFER_SIZE;


    public PlainTextSerializer() {
        this(Charset.defaultCharset());
    }

    public PlainTextSerializer(String charsetName) {
        this(charsetName == null ? Charset.defaultCharset() : Charset.forName(charsetName));
    }

    public PlainTextSerializer(Charset charset) {
        Utils.checkNull(charset, "charset");

        this.charset = charset;
    }

    public int getTabWidth() {
        return tabWidth;
    }

    public void setTabWidth(int tabWidth) {
        Utils.checkNonNegative(tabWidth, "tabWidth");

        this.tabWidth = tabWidth;
    }

    public boolean getExpandTabs() {
        return expandTabs;
    }

    /**
     * Sets whether tab characters (<code>\t</code>) are expanded to an
     * appropriate number of spaces while reading the text.
     *
     * @param expandTabs <code>true</code> to replace tab characters with space
     * characters (depending on the setting of the {@link #getTabWidth} value)
     * or <code>false</code> to leave tabs alone.
     */
    public void setExpandTabs(boolean expandTabs) {
        this.expandTabs = expandTabs;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Sets the buffer size used internally for reading and writing.  The
     * default value is {@link Constants#BUFFER_SIZE}.  A value of <tt>0</tt> will
     * use the default value in the {@link BufferedReader} and {@link BufferedWriter}
     * classes (probably 8192).
     *
     * @param bufferSize The new buffer size to use (or 0 to use the system default).
     */
    public void setBufferSize(int bufferSize) {
        Utils.checkNonNegative(bufferSize, "bufferSize");

        this.bufferSize = bufferSize;
    }


    @Override
    public Document readObject(InputStream inputStream) throws IOException {
        Utils.checkNull(inputStream, "inputStream");

        Reader reader = new InputStreamReader(inputStream, charset);
        Document document = readObject(reader);

        return document;
    }

    public Document readObject(Reader reader) throws IOException {
        Utils.checkNull(reader, "reader");

        Document document = new Document();

        BufferedReader bufferedReader =
            bufferSize == 0 ? new BufferedReader(reader)
                            : new BufferedReader(reader, bufferSize);

        String line;
        while ((line  = bufferedReader.readLine()) != null) {
            if (expandTabs) {
                int ix = 0;
                StringBuilder buf = new StringBuilder(line);
                while ((ix = buf.indexOf("\t", ix)) >= 0) {
                    buf.deleteCharAt(ix);
                    int spaces = tabWidth - (ix % tabWidth);
                    for (int j = 0; j < spaces; j++) {
                        buf.insert(ix++, ' ');
                    }
                }
                line = buf.toString();
            }
            document.add(new Paragraph(line));
        }

        return document;
    }

    @Override
    public void writeObject(Document document, OutputStream outputStream) throws IOException {
        Utils.checkNull(document, "document");
        Utils.checkNull(outputStream, "outputStream");

        Writer writer = new OutputStreamWriter(outputStream, charset);
        writeObject(document, writer);
    }

    public void writeObject(Document document, Writer writer) throws IOException {
        Utils.checkNull(document, "document");
        Utils.checkNull(writer, "writer");

        writeValue(document, writer);
    }

    private void writeValue(Object object, Writer writer) throws IOException {
        BufferedWriter bufferedWriter =
            bufferSize == 0 ? new BufferedWriter(writer)
                            : new BufferedWriter(writer, bufferSize);

        if (object instanceof ComponentNode) {
            ComponentNode compNode = (ComponentNode) object;
            bufferedWriter.write(compNode.getText());
            bufferedWriter.newLine();
        } else if (object instanceof Element) {
            Element element = (Element) object;

            for (Node node : element) {
                writeValue(node, writer);
            }

            if (element instanceof Paragraph) {
                bufferedWriter.newLine();
            }
        } else {
            String text;

            if (object instanceof TextNode) {
                TextNode textNode = (TextNode) object;
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
