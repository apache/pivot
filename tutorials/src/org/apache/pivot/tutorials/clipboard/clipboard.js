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
importPackage(org.apache.pivot.wtk);

function copy() {
    // Copy the selected image to the clipboard
    var selectedSourceIndex = sourceImageCardPane.getSelectedIndex();
    var sourceImageView = sourceImageCardPane.get(selectedSourceIndex);
    var sourceImage = sourceImageView.getImage();

    var content = new LocalManifest();
    content.putImage(sourceImage);

    Clipboard.setContent(content);

    // Diagnostic info
    // Alert("Copy Image").open(window);
}

function paste() {
    // Diagnostic info
    // Prompt("Paste Image").open(window);

    // Paste any available image from the clipboard
    var content = Clipboard.getContent();

    if (content != null) {
        var image = content.getImage();

        if (image != null) {
            destinationImageView.setImage(image);
        }
    }
}