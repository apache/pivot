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
package pivot.wtk;

import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Singleton class providing a means of sharing data between components and
 * applications.
 *
 * @author gbrown
 */
public class Clipboard {
    private static Object content = null;
    private static java.awt.datatransfer.Clipboard awtClipboard = null;

    static {
        try {
            awtClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch(SecurityException exception) {
            System.out.println("Access to system clipboard is not allowed; using local clipboard.");
        }
    }

    /**
     * Retrieves the contents of the clipboard.
     *
     * @return
     * The current contents of the clipboard. If the clipboard contents were
     * populated by this application or another application loaded by the same
     * class loader, the return value will be the same value that was passed
     * to the call to {@link #setContent(Object)}.
     * <p>
     * Otherwise, if the application has access to the system clipboard and a
     * supported value is available, it will be returned. Supported types
     * include:
     * <ul>
     * <li>{@link String}</li>
     * </ul>
     * Otherwise, returns <tt>null</tt>.
     */
    public static Object getContent() {
        Object content = Clipboard.content;

        if (content == null
            && awtClipboard != null
            && awtClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            Transferable awtClipboardContents = awtClipboard.getContents(null);

            try {
                content = awtClipboardContents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException exception) {
                System.out.println(exception);
            } catch (IOException exception) {
                System.out.println(exception);
            }
        }

        return content;
    }

    /**
     * Places a value on the clipboard.
     * <p>
     * If the application has access to the system clipboard and the value is
     * of a supported type, it will be copied to the system clipboard.
     * Supported types include:
     * <ul>
     * <li>{@link String}</li>
     * </ul>
     * Otherwise, the string representation of the value will be copied to the
     * system clipboard.
     *
     * @param content
     */
    public static void setContent(Object content) {
        if (content == null) {
            throw new IllegalArgumentException("content is null");
        }

        if (awtClipboard != null) {
            awtClipboard.setContents(new StringSelection(content.toString()), new ClipboardOwner() {
                public void lostOwnership(java.awt.datatransfer.Clipboard awtClipboard,
                    Transferable awtClipboardContents) {
                    Clipboard.content = null;
                }
            });
        }

        Clipboard.content = content;
    }
}
