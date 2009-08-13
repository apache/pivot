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
package org.apache.pivot.wtk.content;

import java.io.File;

import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.HorizontalAlignment;

/**
 * List button file renderer.
 *
 * @author gbrown
 */
public class ListButtonFileRenderer extends FileRenderer implements Button.DataRenderer {
    public ListButtonFileRenderer() {
        getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
    }

    @Override
    public void render(Object data, Button button, boolean highlight) {
        if (data != null) {
            File file = (File)data;

            // Update the image view
            imageView.setImage(getIcon(file));
            imageView.getStyles().put("opacity", button.isEnabled() ? 1.0f : 0.5f);

            // Update the label
            String text = file.getName();
            if (text.length() == 0) {
                text = System.getProperty("file.separator");
            }

            label.setText(text);
        }
    }
}
