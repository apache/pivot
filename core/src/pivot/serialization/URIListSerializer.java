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
package pivot.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

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
        // TODO Auto-generated method stub
        return null;
    }

    public void writeObject(Object object, OutputStream outputStream)
        throws IOException, SerializationException {
        // TODO Auto-generated method stub

    }

    public String getMIMEType(Object object) {
        return MIME_TYPE + "; charset=" + charset.name();
    }
}
