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
package pivot.xml;

import java.io.InputStream;
import java.io.OutputStream;

import pivot.serialization.Serializer;

/**
 * Serializer that reads and writes an XML DOM using instances of
 * {@link Element}.
 * <p>
 * NOTE This class is incomplete.
 *
 * @author gbrown
 */
public class XMLSerializer implements Serializer<Element> {
    public static final String MIME_TYPE = "text/xml";

    public Element readObject(InputStream inputStream) {
        // TODO
        return null;
    }

    public void writeObject(Element element, OutputStream outputStream) {
        // TODO
    }

    public String getMIMEType(Element element) {
        return MIME_TYPE;
    }
}
