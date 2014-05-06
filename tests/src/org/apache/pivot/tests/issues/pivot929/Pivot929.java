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
package org.apache.pivot.tests.issues.pivot929;

import static org.junit.Assert.assertNotNull;

import java.awt.EventQueue;

import org.apache.pivot.wtk.Clipboard;
import org.apache.pivot.wtk.LocalManifest;
import org.junit.Test;

public class Pivot929 {

    @Test
    public void testClipboard() throws Exception {
        setClipboardContent();
        waitForEvents();
        assertNotNull(Clipboard.getContent().getValue("A"));
        setClipboardContent();
        waitForEvents();
        assertNotNull(Clipboard.getContent().getValue("A"));
    }

    private void setClipboardContent() {
        LocalManifest manifest = new LocalManifest();
        manifest.putValue("A", new Object());
        manifest.putText("A");
        Clipboard.setContent(manifest);
    }

    private void waitForEvents() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                // No-op
            }
        });
    }

}
