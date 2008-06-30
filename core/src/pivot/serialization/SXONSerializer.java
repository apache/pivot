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

/**
 * TODO Need a MIME type
 *
 * <!-- Simple XML Object Notation (SXON) -->
 *
 * <!-- "Hello World" -->
 * <string>Hello World</string>
 *
 * <!-- 100, 100.0; infer type from presence of decimal point -->
 * <number>100</number>
 *
 * <!-- true -->
 * <boolean>true</boolean>
 *
 * <!-- ["Hello World", 100, true] -->
 * <list>
 *     <string>Hello World</string>
 *     <number>100</number>
 *     <boolean>true</boolean>
 * </list>
 *
 * <!-- {foo:"Hello World", bar:100, baz:true} -->
 * <map>
 *     <string key="foo">Hello World</string>
 *     <number key="bar">100</number>
 *     <boolean key="baz">true</boolean>
 *     <map key="inner"/>
 *     <list key="list"/>
 *     <null key="null"/>
 * </map>
 *
 * <!-- null -->
 * <null/>
 *
 * <!-- XML -->
 * <xml>...</xml>
 */
public class SXONSerializer implements Serializer {
    public static final String MIME_TYPE = "text/xml"; // TODO

    public Object readObject(InputStream inputStream) throws IOException,
        SerializationException {
        // TODO Auto-generated method stub
        return null;
    }

    public void writeObject(Object object, OutputStream outputStream)
        throws IOException, SerializationException {
        // TODO Auto-generated method stub

    }

    public String getMIMEType() {
        return MIME_TYPE;
    }
}
