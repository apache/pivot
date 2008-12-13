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
package pivot.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.serialization.SerializationException;
import pivot.serialization.Serializer;

/**
 * Serializer that reads and writes a URI list.
 *
 * @author gbrown
 */
public class URIListSerializer implements Serializer {
    private Charset charset;

    public static final String MIME_TYPE = "text/uri-list";
    public static final int BUFFER_SIZE = 2048;

    public URIListSerializer() {
        this(Charset.defaultCharset());
    }

    public URIListSerializer(String charsetName) {
        this(charsetName == null ? Charset.defaultCharset() : Charset.forName(charsetName));
    }

    public URIListSerializer(Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("charset is null.");
        }

        this.charset = charset;
    }

    public Object readObject(InputStream inputStream)
        throws IOException, SerializationException {
        ArrayList<URL> urlList = new ArrayList<URL>();

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader, BUFFER_SIZE);

        String line = bufferedReader.readLine();
        while (line != null) {
            try {
                URI uri = new URI(line);
                urlList.add(uri.toURL());
            } catch(URISyntaxException exception) {
                // No-op; ignore bad URIs
            }

            line = bufferedReader.readLine();
        }

        bufferedReader.close();

        return urlList;
    }

    @SuppressWarnings("unchecked")
    public void writeObject(Object object, OutputStream outputStream)
        throws IOException, SerializationException {
        List<URL> urlList = (List<URL>)object;

        PrintWriter printWriter = null;

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, charset);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter, BUFFER_SIZE);
            printWriter = new PrintWriter(bufferedWriter);

            for (URL url : urlList) {
                try {
                    printWriter.println(url.toURI());
                } catch(URISyntaxException exception) {
                    // No-op; ignore bad URIs
                }
            }
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }

    public String getMIMEType(Object object) {
        return MIME_TYPE + "; charset=" + charset.name();
    }
}
