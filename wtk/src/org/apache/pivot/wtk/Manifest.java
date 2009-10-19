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

import java.io.IOException;

import org.apache.pivot.io.FileList;
import org.apache.pivot.wtk.media.Image;


/**
 * Interface representing a "manifest". Manifests are collections of data used
 * in clipboard and drag/drop operations.
 * <p>
 * TODO In the future, we may want to make this an abstract base class and
 * add a SerializerDictionary that maps keys to Serializers. Local manifests
 * can use the dictionary to write data out to the native OS, and remote
 * manifests can do the opposite. We'll still need a means of mapping value
 * keys to native IDs (which we would need to do via MIME types if the system
 * is based on AWT's DnD mechanism).
 */
public interface Manifest {
    public String getText() throws IOException;
    public boolean containsText();

    public Image getImage() throws IOException;
    public boolean containsImage();

    public FileList getFileList() throws IOException;
    public boolean containsFileList();

    public Object getValue(String key) throws IOException;
    public boolean containsValue(String key);
}
