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
    private static java.awt.datatransfer.Clipboard awtClipboard;
    private static Clipboard instance = new Clipboard();

    public Clipboard() {
        try {
            awtClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch(SecurityException exception) {
            System.out.println("Access to system clipboard is not allowed; using local clipboard.");
            awtClipboard = new java.awt.datatransfer.Clipboard(null);
        }
    }

    public Object get() {
        Object contents = null;

        if (awtClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            Transferable systemClipboardContents = awtClipboard.getContents(this);

            try {
                contents = systemClipboardContents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException exception) {
                System.out.println(exception);
            } catch (IOException exception) {
                System.out.println(exception);
            }
        }

        return contents;
    }

    public void put(Object contents) {
        if (contents == null) {
            throw new IllegalArgumentException("contents is null");
        }

        if (!(contents instanceof String)) {
            throw new IllegalArgumentException("Only string content is currently supported.");
        }

        StringSelection stringSelection = new StringSelection((String)contents);
        awtClipboard.setContents(stringSelection, stringSelection);
    }

    public static Clipboard getInstance() {
        return instance;
    }
}
