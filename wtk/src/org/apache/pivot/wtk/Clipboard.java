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
package org.apache.pivot.wtk;

import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;

import org.apache.pivot.util.Utils;

/**
 * Singleton class providing a means of sharing data between components and
 * applications.
 */
public final class Clipboard {
    private static LocalManifest content = null;
    private static ClipboardContentListener clipboardContentListener = null;

    private static final ClipboardOwner CLIPBOARD_OWNER = new ClipboardOwner() {
        @Override
        public void lostOwnership(final java.awt.datatransfer.Clipboard clipboard,
            final Transferable contents) {
            LocalManifest previousContent = Clipboard.content;
            Clipboard.content = null;

            if (Clipboard.clipboardContentListener != null) {
                Clipboard.clipboardContentListener.contentChanged(previousContent);
            }
        }
    };

    /** This is a utility class which should never be instantiated. */
    private Clipboard() {
    }

    /**
     * Retrieves the contents of the clipboard.
     *
     * @return The current clipboard content manifest.
     */
    public static Manifest getContent() {
        Manifest currentContent = Clipboard.content;

        if (currentContent == null) {
            try {
                java.awt.datatransfer.Clipboard awtClipboard =
                        Toolkit.getDefaultToolkit().getSystemClipboard();
                currentContent = new RemoteManifest(awtClipboard.getContents(null));
            } catch (SecurityException exception) {
                // No-op
            }
        }

        return currentContent;
    }

    /**
     * Places content on the clipboard.
     *
     * @param newContent The new content manifest to place on the clipboard.
     */
    public static void setContent(final LocalManifest newContent) {
        setContent(newContent, null);
    }

    /**
     * Places content on the clipboard.
     *
     * @param newContent The new content manifest for the clipboard.
     * @param contentListener A listener for changes in the content
     * (which can be {@code null}).
     * @throws IllegalArgumentException if the content is {@code null}.
     */
    public static void setContent(final LocalManifest newContent,
        final ClipboardContentListener contentListener) {
        Utils.checkNull(newContent, "content");

        try {
            java.awt.datatransfer.Clipboard awtClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            LocalManifestAdapter localManifestAdapter = new LocalManifestAdapter(newContent);
            awtClipboard.setContents(localManifestAdapter, CLIPBOARD_OWNER);
        } catch (SecurityException exception) {
            // No-op
        }

        Clipboard.content = newContent;
        Clipboard.clipboardContentListener = contentListener;
    }
}
