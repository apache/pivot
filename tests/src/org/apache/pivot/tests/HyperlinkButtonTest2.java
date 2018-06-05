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
package org.apache.pivot.tests;

import java.io.ByteArrayInputStream;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;


public class HyperlinkButtonTest2 implements Application {
    private Frame frame = null;

    private static final String BXML_FILE =
        "<Frame title=\"Hyperlink Button Test 2\""
      + " xmlns:bxml=\"http://pivot.apache.org/bxml\""
      + " xmlns:content=\"org.apache.pivot.wtk.content\""
      + " xmlns=\"org.apache.pivot.wtk\">"
      + "  <BoxPane orientation=\"vertical\">"
      + "    <HyperlinkButton url=\"http://commons.apache.org\"/>"
      + "    <HyperlinkButton url=\"http://apache.org\">"
      + "      <content:ButtonData text=\"ASF website\" icon=\"/org/apache/pivot/tests/house.png\"/>"
      + "    </HyperlinkButton>"
      + "  </BoxPane>"
      + "</Frame>";

    @Override
    public void startup(final Display display, final Map<String, String> properties) throws Exception {
        BXMLSerializer serializer = new BXMLSerializer();
        frame = (Frame) serializer.readObject(new ByteArrayInputStream(BXML_FILE.getBytes()));

        frame.open(display);
    }

    @Override
    public boolean shutdown(final boolean optional) {
        if (frame != null) {
            frame.close();
        }

        return false;
    }

    public static void main(final String[] args) {
        DesktopApplicationContext.main(HyperlinkButtonTest2.class, args);
    }
}
