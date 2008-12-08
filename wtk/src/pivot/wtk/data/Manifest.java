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
package pivot.wtk.data;

import java.io.InputStream;

/**
 * Interface representing the contents of a data source such as the clipboard
 * or a drag/drop operation.
 * <p>
 * TODO This could be an abstract base class. If so, we could put
 * package-private methods in Transport that would allow us to clean up the
 * transports when they are no longer needed, rather than relying on the
 * garbage collector to do so.
 * <p>
 * TODO We'll define two concrete, package-private implementations of this
 * class (which will live in <tt>pivot.wtk</tt>):
 * <ul>
 * <li>LocalManifest - maps Transport sequence to Transferable</li>
 * <li>RemoteManifest - maps Transferable to Transport sequence</li>
 * </ul>
 * <p>
 * TODO Clipboard will provide two methods:
 * <p>
 * <tt>getContent():Manifest</tt><br>
 * <tt>setContent(Sequence<Transport>):void</tt><br>
 * <p>
 * DragSource/DropTarget will be similarly defined.
 *
 * @author gbrown
 */
public interface Manifest {
    /**
     * Returns the MIME type of the content at the given index.
     *
     * @param index
     */
    public String getMIMEType(int index);

    /**
     * Returns the input stream at the given index.
     *
     * @param index
     */
    public InputStream getInputStream(int index);

    /**
     * Returns the number of entries in the manifest.
     */
    public int getLength();

    /**
     * Tests the manifest for the existence of a given MIME type.
     *
     * @param mimeType
     *
     * @return
     * <tt>true</tt> if the manifest contains the MIME type; <tt>false</tt>,
     * otherwise.
     */
    public boolean containsMIMEType(String mimeType);
}
