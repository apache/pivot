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
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import pivot.io.FileSequence;
import pivot.wtk.media.Picture;

/**
 * Singleton class providing a means of sharing data between components and
 * applications.
 *
 * @author gbrown
 */
public final class Clipboard {
    protected static class TransferableContent implements Transferable {
        private Object content;
        private DataFlavor dataFlavor;

        public TransferableContent(Object content) {
            if (content instanceof Picture) {
                dataFlavor = DataFlavor.imageFlavor;
            } else if (content instanceof FileSequence) {
                dataFlavor = DataFlavor.javaFileListFlavor;
            } else {
                dataFlavor = DataFlavor.stringFlavor;
            }

            this.content = content;
        }

        public Object getTransferData(DataFlavor dataFlavor)
            throws UnsupportedFlavorException, IOException {
            Object transferData = null;

            if (dataFlavor == DataFlavor.imageFlavor) {
                Picture picture = (Picture)content;
                transferData = picture.getBufferedImage();
            } else if (dataFlavor == DataFlavor.javaFileListFlavor) {
                FileSequence fileSequence = (FileSequence)content;
                transferData = fileSequence.getList();
            } else {
                transferData = content.toString();
            }

            return transferData;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {dataFlavor};
        }

        public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
            return (this.dataFlavor.isMimeTypeEqual(dataFlavor));
        }
    }

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
            && awtClipboard != null) {
            content = getContent(awtClipboard.getContents(null));
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
            awtClipboard.setContents(new TransferableContent(content), new ClipboardOwner() {
                public void lostOwnership(java.awt.datatransfer.Clipboard awtClipboard,
                    Transferable awtClipboardContents) {
                    Clipboard.content = null;
                }
            });
        }

        Clipboard.content = content;
    }

    /**
     * Selects the most appropriate content type from a given transferable
     * object.
     *
     * @param transferable
     *
     * @return
     * The preferred content type, or <tt>null</tt> if the transferable
     * object did not contain any usable data.
     */
    protected static Class<?> getContentType(Transferable transferable) {
        Class<?> contentType = null;

        DataFlavor[] transferDataFlavors = transferable.getTransferDataFlavors();

        int i = 0;
        int n = transferDataFlavors.length;
        while (i < n
            && contentType == null) {
            DataFlavor dataFlavor = transferDataFlavors[i++];

            if (dataFlavor.isMimeTypeEqual(DataFlavor.imageFlavor)) {
                contentType = Picture.class;
            } else {
                if (dataFlavor.isMimeTypeEqual(DataFlavor.javaFileListFlavor)) {
                    contentType = FileSequence.class;
                }
            }
        }

        if (contentType == null) {
            contentType = String.class;
        }

        return contentType;
    }

    /**
     * Selects the most appropriate content from a given transferable object.
     *
     * @param transferable
     *
     * @return
     * The preferred content value, or <tt>null</tt> if the transferable
     * object did not contain any usable data.
     */
    @SuppressWarnings("unchecked")
    protected static Object getContent(Transferable transferable) {
        Object content = null;

        DataFlavor[] transferDataFlavors = transferable.getTransferDataFlavors();

        int i = 0;
        int n = transferDataFlavors.length;
        while (i < n
            && content == null) {
            DataFlavor dataFlavor = transferDataFlavors[i++];

            if (dataFlavor.isMimeTypeEqual(DataFlavor.imageFlavor)) {
                try {
                    content = new Picture((BufferedImage)transferable.getTransferData(dataFlavor));
                } catch(Exception exception) {
                    // No-op
                }
            } else {
                if (dataFlavor.isMimeTypeEqual(DataFlavor.javaFileListFlavor)) {
                    try {
                        content = new FileSequence((java.util.List<File>)transferable.getTransferData(dataFlavor));
                    } catch(Exception exception) {
                        // No-op
                    }
                }
            }
        }

        if (content == null
            && transferDataFlavors.length > 0) {
            try {
                content = transferable.getTransferData(transferDataFlavors[0]).toString();
            } catch(Exception exception) {
                // No-op
            }
        }

        return content;
    }
}
