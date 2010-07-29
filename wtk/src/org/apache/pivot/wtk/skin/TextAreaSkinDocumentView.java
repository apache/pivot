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
package org.apache.pivot.wtk.skin;

import org.apache.pivot.wtk.text.Document;

/**
 * Document view.
 */
class TextAreaSkinDocumentView extends TextAreaSkinVerticalElementView {

    public TextAreaSkinDocumentView(TextAreaSkin textAreaSkin, Document document) {
        super(textAreaSkin, document);
    }

    @Override
    public void repaint(int x, int y, int width, int height) {
        super.repaint(x, y, width, height);

        textAreaSkin.repaintComponent(x, y, width, height);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        textAreaSkin.invalidateComponent();
    }

    @Override
    public void validate() {
        if (!isValid()) {
            verticalValidate();
            super.validate();
        }
    }

}