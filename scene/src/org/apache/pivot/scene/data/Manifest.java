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
package org.apache.pivot.scene.data;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pivot.scene.media.Image;

/**
 * Interface representing a "manifest". Manifests are collections of data used
 * in clipboard and drag/drop operations.
 */
public interface Manifest {
    public String getText() throws IOException;
    public boolean containsText();

    public Image getImage() throws IOException;
    public boolean containsImage();

    public List<File> getFileList() throws IOException;
    public boolean containsFileList();
}
